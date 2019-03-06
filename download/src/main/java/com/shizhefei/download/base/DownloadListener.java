package com.shizhefei.download.base;

import com.shizhefei.download.entity.HttpInfo;

public abstract class DownloadListener {
    public void onPending(long downloadId) {

    }

    public void onStart(long downloadId, long current, long total) {

    }

    public void onDownloadResetSchedule(long downloadId, int reason, long current, long total) {

    }

    public void onProgressUpdate(long downloadId, long current, long total) {

    }

    public void onConnected(long downloadId, HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName, long current, long total) {

    }

    public void onPaused(long downloadId) {

    }

    public void onBlockStart(long downloadId, String blockName, String blockInfo, long current, long total, long blockCurrent, long blockTotal) {

    }

    public void onBlockComplete(long downloadId, String blockName, String blockInfo, long current, long total, long blockCurrent, long blockTotal) {

    }

    public abstract void onComplete(long downloadId);

    public void onError(long downloadId, int errorCode, String errorMessage) {

    }

    public void onRemove(long downloadId) {

    }
}
