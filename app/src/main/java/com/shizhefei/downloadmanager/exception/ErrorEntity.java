package com.shizhefei.downloadmanager.exception;

/**
 * Created by luckyjayce on 18-3-14.
 */

public class ErrorEntity {
    /**
     * 未知错误
     */
    public static final int ERROR_UNKNOW = -1;
    /**
     * 权限不足错误码
     */
    public static final int ERROR_PERMISSION = 1;
    /**
     * 文件找不到错误码（一般没有存储权限）
     */
    public static final int ERROR_FILENOTFOUNDEXCEPTION = 2;
    /**
     * io异常错误码
     */
    public static final int ERROR_IOEXCEPTION = 3;
    /**
     * http错误
     */
    public static final int ERROR_HTTP = 4;

    /**
     * http错误
     */
    public static final int ERROR_WIFIREQUIRED = 5;

    public static final int ERROR_PRECONDITION_FAILED = 6;

    public static final int ERROR_EMPTY_SIZE = 7;

    public static final int ERROR_SIZE_CHANGE = 8;

    private int errorCode;
    private String errorMessage;
    private int httpCode;
    private String httpMessage;

    public ErrorEntity(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public ErrorEntity(int errorCode, String errorMessage, int httpCode, String httpMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpCode = httpCode;
        this.httpMessage = httpMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public String getHttpMessage() {
        return httpMessage;
    }

    public void setHttpMessage(String httpMessage) {
        this.httpMessage = httpMessage;
    }
}
