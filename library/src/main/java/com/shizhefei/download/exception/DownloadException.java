package com.shizhefei.download.exception;

/**
 * Created by luckyjayce on 18-3-14.
 */

public class DownloadException extends Exception {
    private final int errorCode;
    private final String errorMessage;
    private final long downloadId;

    public DownloadException(long downloadId, int errorCode, String errorMessage) {
        super(errorMessage);
        this.downloadId = downloadId;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public DownloadException(long downloadId, int errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.downloadId = downloadId;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
