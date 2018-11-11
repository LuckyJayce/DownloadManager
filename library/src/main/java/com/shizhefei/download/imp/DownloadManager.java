package com.shizhefei.download.imp;

import android.content.Context;

import com.shizhefei.download.base.DownloadEntity;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.base.DownloadParams;
import com.shizhefei.download.base.DownloadTaskFactory;
import com.shizhefei.download.base.IDownloadDB;
import com.shizhefei.download.base.IdGenerator;

public abstract class DownloadManager {
    private static Context context;
    private static DownloadTaskFactory staticDownloadTaskFactory;
    private static IdGenerator staticIdGenerator;
    private static IDownloadDB downloadDB;

    public static void init(Context context, DownloadTaskFactory downloadTaskFactory, IdGenerator idGenerator) {
        DownloadManager.context = context.getApplicationContext();
        staticDownloadTaskFactory = downloadTaskFactory;
        staticIdGenerator = idGenerator;
    }

    public static Context getContext(){
        return context;
    }

    public static LocalDownloadManager getLocal() {
        return new LocalDownloadManager(staticDownloadTaskFactory, staticIdGenerator);
    }

    public static RemoteDownloadManager getRemote() {
        return new RemoteDownloadManager();
    }

    /**
     * @param downloadParams
     * @param downloadListener
     * @return 返回下载的id
     */
    public abstract long start(DownloadParams downloadParams, DownloadListener downloadListener);

    public abstract long start(DownloadParams downloadParams);

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

    public abstract DownloadEntity getDownloadEntity(long id);

    public abstract DownloadParams getDownloadParams(long id);

    public abstract DownloadEntity get(int position);

    public abstract int getTaskCount();

    public abstract void registerDownloadListener(DownloadListener downloadListener);

    public abstract void unregisterDownloadListener(DownloadListener downloadListener);
}
