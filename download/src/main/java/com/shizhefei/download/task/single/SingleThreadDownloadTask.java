package com.shizhefei.download.task.single;

import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.mvc.RequestHandle;
import com.shizhefei.mvc.ResponseSender;
import com.shizhefei.task.tasks.Tasks;

import java.util.concurrent.Executor;

public class SingleThreadDownloadTask extends AbsDownloadTask {
    private final SingleThreadDownloadImp downloadTask;
    private final Executor executor;
    public static final String DOWNLOAD_TASK_NAME = "SingleThreadDownloadTask";

    public SingleThreadDownloadTask(long downloadId, DownloadParams downloadParams, DownloadDB downloadDB, Executor executor, boolean isOnlyRemove) {
        this.executor = executor;
        downloadTask = new SingleThreadDownloadImp(downloadId, downloadParams, downloadDB, isOnlyRemove);
    }

    @Override
    public RequestHandle execute(ResponseSender<Void> sender) throws Exception {
        return Tasks.async(downloadTask, executor).execute(sender);
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return downloadTask.getDownloadInfo();
    }

    @Override
    public String getDownloadTaskName() {
        return DOWNLOAD_TASK_NAME;
    }

    @Override
    public void remove() {
        downloadTask.remove();
    }
}
