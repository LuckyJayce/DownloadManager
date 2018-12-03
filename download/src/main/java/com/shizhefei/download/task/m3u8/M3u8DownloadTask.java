package com.shizhefei.download.task.m3u8;

import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.mvc.RequestHandle;
import com.shizhefei.mvc.ResponseSender;
import com.shizhefei.task.function.Func1;
import com.shizhefei.task.tasks.Tasks;

import java.util.concurrent.Executor;

public class M3u8DownloadTask extends AbsDownloadTask {
    private final M3u8DownloadTaskImp downloadTask;
    private final Executor executor;
    public static final String DOWNLOAD_TASK_NAME = "M3u8DownloadTask";

    public M3u8DownloadTask(long downloadId, DownloadParams downloadParams, DownloadDB downloadDB, Executor executor, boolean isOnlyRemove) {
        this(downloadId, downloadParams, downloadDB, executor, isOnlyRemove, null);
    }

    public M3u8DownloadTask(long downloadId, DownloadParams downloadParams, DownloadDB downloadDB, Executor executor, boolean isOnlyRemove, Func1<String, String> transformRealUrl) {
        this.executor = executor;
        downloadTask = new M3u8DownloadTaskImp(downloadId, downloadParams, downloadDB, isOnlyRemove, transformRealUrl);
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
