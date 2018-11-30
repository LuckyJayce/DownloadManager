package com.shizhefei.download.manager;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.shizhefei.download.BuildConfig;
import com.shizhefei.download.base.SpeedMonitor;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.taskfactory.DefaultDownloadTaskFactory;
import com.shizhefei.download.entity.DownloadConfig;
import com.shizhefei.download.base.DownloadInfoList;
import com.shizhefei.download.utils.DownloadUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 建议在不要多线程调用
 */
public abstract class DownloadManager {
    public static final String LIB_NAME = "DownloadManager";
    //下载进度被重置原因
    public static final int DOWNLOAD_RESET_SCHEDULE_REASON_FILE_REMOVE = 0;//文件被移除
    public static final int DOWNLOAD_RESET_SCHEDULE_REASON_UNSUPPORT_RANGE = 1;//url的服务器不接受range要重新下载
    public static final int DOWNLOAD_RESET_SCHEDULE_REASON_ETAG_CHANGE = 2;//etag改变，表示内容变化

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
    /**
     * m3u8文件解析失败
     */
    public static final int ERROR_M3U8_FILE_PARSE_FAIL = 10;
    //---------------------     error code---------------------------------------------------------//


    //----------------------   status ------------------------------------------------------------//
    public static final int STATUS_PENDING = 1;//在队列中，还没开始
    public static final int STATUS_START = 1 << 1;//开始
    public static final int STATUS_CONNECTED = 1 << 2;//连接上服务器，可能会出现多次
    public static final int STATUS_DOWNLOAD_RESET_SCHEDULE = 1 << 3;//下载进度被重置， 可能会出现多次
    public static final int STATUS_PROGRESS = 1 << 4;//下载中 更新进度
    public static final int STATUS_PAUSED = 1 << 5;//执行了停止，可以start继续下载
    public static final int STATUS_FINISHED = 1 << 6;//下载完成
    public static final int STATUS_ERROR = 1 << 7;//下载失败出现异常
    //----------------------   status ------------------------------------------------------------//

    // ----------------------------------- database ---------------------------------------------//
    public static final String TABLE_NAME = "download";
    public static final String FIELD_DOWNLOAD_PARAMS = "download_params";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_DIR = "dir";
    public static final String FIELD_FILENAME = "filename";
    public static final String FIELD_TEMP_FILENAME = "tempFileName";
    public static final String FIELD_HTTP_INFO = "http_info";
    public static final String FIELD_ERROR_INFO = "error_info";
    public static final String FIELD_EXT_INFO = "ext_info";
    public static final String FIELD_URL = "url";
    public static final String FIELD_START_TIME = "start_time";
    public static final String FIELD_CURRENT = "current";
    public static final String FIELD_TOTAL = "total";
    public static final String FIELD_KEY = "id";
    public static final String FIELD_DOWNLOAD_TASK_NAME = "downloadTaskName";
    public static final String FIELD_ESTIMATE_TOTAL = "estimateTotal";
    // ----------------------------------- database ---------------------------------------------//

    public static final int INVALID_POSITION = -1;

    private static Context context;
    private static volatile LocalDownloadManager localDownloadManager;
    private static volatile RemoteDownloadManager remoteDownloadManager;
    private static DownloadConfig downloadConfig;
    private static boolean isInit;

    // 自定义一个注解MyState
    @IntDef({STATUS_PENDING, STATUS_START, STATUS_CONNECTED, STATUS_DOWNLOAD_RESET_SCHEDULE, STATUS_PROGRESS, STATUS_PAUSED, STATUS_FINISHED, STATUS_ERROR})
    public @interface Status {
    }

