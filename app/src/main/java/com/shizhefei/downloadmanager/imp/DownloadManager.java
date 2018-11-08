package com.shizhefei.downloadmanager.imp;

import com.shizhefei.downloadmanager.base.DownloadEntity;
import com.shizhefei.downloadmanager.base.DownloadListener;
import com.shizhefei.downloadmanager.base.DownloadParams;
import com.shizhefei.downloadmanager.base.DownloadTaskFactory;
import com.shizhefei.downloadmanager.base.IdGenerator;

public abstract class DownloadManager {
    private static DownloadTaskFactory staticDownloadTaskFactory;
    private static IdGenerator staticIdGenerator;

    public static void init(DownloadTaskFactory downloadTaskFactory, IdGenerator idGenerator) {
        staticDownloadTaskFactory = downloadTaskFactory;
        staticIdGenerator = idGenerator;
    }

    public static DownloadManager getLocal() {
        return new LocalDownloadManager(staticDownloadTaskFactory, staticIdGenerator);
    }

    public static DownloadManager getRemote() {
        return new RemoteDownloadManager();
    }

    /**
     * @param downloadParams
     * @param downloadListener
     * @return 返回下载的id
     */
    public abstract long start(DownloadParams downloadParams, DownloadListener downloadListener);

    /**
     * 停止下载任务
     *
     * @param downloadId
     */
    public abstract void pause(long downloadId);

    /**
     * 停止并删除下载任务
     *
     * @param downloadId
     */
    public abstract void cancel(long downloadId);

    public abstract DownloadEntity getDownloadEntity(int id);

    public abstract DownloadParams getDownloadParams(int id);

    public abstract DownloadEntity get(int position);

    public abstract int getTaskCount();

    public abstract void registerDownloadListener(DownloadListener downloadListener);

    public abstract void unregisterDownloadListener(DownloadListener downloadListener);
}
