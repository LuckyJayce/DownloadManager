package com.shizhefei.download.task;

import android.os.Bundle;
import android.text.TextUtils;

import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.exception.DownloadException;
import com.shizhefei.download.exception.RemoveException;
import com.shizhefei.download.manager.DownloadManager;
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
            case DownloadManager.STATUS_PAUSED:
                downloadListener.onPaused(downloadId);
                break;
            case DownloadManager.STATUS_PROGRESS:
                downloadListener.onProgressUpdate(downloadId, current, total);
                break;
            case DownloadProgressSenderProxy.STATUS_BLOCK_START:
                if (bundle != null) {
                    long blockTotal = bundle.getLong(DownloadProgressSenderProxy.PARAM_BLOCK_TOTAL);
                    long blockCurrent = bundle.getLong(DownloadProgressSenderProxy.PARAM_BLOCK_CURRENT);
                    String blockName = bundle.getString(DownloadProgressSenderProxy.PARAM_BLOCK_NAME);
                    String blockInfo = bundle.getString(DownloadProgressSenderProxy.PARAM_BLOCK_INFO);
                    downloadListener.onBlockStart(downloadId, blockName, blockInfo, current, total, blockCurrent, blockTotal);
                }
                break;
            case DownloadProgressSenderProxy.STATUS_BLOCK_COMPLETE:
                if (bundle != null) {
                    long blockTotal = bundle.getLong(DownloadProgressSenderProxy.PARAM_BLOCK_TOTAL);
                    long blockCurrent = bundle.getLong(DownloadProgressSenderProxy.PARAM_BLOCK_CURRENT);
                    String blockName = bundle.getString(DownloadProgressSenderProxy.PARAM_BLOCK_NAME);
                    String blockInfo = bundle.getString(DownloadProgressSenderProxy.PARAM_BLOCK_INFO);
                    downloadListener.onBlockComplete(downloadId, blockName, blockInfo, current, total, blockCurrent, blockTotal);
                }
                break;
            case DownloadManager.STATUS_START:
                downloadListener.onStart(downloadId, current, total);
                break;
            case DownloadManager.STATUS_DOWNLOAD_RESET_SCHEDULE:
                if (bundle != null) {
                    int reason = bundle.getInt(DownloadProgressSenderProxy.PARAM_DOWNLOADFR_RESET_SCHEDULE);
                    downloadListener.onDownloadResetSchedule(downloadId, reason, current, total);
                }
                break;
            case DownloadManager.STATUS_CONNECTED:
                if (bundle != null) {
                    HttpInfo httpInfo = bundle.getParcelable(DownloadProgressSenderProxy.PARAM_HTTPINFO);
                    String saveDir = bundle.getString(DownloadProgressSenderProxy.PARAM_SAVEDIR);
                    String saveFileName = bundle.getString(DownloadProgressSenderProxy.PARAM_SAVEFILENAME);
                    String tempFileName = bundle.getString(DownloadProgressSenderProxy.PARAM_TEMPFILENAME);
                    downloadListener.onConnected(downloadId, httpInfo, saveDir, saveFileName, tempFileName, current, total);
                }
                break;
        }
    }

    @Override
    public void onPostExecute(Object task, Code code, Exception exception, Void aVoid) {
        switch (code) {
            case EXCEPTION:
                if (exception instanceof RemoveException) {
                    //不处理
                } else if (exception instanceof DownloadException) {
                    DownloadException downloadException = (DownloadException) exception;
                    downloadListener.onError(downloadId, downloadException.getErrorCode(), downloadException.getErrorMessage());
                } else {
                    String message = exception.getMessage();
                    if (TextUtils.isEmpty(message)) {
                        message = exception.getClass().getName();
                    }
                    downloadListener.onError(downloadId, DownloadManager.ERROR_UNKNOW, message);
                }
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