    // 自定义一个注解MyState
    @IntDef({ERROR_UNKNOW, ERROR_PERMISSION, ERROR_FILENOTFOUNDEXCEPTION, ERROR_IOEXCEPTION, ERROR_HTTP, ERROR_WIFIREQUIRED, ERROR_EMPTY_SIZE, ERROR_MALFORMEDURLEXCEPTION, ERROR_PROTOCOLEXCEPTION, ERROR_SIZE_CHANGE, ERROR_M3U8_FILE_PARSE_FAIL})
    public @interface Error {
    }

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
            builder.setExecutor(createDefaultExecutor());
        }
        if (TextUtils.isEmpty(builder.getUserAgent())) {
            builder.setUserAgent(defaultUserAgent());
        }
        if (builder.getMinDownloadProgressTime() <= 0) {
            builder.setMinDownloadProgressTime(100);
        }
        downloadConfig = builder.build();
        DownloadManager.context = context.getApplicationContext();
        isInit = true;
    }

    private static Executor createDefaultExecutor() {
        BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(128);
        ThreadFactory sThreadFactory = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                return new Thread(r, "DownloadManager Task #" + mCount.getAndIncrement());
            }
        };
        return new ThreadPoolExecutor(1, 2, 30, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);
    }

    @NonNull
    public static String defaultUserAgent() {
        return DownloadUtils.formatString("%s/%s", LIB_NAME, BuildConfig.VERSION_NAME);
    }

    @Nullable
    public abstract DownloadInfo findFirst(String url);

    @NonNull
    public abstract List<DownloadInfo> find(String url);

    @Nullable
    public abstract DownloadInfo findFirst(String url, String dir, String fileName);

    @NonNull
    public static DownloadConfig getDownloadConfig() {
        if (!isInit) {
            throw new RuntimeException("请先调用init进行初始化");
        }
        return downloadConfig;
    }

    @NonNull
    public static Context getApplicationContext() {
        return context;
    }

    @NonNull
    public static LocalDownloadManager getLocal() {
        if (!isInit) {
            throw new RuntimeException("请先调用init进行初始化");
        }
        if (localDownloadManager == null) {
            synchronized (DownloadManager.class) {
                if (localDownloadManager == null) {
                    localDownloadManager = new LocalDownloadManager(context, downloadConfig.getDownloadTaskFactory(), downloadConfig.getExecutor());
                }
            }
        }
        return localDownloadManager;
    }

    @NonNull
    public static RemoteDownloadManager getRemote() {
        if (!isInit) {
            throw new RuntimeException("请先调用init进行初始化");
        }
        if (remoteDownloadManager == null) {
            synchronized (DownloadManager.class) {
                if (remoteDownloadManager == null) {
                    remoteDownloadManager = new RemoteDownloadManager();
                }
            }
        }
        return remoteDownloadManager;
    }

    /**
     * 开始下载
     *
     * @param downloadParams
     * @return 返回下载的id
     */
    public abstract long start(DownloadParams downloadParams);

    /**
     * 暂停下载任务
     *
     * @param downloadId
     */
    public abstract void pause(long downloadId);

    /**
     * 暂停所有下载任务
     */
    public abstract void pauseAll();

    /**
     * 执行之前失败,停止且现在未在执行中的任务
     * 如果已经在执行则不会重新执行
     *
     * @param downloadId
     * @return
     */
    public abstract boolean resume(long downloadId);

    /**
     * 恢复所有下载
     */
    public abstract void resumeAll();

    /**
     * 停止并删除下载任务
     *
     * @param downloadId
     */
    public abstract void remove(long downloadId);

    /**
     * 获取下载信息
     *
     * @param downloadId
     * @return
     */
    public abstract DownloadInfo getDownloadInfo(long downloadId);

    /**
     * 获取下载参数
     *
     * @param downloadId
     * @return
     */
    public abstract DownloadParams getDownloadParams(long downloadId);

    /**
     * @return
     */
    public abstract SpeedMonitor getSpeedMonitor();

    /**
     * 创建下载列表信息查询者
     *
     * @return
     */
    public abstract DownloadInfoList createDownloadInfoList();

    /**
     * 创建下载列表信息查询者，只返回符合downloadStatus状态的数据列表
     *
     * @param downloadStatus 下载的状态 多种状态
     * @return
     */
    public abstract DownloadInfoList createDownloadInfoList(final int downloadStatus);

    /**
     * 注册下载监听
     *
     * @param downloadListener
     */
    public abstract void registerDownloadListener(DownloadListener downloadListener);

    /**
     * 取消注册下载监听
     *
     * @param downloadListener
     */
    public abstract void unregisterDownloadListener(DownloadListener downloadListener);

    @NonNull
    public static String getStatusText(@Status int status) {
        switch (status) {
            case STATUS_PENDING:
                return "STATUS_PENDING";
            case STATUS_START:
                return "STATUS_START";
            case STATUS_CONNECTED:
                return "STATUS_CONNECTED";
            case STATUS_DOWNLOAD_RESET_SCHEDULE:
                return "STATUS_DOWNLOAD_RESET_SCHEDULE";
            case STATUS_PROGRESS:
                return "STATUS_PROGRESS";
            case STATUS_PAUSED:
                return "STATUS_PAUSED";
            case STATUS_FINISHED:
                return "STATUS_FINISHED";
            case STATUS_ERROR:
                return "STATUS_ERROR";
        }
        return "UNKNOWN";
    }
}
