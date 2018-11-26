package com.shizhefei.download.task.single;

import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.entity.ErrorInfo;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.exception.RemoveException;
import com.shizhefei.download.manager.DownloadManager;
import com.shizhefei.download.task.DownloadListenerProxy;
import com.shizhefei.download.task.base.DownloadTask;
import com.shizhefei.mvc.ProgressSender;
import com.shizhefei.mvc.RequestHandle;
import com.shizhefei.mvc.ResponseSender;
import com.shizhefei.task.ITask;
import com.shizhefei.task.tasks.Tasks;

import java.util.concurrent.Executor;

public class SingleThreadDownloadTask extends AbsDownloadTask {
    private final Executor executor;
    private final DownloadParams downloadParams;
    private final DownloadTask downloadTask;
    private long downloadId;
    private DownloadDB downloadDB;
    private ErrorInfo.Agency errorInfoAgency;
    private HttpInfo.Agency httpInfoAgency;
    private DownloadInfo.Agency downloadInfoAgency;
    public static final String DOWNLOAD_TASK_NAME = "SingleThreadDownloadTask";
    private volatile boolean isRunning;
    private volatile boolean isRemove;

    public SingleThreadDownloadTask(long downloadId, DownloadParams downloadParams, DownloadDB downloadDB, Executor executor) {
        this.downloadId = downloadId;
        this.downloadDB = downloadDB;
        this.executor = executor;
        this.downloadParams = downloadParams;
        downloadInfoAgency = downloadDB.find(downloadId);
        if (downloadInfoAgency == null) {
            downloadInfoAgency = build(downloadParams);
        }
        errorInfoAgency = downloadInfoAgency.getErrorInfoAgency();
        httpInfoAgency = downloadInfoAgency.getHttpInfoAgency();

        downloadInfoAgency.setStatus(DownloadManager.STATUS_PENDING);
        downloadInfoAgency.setStartTime(System.currentTimeMillis());
        downloadDB.replace(downloadParams, downloadInfoAgency.getInfo());
        downloadTask = new DownloadTask(downloadId, downloadInfoAgency.getInfo());
    }

    @Override
    public RequestHandle execute(ResponseSender<Void> sender) throws Exception {
        DownloadListenerProxy callback = new DownloadListenerProxy(downloadId, downloadListener);
        return Tasks.async(new ITask<Void>() {
            @Override
            public Void execute(ProgressSender progressSender) throws Exception {
                isRunning = true;
                try {
                    downloadTask.execute(progressSender);
                } catch (Exception e) {
                    if (e instanceof RemoveException) {
                        removeFiles();
                    }
                    throw e;
                }
                isRunning = false;
                return null;
            }

            @Override
            public void cancel() {
                downloadTask.cancel();
            }
        }, executor).doOnCallback(callback).execute(sender);
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return downloadInfoAgency.getInfo();
    }

    @Override
    public String getDownloadTaskName() {
        return DOWNLOAD_TASK_NAME;
    }

    @Override
    public void remove() {
        isRemove = true;
        downloadTask.onRemove();
        if (!isRunning) {
            removeFiles();
        }
    }

    private void removeFiles() {
        downloadTask.onRemove();
        downloadDB.delete(downloadId);
    }

    private DownloadListener downloadListener = new DownloadListener() {

        @Override
        public void onDownloadResetBegin(long downloadId, int reason, long current, long total) {
            downloadInfoAgency.setStatus(DownloadManager.STATUS_DOWNLOAD_RESET_BEGIN);
            downloadInfoAgency.setCurrent(0);
            downloadDB.update(downloadInfoAgency.getInfo());
        }

        @Override
        public void onStart(long downloadId, long current, long total) {
            super.onStart(downloadId, current, total);
            downloadInfoAgency.setStatus(DownloadManager.STATUS_START);
        }

        @Override
        public void onPending(long downloadId) {
            super.onPending(downloadId);
            downloadInfoAgency.setStatus(DownloadManager.STATUS_PENDING);
            downloadInfoAgency.setStartTime(System.currentTimeMillis());
            downloadDB.replace(downloadParams, downloadInfoAgency.getInfo());
        }

        @Override
        public void onDownloadIng(long downloadId, long current, long total) {
            super.onDownloadIng(downloadId, current, total);
            downloadInfoAgency.setStatus(DownloadManager.STATUS_DOWNLOAD_ING);
            downloadInfoAgency.setCurrent(current);
            downloadInfoAgency.setTotal(total);
            downloadDB.updateProgress(downloadId, current, total);
        }

        @Override
        public void onConnected(long downloadId, HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName, long current, long total) {
            super.onConnected(downloadId, httpInfo, saveDir, saveFileName, tempFileName, current, total);
            downloadInfoAgency.setStatus(DownloadManager.STATUS_CONNECTED);
            downloadInfoAgency.setFilename(saveFileName);
            downloadInfoAgency.setTempFileName(tempFileName);

            httpInfoAgency.setByInfo(httpInfo);
            downloadDB.update(downloadInfoAgency.getInfo());
        }

        @Override
        public void onPaused(long downloadId) {
            super.onPaused(downloadId);
            downloadInfoAgency.setStatus(DownloadManager.STATUS_PAUSED);
        }

        @Override
        public void onComplete(long downloadId) {
            downloadInfoAgency.setStatus(DownloadManager.STATUS_FINISHED);
            downloadDB.update(downloadInfoAgency.getInfo());
        }

        @Override
        public void onError(long downloadId, int errorCode, String errorMessage) {
            errorInfoAgency.set(errorCode, errorMessage);
            downloadInfoAgency.setStatus(DownloadManager.STATUS_ERROR);
            downloadDB.update(downloadInfoAgency.getInfo());
        }
    };

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
