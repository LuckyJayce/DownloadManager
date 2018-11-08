package com.shizhefei.downloadmanager.base;

import com.shizhefei.downloadmanager.imp.DownloadTask;

public interface DownloadTaskFactory {
    DownloadTask buildDownloadTask(DownloadParams downloadParams);
}
