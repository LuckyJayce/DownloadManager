package com.shizhefei.downloadmanager.base;

import com.shizhefei.downloadmanager.ErrorInfo;
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

    public void onPreExecute(long downloadId) {

    }

    public void onProgress(long downloadId, int percent, long current, long total) {

    }

    public void onConnected(long downloadId) {

    }

    public void onPaused(long downloadId) {

    }

    public void onBlockComplete(long downloadId) {

    }

    public abstract void onComplete(long downloadId);

    public void onError(long downloadId, ErrorInfo errorInfo) {

    }

    public void onCancel(long downloadId) {

    }
}
