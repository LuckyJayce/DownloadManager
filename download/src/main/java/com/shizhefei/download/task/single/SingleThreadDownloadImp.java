package com.shizhefei.download.task.single;


import android.text.TextUtils;

import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.entity.ErrorInfo;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.exception.DownloadException;
import com.shizhefei.download.exception.RemoveException;
import com.shizhefei.download.manager.DownloadManager;
import com.shizhefei.download.task.DownloadProgressSenderProxy;
import com.shizhefei.download.task.base.DownloadProgressListener;
import com.shizhefei.download.task.base.DownloadTaskImp;
import com.shizhefei.mvc.ProgressSender;
import com.shizhefei.task.ITask;
import com.shizhefei.task.function.Func1;

import java.io.File;

class SingleThreadDownloadImp implements ITask<Void> {
    private volatile DownloadTaskImp downloadTask;
    private final DownloadInfo downloadInfo;
    private final Func1<String, String> transformRealUrl;
    private final DownloadParams downloadParams;
    private long downloadId;
    private DownloadDB downloadDB;
    private ErrorInfo.Agency errorInfoAgency;
    private HttpInfo.Agency httpInfoAgency;
    private DownloadInfo.Agency downloadInfoAgency;
    private volatile boolean isRunning;
    private volatile boolean isRemove;
    private volatile boolean isCancel;

    public SingleThreadDownloadImp(long downloadId, DownloadParams downloadParams, DownloadDB downloadDB, boolean isOnlyRemove, Func1<String, String> transformRealUrl) {
        this.downloadId = downloadId;
        this.downloadDB = downloadDB;
        this.transformRealUrl = transformRealUrl;
        this.downloadParams = downloadParams;
        downloadInfoAgency = downloadDB.find(downloadId);
        if (downloadInfoAgency == null) {
            downloadInfoAgency = build(downloadParams);
        }
        errorInfoAgency = downloadInfoAgency.getErrorInfoAgency();
        httpInfoAgency = downloadInfoAgency.getHttpInfoAgency();

        if (!isOnlyRemove) {
            downloadInfoAgency.setStatus(DownloadManager.STATUS_PENDING);
            downloadInfoAgency.setStartTime(System.currentTimeMillis());
            downloadDB.replace(downloadParams, downloadInfoAgency.getInfo());
        }
        downloadInfo = downloadInfoAgency.getInfo();
    }


