package com.shizhefei.download.task.single;


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

class SingleThreadDownloadImp implements ITask<Void> {
    private final DownloadTaskImp downloadTask;
    private final DownloadInfo downloadInfo;
    private long downloadId;
    private DownloadDB downloadDB;
    private ErrorInfo.Agency errorInfoAgency;
    private HttpInfo.Agency httpInfoAgency;
    private DownloadInfo.Agency downloadInfoAgency;
    private volatile boolean isRunning;
    private volatile boolean isRemove;
    private volatile boolean isCancel;

    public SingleThreadDownloadImp(long downloadId, DownloadParams downloadParams, DownloadDB downloadDB, boolean isOnlyRemove) {
        this.downloadId = downloadId;
        this.downloadDB = downloadDB;
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
        downloadTask = new DownloadTaskImp(downloadId, downloadParams, downloadParams.getUrl(), downloadInfo.getDir(), downloadInfo.getCurrent(), downloadInfo.getTotal(), downloadInfo.getFileName(), downloadInfo.getTempFileName(), downloadInfo.getHttpInfo().isAcceptRange(), downloadInfo.getHttpInfo().getETag());
    }


    @Override
    public Void execute(ProgressSender progressSender) throws Exception {
        isRunning = true;
        try {
            final DownloadProgressSenderProxy progressSenderProxy = new DownloadProgressSenderProxy(downloadId, progressSender);
            progressSenderProxy.sendStart(downloadInfo.getCurrent(), downloadInfo.getTotal());
            downloadTask.execute(new DownloadProgressListener() {
                @Override
                public void onDownloadResetBegin(long downloadId, int reason, long current, long total) {
                    downloadInfoAgency.setStatus(DownloadManager.STATUS_DOWNLOAD_RESET_BEGIN);
                    downloadInfoAgency.setCurrent(0);
                    downloadDB.update(downloadInfoAgency.getInfo());
                    progressSenderProxy.sendDownloadFromBegin(current, total, reason);
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
                    downloadInfoAgency.setStatus(DownloadManager.STATUS_DOWNLOAD_ING);
                    downloadInfoAgency.setCurrent(current);
                    downloadInfoAgency.setTotal(total);
                    downloadDB.updateProgress(downloadId, current, total);
                    progressSenderProxy.sendDownloading(current, total);
                }
            });
        } catch (DownloadException e) {
            errorInfoAgency.set(e.getErrorCode(), e.getErrorMessage());
            downloadInfoAgency.setStatus(DownloadManager.STATUS_ERROR);
            downloadDB.update(downloadInfoAgency.getInfo());
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
            downloadDB.update(downloadInfoAgency.getInfo());
        } else {
            downloadInfoAgency.setStatus(DownloadManager.STATUS_FINISHED);
            downloadDB.update(downloadInfoAgency.getInfo());
        }
        isRunning = false;
        return null;
    }

    @Override
    public void cancel() {
        isCancel = true;
        downloadInfoAgency.setStatus(DownloadManager.STATUS_PAUSED);
        downloadDB.update(downloadInfoAgency.getInfo());
        downloadTask.cancel();
    }

    public void remove() {
        isRemove = true;
        downloadTask.onRemove();
        if (!isRunning) {
            removeFiles();
        }
    }

    private void removeFiles() {
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
