package com.shizhefei.download.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import com.shizhefei.download.BuildConfig;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.taskfactory.DefaultDownloadTaskFactory;
import com.shizhefei.download.entity.DownloadConfig;
import com.shizhefei.download.base.DownloadCursor;
import com.shizhefei.download.utils.DownloadUtils;

import java.io.File;

public abstract class DownloadManager {
    public static final int DOWNLOAD_FROM_BEGIN_REASON_FILE_REMOVE = 0;//文件被移除
    public static final int DOWNLOAD_FROM_BEGIN_UNSUPPORT_RANGE = 1;//url的服务器不接受range要重新下载
    public static final int DOWNLOAD_FROM_BEGIN_ETAG_CHANGE = 2;//etag改变，表示内容变化

    //---------------------     error code  -------------------------------------------------------------//
    /**
     * 未知错误
     */
    public static final int ERROR_UNKNOW = -1;
    /**
     * 权限不足错误码
     */
    public static final int ERROR_PERMISSION = 1;
    /**
     * FileNotFoundException 文件找不到错误码（一般没有存储权限）
     */
    public static final int ERROR_FILENOTFOUNDEXCEPTION = 2;
    /**
     * IOException
     */
    public static final int ERROR_IOEXCEPTION = 3;
    /**
     * http错误
     */
    public static final int ERROR_HTTP = 4;
    /**
     * http错误
     */
    public static final int ERROR_WIFIREQUIRED = 5;
    public static final int ERROR_EMPTY_SIZE = 6;
    /**
     * MalformedURLException
     */
    public static final int ERROR_MALFORMEDURLEXCEPTION = 7;
    /**
     * ProtocolException
     */
    public static final int ERROR_PROTOCOLEXCEPTION = 8;
    public static final int ERROR_SIZE_CHANGE = 9;
    //---------------------     error code---------------------------------------------------------//


    //----------------------   status ------------------------------------------------------------//
    public static final int STATUS_PENDING = 0;//在队列中，还没开始
    public static final int STATUS_START = 1;//开始
    public static final int STATUS_CONNECTED = 2;//连接上服务器
    public static final int STATUS_DOWNLOAD_RESET_BEGIN = 3;//连接上服务器
    public static final int STATUS_DOWNLOAD_ING = 4;
    public static final int STATUS_PAUSED = 5;//连接上服务器
    public static final int STATUS_FINISHED = 6;
    public static final int STATUS_FAIL = 7;
    public static final int STATUS_REMOVE = 8;//连接上服务器
    //----------------------   status ------------------------------------------------------------//

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
        return DownloadUtils.formatString("DownloadManager/%s", BuildConfig.VERSION_NAME);
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

    public static String getStatusText(int status) {
        switch (status) {
            case STATUS_PENDING:
                return "STATUS_PENDING";
            case STATUS_START:
                return "STATUS_START";
            case STATUS_CONNECTED:
                return "STATUS_CONNECTED";
            case STATUS_DOWNLOAD_RESET_BEGIN:
                return "STATUS_DOWNLOAD_RESET_BEGIN";
            case STATUS_DOWNLOAD_ING:
                return "STATUS_DOWNLOAD_ING";
            case STATUS_PAUSED:
                return "STATUS_PAUSED";
            case STATUS_FINISHED:
                return "STATUS_FINISHED";
            case STATUS_FAIL:
                return "STATUS_FAIL";
        }
        return "UNKNOWN";
    }
}
