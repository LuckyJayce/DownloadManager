package com.shizhefei.download.prxoy;

import android.os.Bundle;
import android.support.v4.util.Pools;

import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.manager.DownloadManager;
import com.shizhefei.mvc.ProgressSender;

public class DownloadProgressSenderProxy {
    private final long downloadId;
    private ProgressSender progressSender;
    public static final String PARAM_HTTPINFO = "httpInfo";
    public static final String PARAM_SAVEDIR = "saveDir";
    public static final String PARAM_SAVEFILENAME = "saveFileName";
    public static final String PARAM_TEMPFILENAME = "tempFileName";
    public static final String PARAM_DOWNLOADFROMBEGINREASON = "downloadFrombeginReason";
    public static final String PROGRESS_STATUS = "status";
    private static Pools.SynchronizedPool<Bundle> bundleSynchronizedPool = new Pools.SynchronizedPool<>(10);

    public DownloadProgressSenderProxy(long downloadId, ProgressSender progressSender) {
        this.downloadId = downloadId;
        this.progressSender = progressSender;
    }

    public void sendConnected(HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName, long current, long total) {
        Bundle bundle = new Bundle();
        bundle.putInt(PROGRESS_STATUS, DownloadManager.STATUS_CONNECTED);

        bundle.putString(PARAM_SAVEDIR, saveDir);
        bundle.putString(PARAM_SAVEFILENAME, saveFileName);
        bundle.putString(PARAM_TEMPFILENAME, tempFileName);
        bundle.putParcelable(PARAM_HTTPINFO, httpInfo);
        progressSender.sendProgress(current, total, bundle);
    }

    public void sendDownloadFromBegin(long current, long total, int downloadFrombeginReason) {
        Bundle bundle = new Bundle();
        bundle.putInt(PROGRESS_STATUS, DownloadManager.STATUS_DOWNLOAD_RESET_BEGIN);
        bundle.putInt(PARAM_DOWNLOADFROMBEGINREASON, downloadFrombeginReason);
        progressSender.sendProgress(current, total, bundle);
    }

    public void sendStart(long current, long total) {
        progressSender.sendProgress(current, total, DownloadManager.STATUS_START);
    }

    public void sendDownloading(long current, long total) {
        progressSender.sendProgress(current, total, DownloadManager.STATUS_DOWNLOAD_ING);
    }

    public static void release(Bundle bundle) {
        bundle.clear();
        bundleSynchronizedPool.release(bundle);
    }

    private static Bundle acquire() {
        Bundle bundle = bundleSynchronizedPool.acquire();
        if (bundle == null) {
            bundle = new Bundle();
        }
        return bundle;
    }
}
