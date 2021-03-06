package com.shizhefei.download.entity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.shizhefei.download.base.DownloadTaskFactory;

import java.util.concurrent.Executor;

public class DownloadConfig {
    private String dir;
    private int blockSize;
    private boolean isWifiRequired;
    private DownloadTaskFactory downloadTaskFactory;
    private Executor executor;
    private String userAgent;
    private long minDownloadProgressTime;
    private boolean isDebug;

    public boolean isDebug() {
        return isDebug;
    }

    public long getMinDownloadProgressTime() {
        return minDownloadProgressTime;
    }

    @NonNull
    public String getUserAgent() {
        return userAgent;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public boolean isWifiRequired() {
        return isWifiRequired;
    }

    @NonNull
    public String getDir() {
        return dir;
    }

    @NonNull
    public DownloadTaskFactory getDownloadTaskFactory() {
        return downloadTaskFactory;
    }

    @NonNull
    public Executor getExecutor() {
        return executor;
    }

    public static final class Builder {
        private String dir;
        private int blockSize;
        private boolean isWifiRequired;
        private DownloadTaskFactory downloadTaskFactory;
        private Executor executor;
        private String userAgent;
        private long minDownloadProgressTime;
        private boolean isDebug;

        public Builder() {
        }

        public Builder(DownloadConfig config) {
            downloadTaskFactory = config.getDownloadTaskFactory();
            dir = config.getDir();
            executor = config.getExecutor();
            blockSize = config.getBlockSize();
            isWifiRequired = config.isWifiRequired();
            userAgent = config.getUserAgent();
            minDownloadProgressTime = config.getMinDownloadProgressTime();
            isDebug = config.isDebug;
        }

        public Builder setBlockSize(int blockSize) {
            this.blockSize = blockSize;
            return this;
        }

        public Builder setWifiRequired(boolean wifiRequired) {
            isWifiRequired = wifiRequired;
            return this;
        }

        public Builder setDir(String dir) {
            this.dir = dir;
            return this;
        }

        public Builder setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public boolean isDebug() {
            return isDebug;
        }

        public Builder setDebug(boolean debug) {
            isDebug = debug;
            return this;
        }

        public String getUserAgent() {
            return this.userAgent;
        }

        public Builder setDownloadTaskFactory(DownloadTaskFactory downloadTaskFactory) {
            this.downloadTaskFactory = downloadTaskFactory;
            return this;
        }

        public Builder setExecutor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public Builder setMinDownloadProgressTime(long minDownloadProgressTime) {
            this.minDownloadProgressTime = minDownloadProgressTime;
            return this;
        }

        public String getDir() {
            return dir;
        }

        public long getMinDownloadProgressTime() {
            return minDownloadProgressTime;
        }

        public DownloadTaskFactory getDownloadTaskFactory() {
            return downloadTaskFactory;
        }

        public Executor getExecutor() {
            return executor;
        }

        public DownloadConfig build() {
            DownloadConfig downloadConfig = new DownloadConfig();
            downloadConfig.downloadTaskFactory = this.downloadTaskFactory;
            downloadConfig.dir = this.dir;
            downloadConfig.executor = this.executor;
            downloadConfig.blockSize = this.blockSize;
            downloadConfig.isWifiRequired = this.isWifiRequired;
            downloadConfig.userAgent = this.userAgent;
            downloadConfig.minDownloadProgressTime = this.minDownloadProgressTime;
            downloadConfig.isDebug = isDebug;
            return downloadConfig;
        }
    }
}
