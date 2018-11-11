package com.shizhefei.download.exception;

/**
 * Created by luckyjayce on 18-3-14.
 */

public class DownloadException extends Exception {
    private ErrorEntity errorEntity;

    public DownloadException(ErrorEntity errorEntity) {
        super(errorEntity.getErrorMessage());
        this.errorEntity = errorEntity;
    }

    public DownloadException(ErrorEntity errorEntity, Throwable throwable) {
        super(errorEntity.getErrorMessage(), throwable);
        this.errorEntity = errorEntity;
    }

    public ErrorEntity getErrorEntity() {
        return errorEntity;
    }

    public void setErrorEntity(ErrorEntity errorEntity) {
        this.errorEntity = errorEntity;
    }
}
