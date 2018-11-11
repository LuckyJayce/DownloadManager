package com.shizhefei.downloadmanager.base;

public interface DownloadTaskFactory {
    AbsDownloadTask buildDownloadTask(long downloadId, DownloadParams downloadParams);

    AbsDownloadTask buildDownloadTask(long downloadId);
}
