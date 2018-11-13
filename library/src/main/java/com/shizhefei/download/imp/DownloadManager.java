package com.shizhefei.download.imp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import com.shizhefei.download.BuildConfig;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.base.DownloadParams;
import com.shizhefei.download.base.DownloadTaskFactory;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.base.IdGenerator;
import com.shizhefei.download.utils.DownloadJsonUtils;
import com.shizhefei.download.utils.DownloadLogUtils;
import com.shizhefei.download.utils.FileDownloadUtils;

import java.io.File;

public abstract class DownloadManager {
    public static final int DOWNLOAD_FROMBEGIN_REASON_FILE_REMOVE = 0;//文件被移除
    public static final int DOWNLOAD_FROMBEGIN_UNSUPPORT_RANGE = 1;//url的服务器不接受range要重新下载
    public static final int DOWNLOAD_FROMBEGIN_ETAG_CHANGE = 2;//etag改变，表示内容变化

    private static Context context;
    //    private static DownloadTaskFactory staticDownloadTaskFactory;
//    private static IdGenerator staticIdGenerator;
//    private static DownloadDB downloadDB;
    private static volatile LocalDownloadManager localDownloadManager;
    private static volatile RemoteDownloadManager remoteDownloadManager;
    private static DownloadConfig downloadConfig;

    public static void init(Context context, DownloadConfig config) {
        DownloadConfig.Builder builder = new DownloadConfig.Builder(config);
        if (TextUtils.isEmpty(builder.getDir())) {
            String dir = Environment.getExternalStorageDirectory() + File.separator + context.getPackageName() + File.separator + "download";
            builder.setDir(dir);
        }
        if (builder.getDownloadTaskFactory() == null) {
            builder.setDownloadTaskFactory(new DefaultDownloadTaskFactory());
        }
        if (builder.getExecutor() == null) {
            builder.setExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        if (TextUtils.isEmpty(builder.getUserAgent())) {
            builder.setUserAgent(defaultUserAgent());
        }
        downloadConfig = builder.build();
        DownloadManager.context = context.getApplicationContext();
    }

    public static String defaultUserAgent() {
        return FileDownloadUtils.formatString("DownloadManager/%s", BuildConfig.VERSION_NAME);
    }

    public static DownloadConfig getDownloadConfig() {
        return downloadConfig;
    }

    public static Context getApplicationContext() {
        return context;
    }

    public static LocalDownloadManager getLocal() {
        if (localDownloadManager == null) {
            synchronized (DownloadManager.class) {
                if (localDownloadManager == null) {
                    localDownloadManager = new LocalDownloadManager(context, downloadConfig.getDownloadTaskFactory(), downloadConfig.getExecutor());
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
     * 执行之前失败,停止且现在未在执行中的任务
     * 如果已经在执行则不会重新执行
     *
     * @param downloadId
     * @return
     */
    public abstract long restartPauseOrFail(long downloadId, DownloadListener downloadListener);

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
    public abstract void remove(long downloadId);

    public abstract DownloadInfo getDownloadEntity(long downloadId);

    public abstract DownloadParams getDownloadParams(long downloadId);

    public abstract DownloadCursor getDownloadCursor();

    public abstract void registerDownloadListener(DownloadListener downloadListener);

    public abstract void unregisterDownloadListener(DownloadListener downloadListener);
}
