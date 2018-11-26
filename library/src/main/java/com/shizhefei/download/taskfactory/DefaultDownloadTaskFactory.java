package com.shizhefei.download.taskfactory;

import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.base.DownloadTaskFactory;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.task.m3u8.M3u8DownloadTask;
import com.shizhefei.download.task.single.SingleThreadDownloadTask;

import java.util.concurrent.Executor;

public class DefaultDownloadTaskFactory implements DownloadTaskFactory {
    public AbsDownloadTask buildDownloadTask(long downloadId, boolean isOnlyRemove, DownloadParams downloadParams, DownloadDB downloadDB, Executor executor) {
        String downloadTaskName = downloadDB.findDownloadTaskName(downloadId);
        if (M3u8DownloadTask.DOWNLOAD_TASK_NAME.equals(downloadTaskName) || downloadParams.getUrl().endsWith(".m3u8")) {
            return new M3u8DownloadTask(downloadId, downloadParams, downloadDB, executor, isOnlyRemove);
        }
        return new SingleThreadDownloadTask(downloadId, downloadParams, downloadDB, executor, isOnlyRemove);
    }
}
