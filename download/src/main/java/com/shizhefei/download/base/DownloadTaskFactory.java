package com.shizhefei.download.base;

import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.entity.DownloadParams;

import java.util.concurrent.Executor;

public interface DownloadTaskFactory {
    AbsDownloadTask buildDownloadTask(long downloadId, boolean isOnlyRemove, DownloadParams downloadParams, DownloadDB downloadDB, Executor executor);
}