package com.shizhefei.download.taskfactory;

import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.base.DownloadTaskFactory;
import com.shizhefei.download.base.RemoveHandler;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.task.ProxyDownloadTask;

import java.util.concurrent.Executor;

public class DefaultDownloadTaskFactory implements DownloadTaskFactory {
    @Override
    public AbsDownloadTask buildDownloadTask(long downloadId, DownloadParams downloadParams, DownloadDB downloadDB, RemoveHandler removeHandler, Executor executor) {
        return new ProxyDownloadTask(downloadId, downloadParams, downloadDB, removeHandler, executor);
    }
}