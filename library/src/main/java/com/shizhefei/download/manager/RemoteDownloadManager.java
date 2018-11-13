package com.shizhefei.download.manager;

import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.base.DownloadCursor;

public class RemoteDownloadManager extends DownloadManager {
    public RemoteDownloadManager(LocalDownloadManager localDownloadManager) {

    }

    @Override
    public long start(DownloadParams downloadParams, DownloadListener downloadListener) {
        return 0;
    }

    @Override
    public long start(DownloadParams downloadParams) {
        return 0;
    }

    @Override
    public long restartPauseOrFail(long downloadId, DownloadListener downloadListener) {
        return 0;
    }

    @Override
    public void pause(long downloadId) {

    }

    @Override
    public void remove(long downloadId) {

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
    public DownloadCursor getDownloadCursor() {
        return null;
    }

    @Override
    public void registerDownloadListener(DownloadListener downloadListener) {

    }

    @Override
    public void unregisterDownloadListener(DownloadListener downloadListener) {

    }
}
