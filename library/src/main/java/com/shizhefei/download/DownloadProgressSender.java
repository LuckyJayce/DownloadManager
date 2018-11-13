package com.shizhefei.download;

import com.shizhefei.mvc.ProgressSender;

public abstract class DownloadProgressSender implements ProgressSender {
    public abstract void onHttpConnect(int httpCode, String saveFileName, String tempFileName, String contentType, long current, long contentLength);

    public abstract void onProgress(long currentSize, long contentLength);
}
