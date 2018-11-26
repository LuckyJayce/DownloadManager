package com.shizhefei.download.task.base;

import com.shizhefei.download.entity.HttpInfo;

public interface DownloadProgressListener {
    void onDownloadResetBegin(long downloadId, int reason, long current, long total);

    void onConnected(long downloadId, HttpInfo info, String saveDir, String saveFileName, String tempFileName, long current, long total);

    void onDownloadIng(long downloadId, long current, long total);
}
