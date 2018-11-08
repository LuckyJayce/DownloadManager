package com.shizhefei.downloadmanager.base;

import com.shizhefei.downloadmanager.ErrorInfo;
import com.shizhefei.downloadmanager.HttpInfo;

public class DownloadEntity {
    private int id;
    private String url;
    private String dir;
    private String filename;
    private int status;
    private int step;
    private long total;
    private long current;
    private HttpInfo httpInfo;
    private ErrorInfo errorInfo;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public HttpInfo getHttpInfo() {
        return httpInfo;
    }

    public void setHttpInfo(HttpInfo httpInfo) {
        this.httpInfo = httpInfo;
    }

    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(ErrorInfo errorInfo) {
        this.errorInfo = errorInfo;
    }
}