    @Override
    public Void execute(ProgressSender progressSender) throws Exception {
        isRunning = true;
        try {
            String url;
            if (transformRealUrl != null) {
                url = transformRealUrl.call(downloadParams.getUrl());
            } else {
                url = downloadParams.getUrl();
            }
            downloadTask = new DownloadTaskImp(downloadId, downloadParams, url, downloadInfo.getDir(), downloadInfo.getCurrent(), downloadInfo.getTotal(), downloadInfo.getFileName(), downloadInfo.getTempFileName(), downloadInfo.getHttpInfo().isAcceptRange(), downloadInfo.getHttpInfo().getETag());
            final DownloadProgressSenderProxy progressSenderProxy = new DownloadProgressSenderProxy(downloadId, progressSender);
            progressSenderProxy.sendStart(downloadInfo.getCurrent(), downloadInfo.getTotal());
            downloadTask.execute(new DownloadProgressListener() {
                @Override
                public void onDownloadResetSchedule(long downloadId, int reason, long current, long total) {
                    downloadInfoAgency.setStatus(DownloadManager.STATUS_DOWNLOAD_RESET_SCHEDULE);
                    downloadInfoAgency.setCurrent(0);
                    downloadDB.updateDownloadResetSchedule(downloadInfoAgency.getId(), downloadInfoAgency.getCurrent(), downloadInfoAgency.getTotal(), downloadInfoAgency.getExtInfo());
                    progressSenderProxy.sendDownloadResetSchedule(current, total, reason);
                }

                @Override
                public void onConnected(long downloadId, HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName, long current, long total) {
                    downloadInfoAgency.setStatus(DownloadManager.STATUS_CONNECTED);
                    downloadInfoAgency.setFilename(saveFileName);
                    downloadInfoAgency.setTempFileName(tempFileName);

                    httpInfoAgency.setByInfo(httpInfo);
                    downloadDB.update(downloadInfoAgency.getInfo());
                    progressSenderProxy.sendConnected(httpInfo, saveDir, saveFileName, tempFileName, current, total);
                }

                @Override
                public void onDownloadIng(long downloadId, long current, long total) {
                    downloadInfoAgency.setStatus(DownloadManager.STATUS_PROGRESS);
                    downloadInfoAgency.setCurrent(current);
                    downloadInfoAgency.setTotal(total);
                    downloadDB.updateProgress(downloadId, current, total);
                    progressSenderProxy.sendDownloading(current, total);
                }
            });
        } catch (DownloadException e) {
            errorInfoAgency.set(e.getErrorCode(), e.getErrorMessage());
            downloadInfoAgency.setStatus(DownloadManager.STATUS_ERROR);
            downloadDB.updateError(downloadInfoAgency.getId(), errorInfoAgency.getInfo());
            isRunning = false;
            throw e;
        }
        if (isRemove) {
            removeFiles();
            isRunning = false;
            throw new RemoveException(downloadId);
        }
        if (isCancel) {
            downloadInfoAgency.setStatus(DownloadManager.STATUS_PAUSED);
            downloadDB.updateStatus(downloadInfoAgency.getId(), DownloadManager.STATUS_PAUSED);
        } else {
            downloadInfoAgency.setStatus(DownloadManager.STATUS_FINISHED);
            downloadDB.updateStatus(downloadInfoAgency.getId(), DownloadManager.STATUS_FINISHED);
        }
        isRunning = false;
        return null;
    }

    @Override
    public void cancel() {
        isCancel = true;
        if (downloadTask != null) {
            downloadTask.cancel();
        }
        downloadInfoAgency.setStatus(DownloadManager.STATUS_PAUSED);
        downloadDB.updateStatus(downloadInfoAgency.getId(), DownloadManager.STATUS_PAUSED);
    }

    public void remove() {
        isRemove = true;
        if (downloadTask != null) {
            downloadTask.remove();
        }
        if (!isRunning) {
            removeFiles();
        }
    }

    private void removeFiles() {
        try {
            if (!TextUtils.isEmpty(downloadInfo.getFileName())) {
                new File(downloadInfo.getDir(), downloadInfo.getFileName()).delete();
            }
            if (!TextUtils.isEmpty(downloadInfo.getTempFileName())) {
                new File(downloadInfo.getDir(), downloadInfo.getTempFileName()).delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        downloadDB.delete(downloadId);
    }

    public DownloadInfo getDownloadInfo() {
        return downloadInfoAgency.getInfo();
    }

    public String getDownloadTaskName() {
        return SingleThreadDownloadTask.DOWNLOAD_TASK_NAME;
    }

    private DownloadInfo.Agency build(DownloadParams downloadParams) {
        DownloadInfo.Agency downloadInfoAgency = new DownloadInfo.Agency(downloadParams);
        downloadInfoAgency.setId(downloadId);
        downloadInfoAgency.setUrl(downloadParams.getUrl());
        downloadInfoAgency.setCurrent(0);
        if (downloadParams.getTotalSize() > 0) {
            downloadInfoAgency.setTotal(downloadParams.getTotalSize());
        } else {
            downloadInfoAgency.setTotal(0);
        }
        downloadInfoAgency.setDownloadTaskName(getDownloadTaskName());
        downloadInfoAgency.setStartTime(System.currentTimeMillis());
        downloadInfoAgency.setStatus(DownloadManager.STATUS_PENDING);
        downloadInfoAgency.setFilename(downloadParams.getFileName());
        downloadInfoAgency.setDir(downloadParams.getDir());
        return downloadInfoAgency;
    }
}
