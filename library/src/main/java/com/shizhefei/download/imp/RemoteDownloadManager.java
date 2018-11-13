package com.shizhefei.download.imp;

import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.base.DownloadParams;

public class RemoteDownloadManager extends DownloadManager {
    @Override
    public long start(DownloadParams downloadParams, DownloadListener downloadListener) {
        return 0;
    }

    @Override
    public long start(DownloadParams downloadParams) {
        return 0;
    }

    @Override
    public void pause(long downloadId) {

    }

    @Override
    public void cancel(long downloadId) {

    }

    @Override
    public DownloadInfo getDownloadEntity(long id) {
        return null;
    }

    @Override
    public DownloadParams getDownloadParams(long id) {
        return null;
    }

    @Override
    public DownloadInfo get(int position) {
        return null;
    }

    @Override
    public int getTaskCount() {
        return 0;
    }

    @Override
    public void registerDownloadListener(DownloadListener downloadListener) {

    }

    @Override
    public void unregisterDownloadListener(DownloadListener downloadListener) {

    }
}
