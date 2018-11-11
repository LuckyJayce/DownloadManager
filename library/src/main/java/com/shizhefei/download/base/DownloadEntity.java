package com.shizhefei.download.base;

import android.support.annotation.NonNull;

import com.shizhefei.download.HttpInfo;
import com.shizhefei.download.exception.ErrorEntity;

import java.util.List;

public class DownloadEntity {
    public static final int STATUS_NEW = -1;
    public static final int STATUS_pending = 0;//在队列中，还没开始
    public static final int STATUS_START = 1;//开始
    public static final int STATUS_CONNECTED = 2;//连接上服务器
    public static final int STATUS_DOWNLOAD_ING = 3;
    public static final int STATUS_PAUSED = 4;//连接上服务器
    public static final int STATUS_FINISHED = 5;
    public static final int STATUS_CONNECT_ING = 6;
    public static final int STATUS_FAIL = 7;
    private long id;
    private String url;
    private String dir;
    private String filename;
    private String tempFileName;
    private long startTime;//单位 毫秒
    private int status = STATUS_NEW;
    private long total;
    private long current;
    private final HttpInfo httpInfo = new HttpInfo();
    private ErrorEntity errorInfo;
    //当线程下载为null，多线程下载 DownloadEntity对应多个DownloadItem
    private List<DownloadItem> downloadItems;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getTempFileName() {
        return tempFileName;
    }

    public void setTempFileName(String tempFileName) {
        this.tempFileName = tempFileName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    @NonNull
    public HttpInfo getHttpInfo() {
        return httpInfo;
    }

    public ErrorEntity getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(ErrorEntity errorInfo) {
        this.errorInfo = errorInfo;
    }

    public List<DownloadItem> getDownloadItems() {
        return downloadItems;
    }

    public void setDownloadItems(List<DownloadItem> downloadItems) {
        this.downloadItems = downloadItems;
    }
}
