package com.shizhefei.download.entity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.shizhefei.download.manager.DownloadManager;

import java.util.List;

public class DownloadInfo {
    private long id;
    private String url;
    private String dir;
    private String filename;
    private String tempFileName;
    private long startTime;//单位 毫秒
    private int status = DownloadManager.STATUS_PENDING;
    private long total;
    private long current;
    private final DownloadParams downloadParams;
    private final HttpInfo httpInfo;
    private final ErrorInfo errorInfo;
    private String extInfo;
    //当线程下载为null，多线程下载 DownloadEntity对应多个DownloadItem
    private List<DownloadItem> downloadItems;

    private DownloadInfo(@NonNull DownloadParams downloadParams, @NonNull HttpInfo httpInfo, @NonNull ErrorInfo errorInfo) {
        this.downloadParams = downloadParams;
        this.httpInfo = httpInfo;
        this.errorInfo = errorInfo;
    }

    public DownloadParams getDownloadParams() {
        return downloadParams;
    }

    public String getExtInfo() {
        return extInfo;
    }

    private void setExtInfo(String extInfo) {
        this.extInfo = extInfo;
    }

    public long getStartTime() {
        return startTime;
    }

    private void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getTempFileName() {
        return tempFileName;
    }

    private void setTempFileName(String tempFileName) {
        this.tempFileName = tempFileName;
    }

    public long getId() {
        return id;
    }

    private void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    private void setUrl(String url) {
        this.url = url;
    }

    @NonNull
    public String getDir() {
        return dir;
    }

    private void setDir(String dir) {
        this.dir = dir;
    }

    @Nullable
    public String getFilename() {
        return filename;
    }

    private void setFilename(String filename) {
        this.filename = filename;
    }

    public int getStatus() {
        return status;
    }

    private void setStatus(int status) {
        this.status = status;
    }

    public long getTotal() {
        return total;
    }

    private void setTotal(long total) {
        this.total = total;
    }

    public long getCurrent() {
        return current;
    }

    private void setCurrent(long current) {
        this.current = current;
    }

    @NonNull
    public HttpInfo getHttpInfo() {
        return httpInfo;
    }

    @NonNull
    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }

    public List<DownloadItem> getDownloadItems() {
        return downloadItems;
    }

    private void setDownloadItems(List<DownloadItem> downloadItems) {
        this.downloadItems = downloadItems;
    }

    public static class Agency {
        private final HttpInfo.Agency httpInfoAgency;
        private final ErrorInfo.Agency errorInfoAgency;
        private DownloadInfo downloadInfo;

        public Agency(DownloadParams downloadParams) {
            httpInfoAgency = new HttpInfo.Agency();
            errorInfoAgency = new ErrorInfo.Agency();
            this.downloadInfo = new DownloadInfo(downloadParams, httpInfoAgency.getInfo(), errorInfoAgency.getInfo());
        }

        public DownloadInfo getInfo() {
            return downloadInfo;
        }

        public long getStartTime() {
            return downloadInfo.getStartTime();
        }

        public void setStartTime(long startTime) {
            downloadInfo.setStartTime(startTime);
        }

        public String getTempFileName() {
            return downloadInfo.getTempFileName();
        }

        public void setTempFileName(String tempFileName) {
            downloadInfo.setTempFileName(tempFileName);
        }

        public long getId() {
            return downloadInfo.getId();
        }

        public void setId(long id) {
            downloadInfo.setId(id);
        }

        public String getUrl() {
            return downloadInfo.getUrl();
        }

        public void setUrl(String url) {
            downloadInfo.setUrl(url);
        }

        public String getDir() {
            return downloadInfo.getDir();
        }

        public void setDir(String dir) {
            downloadInfo.setDir(dir);
        }

        public String getFilename() {
            return downloadInfo.getFilename();
        }

        public void setFilename(String filename) {
            downloadInfo.setFilename(filename);
        }

        public int getStatus() {
            return downloadInfo.getStatus();
        }

        public void setStatus(int status) {
            downloadInfo.setStatus(status);
        }

        public long getTotal() {
            return downloadInfo.getTotal();
        }

        public void setTotal(long total) {
            downloadInfo.setTotal(total);
        }

        public String getExtInfo() {
            return downloadInfo.getExtInfo();
        }

        public void setExtInfo(String extInfo) {
            downloadInfo.setExtInfo(extInfo);
        }

        public HttpInfo.Agency getHttpInfoAgency() {
            return httpInfoAgency;
        }

        public ErrorInfo.Agency getErrorInfoAgency() {
            return errorInfoAgency;
        }

        public long getCurrent() {
            return downloadInfo.getCurrent();
        }

        public void setCurrent(long current) {
            downloadInfo.setCurrent(current);
        }

        @NonNull
        public HttpInfo getHttpInfo() {
            return downloadInfo.getHttpInfo();
        }

        @NonNull
        public ErrorInfo getErrorInfo() {
            return downloadInfo.getErrorInfo();
        }

        public List<DownloadItem> getDownloadItems() {
            return downloadInfo.getDownloadItems();
        }

        public void setDownloadItems(List<DownloadItem> downloadItems) {
            downloadInfo.setDownloadItems(downloadItems);
        }
    }
}
