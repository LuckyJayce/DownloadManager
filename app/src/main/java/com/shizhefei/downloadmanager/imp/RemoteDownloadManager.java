package com.shizhefei.downloadmanager.imp;

import com.shizhefei.downloadmanager.base.DownloadEntity;
import com.shizhefei.downloadmanager.base.DownloadListener;
import com.shizhefei.downloadmanager.base.DownloadParams;

public class RemoteDownloadManager extends DownloadManager {
    @Override
    public long start(DownloadParams downloadParams, DownloadListener downloadListener) {
        return 0;
    }

    @Override
    public void pause(long downloadId) {

    }

    @Override
    public void cancel(long downloadId) {

    }

    @Override
    public DownloadEntity getDownloadEntity(int id) {
        return null;
    }

    @Override
    public DownloadParams getDownloadParams(int id) {
        return null;
    }

    @Override
    public DownloadEntity get(int position) {
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
