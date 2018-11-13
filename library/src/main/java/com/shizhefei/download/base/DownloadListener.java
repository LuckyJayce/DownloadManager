package com.shizhefei.download.base;

import com.shizhefei.download.entity.HttpInfo;

public abstract class DownloadListener {
    public void onStart(long downloadId) {

    }

    public void onPending(long downloadId) {

    }

    public void onDownloadIng(long downloadId, long current, long total) {

    }

    public void onConnected(long downloadId, HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName) {

    }

    public void onPaused(long downloadId) {

    }
//
//    public void onBlockComplete(long downloadId) {
//
//    }

    public abstract void onComplete(long downloadId);

    public void onError(long downloadId, int errorCode, String errorMessage) {

    }

    public void onRemove(long downloadId) {

    }
}
