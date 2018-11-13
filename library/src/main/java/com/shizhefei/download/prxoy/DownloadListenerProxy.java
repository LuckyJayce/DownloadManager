package com.shizhefei.download.prxoy;

import android.os.Bundle;

import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.exception.DownloadException;
import com.shizhefei.task.Code;
import com.shizhefei.task.ICallback;

public class DownloadListenerProxy implements ICallback<Void> {
    private final long downloadId;
    private DownloadListener downloadListener;

    public DownloadListenerProxy(long downloadId, DownloadListener downloadListener) {
        this.downloadId = downloadId;
        this.downloadListener = downloadListener;
    }

    @Override
    public void onPreExecute(Object task) {
        downloadListener.onPending(downloadId);
    }

    @Override
    public void onProgress(Object task, int percent, long current, long total, Object extraData) {
        int status = -1;
        Bundle bundle = null;
        if (extraData instanceof Bundle) {
            bundle = (Bundle) extraData;
            status = bundle.getInt(DownloadProgressSenderProxy.PROGRESS_STATUS);
        } else if (extraData instanceof Integer) {
            status = (int) extraData;
        }
        switch (status) {
            case DownloadInfo.STATUS_PAUSED:
                downloadListener.onPaused(downloadId);
                break;
            case DownloadInfo.STATUS_DOWNLOAD_ING:
                downloadListener.onDownloadIng(downloadId, current, total);
                break;
            case DownloadInfo.STATUS_START:
                downloadListener.onStart(downloadId);
                break;
            case DownloadInfo.STATUS_CONNECTED:
                if (bundle != null) {
                    HttpInfo httpInfo = bundle.getParcelable(DownloadProgressSenderProxy.PARAM_HTTPINFO);
                    String saveDir = bundle.getString(DownloadProgressSenderProxy.PARAM_SAVEDIR);
                    String saveFileName = bundle.getString(DownloadProgressSenderProxy.PARAM_SAVEFILENAME);
                    String tempFileName = bundle.getString(DownloadProgressSenderProxy.PARAM_TEMPFILENAME);
                    downloadListener.onConnected(downloadId, httpInfo, saveDir, saveFileName, tempFileName);
                }
                break;
        }
    }

    @Override
    public void onPostExecute(Object task, Code code, Exception exception, Void aVoid) {
        switch (code) {
            case EXCEPTION:
                DownloadException downloadException = (DownloadException) exception;
                downloadListener.onError(downloadId, downloadException.getErrorCode(), downloadException.getErrorMessage());
                break;
            case SUCCESS:
                downloadListener.onComplete(downloadId);
                break;
            case CANCEL:
                downloadListener.onPaused(downloadId);
                break;
        }
    }
}