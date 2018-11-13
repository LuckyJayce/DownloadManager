package com.shizhefei.download.exception;

import com.shizhefei.download.entity.ErrorInfo;

/**
 * Created by luckyjayce on 18-3-14.
 */

public class DownloadException extends Exception {
    private final int errorCode;
    private final String errorMessage;

    public DownloadException(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
