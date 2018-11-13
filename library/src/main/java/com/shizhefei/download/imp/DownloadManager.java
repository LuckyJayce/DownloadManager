package com.shizhefei.download.imp;

import android.content.Context;

import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.base.DownloadParams;
import com.shizhefei.download.base.DownloadTaskFactory;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.base.IdGenerator;

public abstract class DownloadManager {
    private static Context context;
//    private static DownloadTaskFactory staticDownloadTaskFactory;
//    private static IdGenerator staticIdGenerator;
//    private static DownloadDB downloadDB;
    private static volatile LocalDownloadManager localDownloadManager;
    private static volatile RemoteDownloadManager remoteDownloadManager;

    public static void init(Context context) {
        DownloadManager.context = context.getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

    public static LocalDownloadManager getLocal() {
        if (localDownloadManager == null) {
            synchronized (DownloadManager.class) {
                if (localDownloadManager == null) {
                    localDownloadManager = new LocalDownloadManager(context, null);
                }
            }
        }
        return localDownloadManager;
    }

    public static RemoteDownloadManager getRemote() {
        if (remoteDownloadManager == null) {
            synchronized (DownloadManager.class) {
                if (remoteDownloadManager == null) {
                    remoteDownloadManager = new RemoteDownloadManager(localDownloadManager);
                }
            }
        }
        return remoteDownloadManager;
    }

    /**
     * @param downloadParams
     * @param downloadListener
     * @return 返回下载的id
     */
    public abstract long start(DownloadParams downloadParams, DownloadListener downloadListener)
    ;

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

    public abstract DownloadInfo getDownloadEntity(long id);

    public abstract DownloadParams getDownloadParams(long id);

    public abstract DownloadInfo get(int position);

    public abstract int getTaskCount();

    public abstract void registerDownloadListener(DownloadListener downloadListener);

    public abstract void unregisterDownloadListener(DownloadListener downloadListener);
}
