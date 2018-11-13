package com.shizhefei.download.exception;

public class RemoveException extends Exception {
    private long downloadId;

    public RemoveException(long downloadId) {
        super("remove downloadId:" + downloadId);
        this.downloadId = downloadId;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }
}
