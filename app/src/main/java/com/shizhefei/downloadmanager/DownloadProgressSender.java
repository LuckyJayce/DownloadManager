package com.shizhefei.downloadmanager;

public interface DownloadProgressSender {
    void onHttpConnect(int httpCode, String saveFileName, String tempFileName, String contentType, long current, long contentLength);

    void onProgress(long currentSize, long contentLength);
}
