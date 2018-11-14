package com.shizhefei.download.manager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.base.DownloadCursor;

import java.util.List;

public class RemoteDownloadManager extends DownloadManager {
    public RemoteDownloadManager(LocalDownloadManager localDownloadManager) {

    }

    @Nullable
    @Override
    public DownloadInfo findFirst(String url) {
        return null;
    }

    @NonNull
    @Override
    public List<DownloadInfo> find(String url) {
        return null;
    }

    @Nullable
    @Override
    public DownloadInfo findFirst(String url, String dir, String fileName) {
        return null;
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
    public boolean restartPauseOrFail(long downloadId, DownloadListener downloadListener) {
        return false;
    }

    @Override
    public void pause(long downloadId) {

    }

    @Override
    public void pauseAll() {

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
