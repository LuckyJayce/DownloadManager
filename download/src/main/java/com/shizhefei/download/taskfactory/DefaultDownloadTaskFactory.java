package com.shizhefei.download.taskfactory;

import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.base.DownloadTaskFactory;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.task.m3u8.M3u8DownloadTask;
import com.shizhefei.download.task.single.SingleThreadDownloadTask;

import java.util.concurrent.Executor;

public class DefaultDownloadTaskFactory implements DownloadTaskFactory {
    public AbsDownloadTask buildDownloadTask(long downloadId, boolean isOnlyRemove, DownloadInfo.Agency downloadInfoAgency, DownloadDB downloadDB, Executor executor) {
        DownloadParams downloadParams = downloadInfoAgency.getDownloadParams();
        String downloadTaskName = downloadDB.findDownloadTaskName(downloadId);
        if (M3u8DownloadTask.DOWNLOAD_TASK_NAME.equals(downloadTaskName) || downloadParams.getUrl().endsWith(".m3u8")) {
            return new M3u8DownloadTask(downloadId, downloadInfoAgency, downloadDB, executor, isOnlyRemove);
        }
        return new SingleThreadDownloadTask(downloadId, downloadInfoAgency, downloadDB, executor, isOnlyRemove);
    }
}
