package com.shizhefei.download.task.m3u8;

import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.base.RemoveHandler;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.mvc.RequestHandle;
import com.shizhefei.mvc.ResponseSender;
import com.shizhefei.task.tasks.Tasks;

import java.util.concurrent.Executor;

public class ProxyM3u8DownloadTask extends AbsDownloadTask {
    private final M3u8DownloadTask downloadTask;
    private final Executor executor;

    public ProxyM3u8DownloadTask(long downloadId, DownloadParams downloadParams, DownloadDB downloadDB, RemoveHandler removeHandler, Executor executor) {
        this.executor = executor;
        downloadTask = new M3u8DownloadTask(downloadId, downloadParams, downloadDB, removeHandler, executor);
    }

    @Override
    public RequestHandle execute(ResponseSender<Void> sender) throws Exception {
        return Tasks.async(downloadTask, executor).execute(sender);
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return downloadTask.getDownloadInfo();
    }
}
