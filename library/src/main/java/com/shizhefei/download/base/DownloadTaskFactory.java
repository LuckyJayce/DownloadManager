package com.shizhefei.download.base;

import com.shizhefei.download.db.DownloadDB;

public interface DownloadTaskFactory {
    AbsDownloadTask buildDownloadTask(long downloadId, DownloadParams downloadParams, DownloadDB downloadDB, RemoveHandler removeHandler);
}
