package com.shizhefei.download.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.StatFs;

import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.imp.DownloadManager;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.Locale;

public class FileDownloadUtils {
    public static final int RANGE_INFINITE = -1;
    public static final int TOTAL_VALUE_IN_CHUNKED_RESOURCE = -1;

    public static boolean isNetworkNotOnWifiType() {
        final ConnectivityManager manager = (ConnectivityManager) DownloadManager.getApplicationContext().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            DownloadLogUtils.d("failed to get connectivity manager!");
            return true;
        }
        //noinspection MissingPermission, because we check permission accessable when invoked
        @SuppressLint("MissingPermission") final NetworkInfo info = manager.getActiveNetworkInfo();
        return info == null || info.getType() != ConnectivityManager.TYPE_WIFI;
    }

    public static boolean checkPermission(String accessNetworkState) {
        return false;
    }

    public static String findEtag(long downloadId, HttpURLConnection httpURLConnection) {
        if (httpURLConnection == null) {
            throw new RuntimeException("connection is null when findEtag");
        }

        final String newEtag = httpURLConnection.getHeaderField("Etag");

        DownloadLogUtils.d("eTag find %s for task(%d)", newEtag, downloadId);

        return newEtag;
    }


    public static boolean isPreconditionFailed(HttpURLConnection connection, int httpCode, DownloadInfo entity, boolean isAcceptRange) throws Exception {
        final boolean onlyFromBeginning = (httpCode == HttpURLConnection.HTTP_OK
                || httpCode == HttpURLConnection.HTTP_CREATED);

        final String oldEtag = entity.getHttpInfo().getETag();
        String newEtag = FileDownloadUtils.findEtag(entity.getId(), connection);

        // handle whether need retry because of etag is overdue
        boolean isPreconditionFailed = false;
        if (httpCode == HttpURLConnection.HTTP_PRECON_FAILED) {
            isPreconditionFailed = true;
        }

        if (oldEtag != null && !oldEtag.equals(newEtag)) {
            // etag changed.
            if (onlyFromBeginning || isAcceptRange) {
                // 200 or 206
                isPreconditionFailed = true;
            }
        }
        return isPreconditionFailed;
    }

    public static boolean isAcceptRange(int httpCode, HttpURLConnection httpURLConnection) {
        if (httpCode == HttpURLConnection.HTTP_PARTIAL) return true;
        final String acceptRanges = httpURLConnection.getHeaderField("Accept-Ranges");
        return "bytes".equals(acceptRanges);
    }

    public static void addRangeHeader(HttpURLConnection connection, long startOffset, long endOffset) throws ProtocolException {
        final String range;
        if (endOffset == RANGE_INFINITE) {
            range = FileDownloadUtils.formatString("bytes=%d-", startOffset);
        } else {
            range = FileDownloadUtils
                    .formatString("bytes=%d-%d", startOffset, endOffset);
        }
        connection.addRequestProperty("Range", range);
    }

    public static long getFreeSpaceBytes(final String path) {
        long freeSpaceBytes;
        final StatFs statFs = new StatFs(path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            freeSpaceBytes = statFs.getAvailableBytes();
        } else {
            //noinspection deprecation
            freeSpaceBytes = statFs.getAvailableBlocks() * (long) statFs.getBlockSize();
        }
        return freeSpaceBytes;
    }

    public static String formatStringE(Throwable e, String text, Object... args) {
        return String.format(Locale.getDefault(), text, args) + " throwable:" + e.getMessage();
    }

    public static String formatString(final String msg, Object... args) {
        return String.format(Locale.getDefault(), msg, args);
    }
}
