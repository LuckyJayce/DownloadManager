package com.shizhefei.download.base;

public interface DownloadTaskFactory {
    AbsDownloadTask buildDownloadTask(long downloadId, DownloadParams downloadParams);

    AbsDownloadTask buildDownloadTask(long downloadId);
}
