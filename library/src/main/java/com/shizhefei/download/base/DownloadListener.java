package com.shizhefei.download.base;

import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.task.Code;
import com.shizhefei.task.ICallback;

public abstract class DownloadListener implements ICallback<Void> {

    @Override
    public final void onPreExecute(Object task) {

    }

    @Override
    public final void onProgress(Object task, int percent, long current, long total, Object extraData) {

    }

    @Override
    public final void onPostExecute(Object task, Code code, Exception exception, Void aVoid) {

    }

    public void onStart(long downloadId) {

    }

    public void onPending(long downloadId) {

    }

    public void onProgress(long downloadId, int percent, long current, long total) {

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
