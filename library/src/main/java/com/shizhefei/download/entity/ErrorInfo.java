package com.shizhefei.download.entity;

/**
 * Created by luckyjayce on 18-3-14.
 */

public class ErrorInfo {
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

    private ErrorInfo() {
    }

    public int getErrorCode() {
        return errorCode;
    }

    private void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getHttpCode() {
        return httpCode;
    }

    private void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public String getHttpMessage() {
        return httpMessage;
    }

    private void setHttpMessage(String httpMessage) {
        this.httpMessage = httpMessage;
    }

    public String toJson() {
        return null;
    }

    public void setByJson(String json) {
    }

    public static class Agency {
        private ErrorInfo errorInfo;

        public Agency() {
            this.errorInfo = new ErrorInfo();
        }

        public void set(int errorCode, String errorMessage) {
            errorInfo.setErrorCode(errorCode);
            errorInfo.setErrorMessage(errorMessage);
        }

        public int getErrorCode() {
            return errorInfo.getErrorCode();
        }

        public void setErrorCode(int errorCode) {
            errorInfo.setErrorCode(errorCode);
        }

        public String getErrorMessage() {
            return errorInfo.getErrorMessage();
        }

        public void setErrorMessage(String errorMessage) {
            errorInfo.setErrorMessage(errorMessage);
        }

        public int getHttpCode() {
            return errorInfo.getHttpCode();
        }

        public void setHttpCode(int httpCode) {
            errorInfo.setHttpCode(httpCode);
        }

        public String getHttpMessage() {
            return errorInfo.getHttpMessage();
        }

        public void setHttpMessage(String httpMessage) {
            errorInfo.setHttpMessage(httpMessage);
        }

        public void set(int errorHttp, String httpMessage, int httpCode, String httpMessage1) {

        }

        public ErrorInfo getInfo() {
            return errorInfo;
        }

        public ErrorInfo newInfo() {
            return null;
        }
    }
}
