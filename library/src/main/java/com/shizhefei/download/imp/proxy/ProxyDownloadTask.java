package com.shizhefei.download.imp.proxy;

import android.text.TextUtils;

import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.base.RemoveHandler;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.base.DownloadParams;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.entity.ErrorInfo;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.imp.single.DownloadTask;
import com.shizhefei.download.prxoy.DownloadListenerProxy;
import com.shizhefei.mvc.RequestHandle;
import com.shizhefei.mvc.ResponseSender;
import com.shizhefei.task.tasks.Tasks;

import java.io.File;
import java.util.concurrent.Executor;

public class ProxyDownloadTask extends AbsDownloadTask {
    private final Executor executor;
    private long downloadId;
    private DownloadDB downloadDB;
    private ErrorInfo.Agency errorInfoAgency;
    private HttpInfo.Agency httpInfoAgency;
    private RemoveHandler removeHandler;
    private DownloadInfo.Agency downloadInfoAgency;
    private DownloadParams downloadParams;

    public ProxyDownloadTask(long downloadId, DownloadParams downloadParams, DownloadDB downloadDB, RemoveHandler removeHandler, Executor executor) {
        this.downloadId = downloadId;
        this.downloadParams = downloadParams;
        this.downloadDB = downloadDB;
        this.executor = executor;
        downloadInfoAgency = downloadDB.find(downloadId);
        if (downloadInfoAgency == null) {
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
            case DownloadInfo.STATUS_START:
            case DownloadInfo.STATUS_DOWNLOAD_ING:
            case DownloadInfo.STATUS_CONNECTED:
            case DownloadInfo.STATUS_DOWNLOAD_RESET_BEGIN:
                downloadInfoAgency.setStatus(DownloadInfo.STATUS_PENDING);
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
        DownloadListenerProxy callback = new DownloadListenerProxy(downloadId, downloadListener);
        return Tasks.async(downloadTask, executor).doOnCallback(callback).execute(sender);
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return downloadInfoAgency.getInfo();
    }

    private DownloadListener downloadListener = new DownloadListener() {

        @Override
        public void onDownloadResetBegin(long downloadId) {
            downloadInfoAgency.setStatus(DownloadInfo.STATUS_DOWNLOAD_RESET_BEGIN);
            downloadInfoAgency.setCurrent(0);
            downloadDB.update(downloadInfoAgency.getInfo());
        }

        @Override
        public void onPending(long downloadId) {
            super.onPending(downloadId);
            downloadInfoAgency.setStatus(DownloadInfo.STATUS_PENDING);
            downloadInfoAgency.setStartTime(System.currentTimeMillis());
            downloadDB.replace(downloadParams, downloadInfoAgency.getInfo());
        }

        @Override
        public void onStart(long downloadId) {
            super.onStart(downloadId);
            downloadInfoAgency.setStatus(DownloadInfo.STATUS_START);
        }

        @Override
        public void onDownloadIng(long downloadId, long current, long total) {
            super.onDownloadIng(downloadId, current, total);
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
            downloadInfoAgency.setStatus(DownloadInfo.STATUS_FAIL);
            downloadDB.update(downloadInfoAgency.getInfo());
        }

        @Override
        public void onRemove(long downloadId) {
            downloadInfoAgency.setStatus(DownloadInfo.STATUS_PAUSED);
            downloadDB.delete(downloadId);
        }
    };

    private DownloadInfo.Agency build(DownloadParams downloadParams) {
        DownloadInfo.Agency downloadInfoAgency = new DownloadInfo.Agency(downloadParams);
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
