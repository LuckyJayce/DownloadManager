package com.shizhefei.download.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.StatFs;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.manager.DownloadManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DownloadUtils {
    public static final int RANGE_INFINITE = -1;
    public static final int TOTAL_VALUE_IN_CHUNKED_RESOURCE = -1;

    public static boolean isNetworkNotOnWifiType() {
        final ConnectivityManager manager = (ConnectivityManager) DownloadManager.getApplicationContext().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            logD("failed to get connectivity manager!");
            return true;
        }
        //noinspection MissingPermission, because we check permission accessable when invoked
        @SuppressLint("MissingPermission") final NetworkInfo info = manager.getActiveNetworkInfo();
        return info == null || info.getType() != ConnectivityManager.TYPE_WIFI;
    }

    public static boolean checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static String findEtag(long downloadId, HttpURLConnection httpURLConnection) {
        if (httpURLConnection == null) {
            throw new RuntimeException("connection is null when findEtag");
        }

        final String newEtag = httpURLConnection.getHeaderField("Etag");

        logD("eTag find %s for task(%d)", newEtag, downloadId);

        return newEtag;
    }


    public static boolean isPreconditionFailed(HttpURLConnection connection, int httpCode, DownloadInfo entity, boolean isAcceptRange) throws Exception {
        final boolean onlyFromBeginning = (httpCode == HttpURLConnection.HTTP_OK
                || httpCode == HttpURLConnection.HTTP_CREATED);

        final String oldEtag = entity.getHttpInfo().getETag();
        String newEtag = DownloadUtils.findEtag(entity.getId(), connection);

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
            range = DownloadUtils.formatString("bytes=%d-", startOffset);
        } else {
            range = DownloadUtils
                    .formatString("bytes=%d-%logD", startOffset, endOffset);
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

    public static JSONObject toJsonObject(Map<String, List<String>> map) {
        return new JSONObject(map);
    }

    public static JSONObject toJsonObject2(Map<String, String> map) {
        return new JSONObject(map);
    }

    public static Map<String, List<String>> parseJson(JSONObject jsonObject) {
        Map<String, List<String>> map = new HashMap<>();
        try {
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONArray jsonArray = jsonObject.optJSONArray(key);
                int length = jsonArray.length();
                List<String> values = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    String value = jsonArray.getString(i);
                    values.add(value);
                }
                map.put(key, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static Map<String, String> parseJson2(JSONObject jsonObject) {
        Map<String, String> map = new HashMap<>();
        try {
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = jsonObject.optString(key);
                map.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static void logD(String msg, Object... args) {
        Log.d(DownloadManager.LIB_NAME, formatString(msg, args));
    }

    public static void logE(Throwable e, String msg, Object... args) {
        Log.e(DownloadManager.LIB_NAME, formatString(msg, args), e);
    }

    public static void logE(String msg, Object... args) {
        Log.e(DownloadManager.LIB_NAME, formatString(msg, args));
    }

    //-----------------------------------------------------------------------

    /**
     * Deletes a directory recursively.
     *
     * @param directory directory to delete
     * @throws IOException              in case deletion is unsuccessful
     * @throws IllegalArgumentException if {@code directory} does not exist or is not a directory
     */
    public static void deleteDirectory(final File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        if (!isSymlink(directory)) {
            cleanDirectory(directory);
        }

        if (!directory.delete()) {
            final String message =
                    "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    /**
     * Determines whether the specified file is a Symbolic Link rather than an actual file.
     * <p>
     * Will not return true if there is a Symbolic Link anywhere in the path,
     * only if the specific file is.
     * <p>
     * When using jdk1.7, this method delegates to {@code boolean java.nio.file.Files.isSymbolicLink(Path path)}
     *
     * <b>Note:</b> the current implementation always returns {@code false} if running on
     * jkd1.6 and the system is detected as Windows using {@link FilenameUtils#isSystemWindows()}
     * <p>
     * For code that runs on Java 1.7 or later, use the following method instead:
     * <br>
     * {@code boolean java.nio.file.Files.isSymbolicLink(Path path)}
     *
     * @param file the file to check
     * @return true if the file is a Symbolic Link
     * @throws IOException if an IO error occurs while checking the file
     * @since 2.0
     */
    public static boolean isSymlink(final File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Files.isSymbolicLink(file.toPath());
        }
        return false;
    }

    /**
     * Cleans a directory without deleting it.
     *
     * @param directory directory to clean
     * @throws IOException              in case cleaning is unsuccessful
     * @throws IllegalArgumentException if {@code directory} does not exist or is not a directory
     */
    public static void cleanDirectory(final File directory) throws IOException {
        final File[] files = verifiedListFiles(directory);

        IOException exception = null;
        for (final File file : files) {
            try {
                forceDelete(file);
            } catch (final IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * Lists files in a directory, asserting that the supplied directory satisfies exists and is a directory
     *
     * @param directory The directory to list
     * @return The files in the directory, never null.
     * @throws IOException if an I/O error occurs
     */
    private static File[] verifiedListFiles(final File directory) throws IOException {
        if (!directory.exists()) {
            final String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            final String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        final File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }
        return files;
    }


    //-----------------------------------------------------------------------

    /**
     * Deletes a file. If file is a directory, delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted.
     * (java.io.File methods returns a boolean)</li>
     * </ul>
     *
     * @param file file or directory to delete, must not be {@code null}
     * @throws NullPointerException  if the directory is {@code null}
     * @throws FileNotFoundException if the file was not found
     * @throws IOException           in case deletion is unsuccessful
     */
    public static void forceDelete(final File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            final boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                final String message =
                        "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

}
