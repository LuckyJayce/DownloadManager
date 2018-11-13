package com.shizhefei.download.imp.proxy;

import android.text.TextUtils;
import android.util.Pair;

import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.base.RemoveHandler;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.base.DownloadParams;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.entity.ErrorInfo;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.imp.single.DownloadTask;
import com.shizhefei.mvc.RequestHandle;
import com.shizhefei.mvc.ResponseSender;
import com.shizhefei.task.tasks.Tasks;

import java.io.File;

public class ProxyDownloadTask extends AbsDownloadTask {
    private long downloadId;
    private DownloadDB downloadDB;
    private ErrorInfo.Agency errorInfoAgency;
    private HttpInfo.Agency httpInfoAgency;
    private RemoveHandler removeHandler;
    private DownloadInfo.Agency downloadInfoAgency;
    private DownloadParams downloadParams;

    public ProxyDownloadTask(long downloadId, DownloadParams downloadParams, DownloadDB downloadDB, RemoveHandler removeHandler) {
        this.downloadId = downloadId;
        this.downloadParams = downloadParams;
        this.downloadDB = downloadDB;
        Pair<DownloadInfo.Agency, DownloadParams> paramsPair = downloadDB.find(downloadId);
        if (paramsPair != null) {
            downloadInfoAgency = paramsPair.first;
        } else {
            downloadInfoAgency = build(downloadParams);
        }
        errorInfoAgency = downloadInfoAgency.getErrorInfoAgency();
        httpInfoAgency = downloadInfoAgency.getHttpInfoAgency();
        this.removeHandler = removeHandler;
    }

    @Override
    public RequestHandle execute(ResponseSender<Void> sender) throws Exception {
        DownloadInfo downloadInfo = downloadInfoAgency.getInfo();
        switch (downloadInfo.getStatus()) {
            case DownloadInfo.STATUS_CONNECT_ING:
            case DownloadInfo.STATUS_START:
            case DownloadInfo.STATUS_DOWNLOAD_ING:
            case DownloadInfo.STATUS_CONNECTED:

                break;
            case DownloadInfo.STATUS_FAIL:
            case DownloadInfo.STATUS_FINISHED:
                if (!TextUtils.isEmpty(downloadInfo.getTempFileName())) {
                    File file = new File(downloadInfo.getDir(), downloadInfo.getTempFileName());
                    if (file.exists()) {
                        file.delete();
                    }
                }
                if (!TextUtils.isEmpty(downloadInfo.getFilename())) {
                    File file = new File(downloadInfo.getDir(), downloadInfo.getFilename());
                    if (file.exists()) {
                        file.delete();
                    }
                }
                break;
            case DownloadInfo.STATUS_PAUSED:
                if (!TextUtils.isEmpty(downloadInfo.getTempFileName())) {
                    File file = new File(downloadInfo.getDir(), downloadInfo.getTempFileName());
                    if (!file.exists()) {
                        downloadInfoAgency.setStatus(DownloadInfo.STATUS_START);
                        downloadInfoAgency.setCurrent(0);
                    }
                }
                break;
        }

        DownloadTask downloadTask = new DownloadTask(downloadId, downloadParams, downloadInfoAgency.getInfo());
        removeHandler.addRemoveListener(downloadTask);
        return Tasks.async(downloadTask).doOnCallback(downloadListener).execute(sender);
    }

    @Override
    public DownloadParams getDownloadParams() {
        return downloadParams;
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return downloadInfoAgency.getInfo();
    }

    private DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onPending(long downloadId) {
            super.onPending(downloadId);
            downloadInfoAgency.setStartTime(System.currentTimeMillis());
            downloadInfoAgency.setStatus(DownloadInfo.STATUS_PENDING);
            downloadDB.replace(downloadParams, downloadInfoAgency.getInfo());
        }

        @Override
        public void onStart(long downloadId) {
            super.onStart(downloadId);
            downloadInfoAgency.setStatus(DownloadInfo.STATUS_START);
        }

        @Override
        public void onProgress(long downloadId, int percent, long current, long total) {
            super.onProgress(downloadId, percent, current, total);
            downloadInfoAgency.setStatus(DownloadInfo.STATUS_DOWNLOAD_ING);
            downloadInfoAgency.setCurrent(current);
            downloadInfoAgency.setTotal(total);
            downloadDB.update(downloadId, current, total);
        }

        @Override
        public void onConnected(long downloadId, HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName) {
            super.onConnected(downloadId, httpInfo, saveDir, saveFileName, tempFileName);
            downloadInfoAgency.setStatus(DownloadInfo.STATUS_CONNECTED);
            downloadInfoAgency.setFilename(saveFileName);
            downloadInfoAgency.setTempFileName(tempFileName);

            httpInfoAgency.setContentLength(httpInfo.getContentLength());
            httpInfoAgency.setContentType(httpInfo.getContentType());
            httpInfoAgency.setETag(httpInfo.getETag());
            httpInfoAgency.setHttpCode(httpInfo.getHttpCode());
            downloadDB.update(downloadInfoAgency.getInfo());
        }

        @Override
        public void onPaused(long downloadId) {
            super.onPaused(downloadId);
            downloadInfoAgency.setStatus(DownloadInfo.STATUS_PAUSED);
        }

        @Override
        public void onComplete(long downloadId) {
            downloadInfoAgency.setStatus(DownloadInfo.STATUS_FINISHED);
            downloadDB.update(downloadInfoAgency.getInfo());
        }

        @Override
        public void onError(long downloadId, int errorCode, String errorMessage) {
            errorInfoAgency.set(errorCode, errorMessage);
            downloadDB.update(downloadInfoAgency.getInfo());
        }

        @Override
        public void onRemove(long downloadId) {
            downloadInfoAgency.setStatus(DownloadInfo.STATUS_PAUSED);
            downloadDB.delete(downloadId);
        }
    };

    private DownloadInfo.Agency build(DownloadParams downloadParams) {
        DownloadInfo.Agency downloadInfoAgency = new DownloadInfo.Agency();
        downloadInfoAgency.setId(downloadId);
        downloadInfoAgency.setUrl(downloadParams.getUrl());
        downloadInfoAgency.setCurrent(0);
        downloadInfoAgency.setTotal(0);
        downloadInfoAgency.setStatus(DownloadInfo.STATUS_PENDING);
        downloadInfoAgency.setFilename(downloadParams.getFileName());
        downloadInfoAgency.setDir(downloadParams.getDir());
        return downloadInfoAgency;
    }
}
