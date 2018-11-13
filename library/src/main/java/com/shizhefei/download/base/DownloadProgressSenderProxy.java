package com.shizhefei.download.base;

import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.mvc.ProgressSender;

public class DownloadProgressSenderProxy {
    private ProgressSender progressSender;

    public DownloadProgressSenderProxy(long downloadId, ProgressSender progressSender) {
        this.progressSender = progressSender;
    }

    public void sendConnected(int httpCode, String path, String saveFileName, String tempFileName, String contentType, String newEtag, long current, long total) {

    }

    public void sendProgress(long current, long total) {

    }

    public void sendPaused() {

    }

    public void sendBlockComplete() {

    }

    public void sendOnComplete() {

    }

    public void sendConnected(int httpCode, HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName) {

    }

    public void sendRemove() {

    }
}
