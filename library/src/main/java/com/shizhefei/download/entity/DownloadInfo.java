package com.shizhefei.download.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.shizhefei.download.manager.DownloadManager;

public class DownloadInfo implements Parcelable {
    private long id;
    private String url;
    private String dir;
    private String filename;
    private String tempFileName;
    private long startTime;//单位 毫秒
    @DownloadManager.Status
    private int status = DownloadManager.STATUS_PENDING;
    private long total;
    //估算的总大小
    private long estimateTotal;
    private long current;
    private final DownloadParams downloadParams;
    private final HttpInfo httpInfo;
    private final ErrorInfo errorInfo;
    private String extInfo;
    private String downloadTaskName;
//    //当线程下载为null，多线程下载 DownloadEntity对应多个DownloadItem
//    private List<DownloadBlockInfo> downloadItems;

    private DownloadInfo(@NonNull DownloadParams downloadParams, @NonNull HttpInfo httpInfo, @NonNull ErrorInfo errorInfo) {
        this.downloadParams = downloadParams;
        this.httpInfo = httpInfo;
        this.errorInfo = errorInfo;
    }

    protected DownloadInfo(Parcel in) {
        id = in.readLong();
        url = in.readString();
        dir = in.readString();
        filename = in.readString();
        tempFileName = in.readString();
        startTime = in.readLong();
        status = in.readInt();
        total = in.readLong();
        current = in.readLong();
        downloadParams = in.readParcelable(DownloadParams.class.getClassLoader());
        httpInfo = in.readParcelable(HttpInfo.class.getClassLoader());
        errorInfo = in.readParcelable(ErrorInfo.class.getClassLoader());
        extInfo = in.readString();
        downloadTaskName = in.readString();
        estimateTotal = in.readLong();
    }

    public static final Creator<DownloadInfo> CREATOR = new Creator<DownloadInfo>() {
        @Override
        public DownloadInfo createFromParcel(Parcel in) {
            return new DownloadInfo(in);
        }

        @Override
        public DownloadInfo[] newArray(int size) {
            return new DownloadInfo[size];
        }
    };

    public long getEstimateTotal() {
        return estimateTotal;
    }

    void setEstimateTotal(long estimateTotal) {
        this.estimateTotal = estimateTotal;
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
    public String getFileName() {
        return filename;
    }

    private void setFilename(String filename) {
        this.filename = filename;
    }

    @DownloadManager.Status
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

    public String getDownloadTaskName() {
        return downloadTaskName;
    }

    void setDownloadTaskName(String downloadTaskName) {
        this.downloadTaskName = downloadTaskName;
    }

    //    public List<DownloadBlockInfo> getDownloadItems() {
//        return downloadItems;
//    }
//
//    private void setDownloadItems(List<DownloadBlockInfo> downloadItems) {
//        this.downloadItems = downloadItems;
//    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(url);
        dest.writeString(dir);
        dest.writeString(filename);
        dest.writeString(tempFileName);
        dest.writeLong(startTime);
        dest.writeInt(status);
        dest.writeLong(total);
        dest.writeLong(current);
        dest.writeParcelable(downloadParams, flags);
        dest.writeParcelable(httpInfo, flags);
        dest.writeParcelable(errorInfo, flags);
        dest.writeString(extInfo);
        dest.writeString(downloadTaskName);
        dest.writeLong(estimateTotal);
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", dir='" + dir + '\'' +
                ", filename='" + filename + '\'' +
                ", tempFileName='" + tempFileName + '\'' +
                ", startTime=" + startTime +
                ", status=" + status +
                ", total=" + total +
                ", estimateTotal=" + estimateTotal +
                ", current=" + current +
                ", downloadParams=" + downloadParams +
                ", httpInfo=" + httpInfo +
                ", errorInfo=" + errorInfo +
                ", extInfo='" + extInfo + '\'' +
                ", downloadTaskName='" + downloadTaskName + '\'' +
                '}';
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

        public long getEstimateTotal() {
            return downloadInfo.getEstimateTotal();
        }

        public void setEstimateTotal(long estimateTotal) {
            downloadInfo.setEstimateTotal(estimateTotal);
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

        public void setDownloadTaskName(String downloadTaskName) {
            downloadInfo.setDownloadTaskName(downloadTaskName);
        }

        public String getDownloadTaskName() {
            return downloadInfo.getDownloadTaskName();
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
            return downloadInfo.getFileName();
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

        @Override
        public String toString() {
            return downloadInfo.toString();
        }

        //        public List<DownloadBlockInfo> getDownloadItems() {
//            return downloadInfo.getDownloadItems();
//        }
//
//        public void setDownloadItems(List<DownloadBlockInfo> downloadItems) {
//            downloadInfo.setDownloadItems(downloadItems);
//        }
    }
}
