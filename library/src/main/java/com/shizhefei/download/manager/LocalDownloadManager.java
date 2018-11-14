package com.shizhefei.download.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Process;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;

import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.base.RemoveHandler;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.base.DownloadTaskFactory;
import com.shizhefei.download.base.IdGenerator;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.idgenerator.DefaultIdGenerator;
import com.shizhefei.download.taskfactory.DefaultDownloadTaskFactory;
import com.shizhefei.download.base.DownloadCursor;
import com.shizhefei.download.task.DownloadListenerProxy;
import com.shizhefei.download.utils.DownloadUtils;
import com.shizhefei.mvc.RequestHandle;
import com.shizhefei.task.TaskHandle;
import com.shizhefei.task.TaskHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

public class LocalDownloadManager extends DownloadManager {
    private final DownloadDB downloadDB;
    private DownloadTaskFactory downloadTaskFactory;
    private IdGenerator idGenerator;
    private TaskHelper taskHelper = new TaskHelper();
    private LongSparseArray<DownloadData> tasks = new LongSparseArray<>();
    private LongSparseArray<DownloadListener> listeners = new LongSparseArray<>();
    private Set<DownloadListener> downloadListeners = new HashSet<>();
    private List<DownloadInfo> downloadInfoList;
    private Executor executor;

    public LocalDownloadManager(Context context, DownloadTaskFactory downloadTaskFactory, Executor executor) {
        this.downloadDB = new DownloadDB(context, AsyncTask.SERIAL_EXECUTOR);
        if (downloadTaskFactory == null) {
            this.downloadTaskFactory = new DefaultDownloadTaskFactory();
        } else {
            this.downloadTaskFactory = downloadTaskFactory;
        }
        this.executor = executor;
        this.idGenerator = new DefaultIdGenerator(context);
        List<DownloadInfo.Agency> infoList = downloadDB.findAll();
        downloadInfoList = new ArrayList<>(infoList.size());
        for (DownloadInfo.Agency info : infoList) {
            switch (info.getStatus()) {
                case DownloadManager.STATUS_CONNECTED:
                case DownloadManager.STATUS_DOWNLOAD_ING:
                case DownloadManager.STATUS_PENDING:
                case DownloadManager.STATUS_START:
                case DownloadManager.STATUS_DOWNLOAD_RESET_BEGIN:
                    info.setStatus(DownloadManager.STATUS_PAUSED);
                    break;
            }
            if (!info.getHttpInfo().isAcceptRange()) {
                info.setCurrent(0);
            }
            downloadInfoList.add(info.getInfo());
        }
    }

    @Override
    public long start(DownloadParams downloadParams) {
        return start(downloadParams, null);
    }

    @Override
    public boolean restartPauseOrFail(long downloadId, DownloadListener downloadListener) {
        int index = -1;
        DownloadInfo downloadInfo = null;
        for (int i = 0; i < downloadInfoList.size(); i++) {
            DownloadInfo info = downloadInfoList.get(i);
            if (info.getId() == downloadId) {
                downloadInfo = info;
                index = i;
                break;
            }
        }
        if (downloadInfo != null) {
            int status = downloadInfo.getStatus();
            if (status == DownloadManager.STATUS_PAUSED || status == DownloadManager.STATUS_ERROR) {
                DownloadParams downloadParams = downloadInfo.getDownloadParams();
                if (downloadListener != null) {
                    listeners.put(downloadId, downloadListener);
                }

                RemoveHandler removeHandler = new RemoveHandlerImp();
                AbsDownloadTask downloadTask = downloadTaskFactory.buildDownloadTask(downloadId, downloadParams, downloadDB, removeHandler, executor);
                downloadInfoList.set(index, downloadTask.getDownloadInfo());

                TaskHandle taskHandle = taskHelper.execute(downloadTask, new DownloadListenerProxy(downloadId, proxyDownloadListener));
                tasks.put(downloadId, new DownloadData(taskHandle, downloadTask, removeHandler));
                return true;
            }
        }
        return false;
    }

    @NonNull
    @Override
    public List<DownloadInfo> find(String url) {
        List<DownloadInfo> list = new ArrayList<>(2);
        for (DownloadInfo downloadInfo : downloadInfoList) {
            if (downloadInfo.getDownloadParams().getUrl().equals(url)) {
                list.add(downloadInfo);
            }
        }
        return list;
    }

    @Override
    public DownloadInfo findFirst(String url) {
        for (DownloadInfo downloadInfo : downloadInfoList) {
            if (downloadInfo.getDownloadParams().getUrl().equals(url)) {
                return downloadInfo;
            }
        }
        return null;
    }

    @Override
    public DownloadInfo findFirst(String url, String dir, String fileName) {
        for (DownloadInfo downloadInfo : downloadInfoList) {
            DownloadParams downloadParams = downloadInfo.getDownloadParams();
            if (downloadParams.getUrl().equals(url) &&
                    downloadParams.getDir().equals(dir)
                    && (downloadParams.getFileName() != null && downloadParams.getFileName().equals(fileName))) {
                return downloadInfo;
            }
        }
        return null;
    }

    @Override
    public long start(DownloadParams downloadParams, DownloadListener downloadListener) {
        long downloadId = idGenerator.generateId(downloadParams);
        if (downloadListener != null) {
            listeners.put(downloadId, downloadListener);
        }

        RemoveHandler removeHandler = new RemoveHandlerImp();
        AbsDownloadTask downloadTask = downloadTaskFactory.buildDownloadTask(downloadId, downloadParams, downloadDB, removeHandler, executor);
        downloadInfoList.add(downloadTask.getDownloadInfo());

        TaskHandle taskHandle = taskHelper.execute(downloadTask, new DownloadListenerProxy(downloadId, proxyDownloadListener));
        tasks.put(downloadId, new DownloadData(taskHandle, downloadTask, removeHandler));
        return downloadId;
    }

    @Override
    public void pause(long downloadId) {
        DownloadData data = tasks.get(downloadId);
        if (data != null) {
            data.requestHandle.cancle();
            tasks.remove(downloadId);
        }
    }

    @Override
    public void pauseAll() {
        LongSparseArray<DownloadData> clone = tasks.clone();
        for (int i = 0; i < clone.size(); i++) {
            DownloadData data = clone.valueAt(i);
            data.requestHandle.cancle();
        }
        tasks.clear();
    }

    @Override
    public void remove(long downloadId) {
        DownloadInfo downloadInfo = null;
        Iterator<DownloadInfo> iterator = downloadInfoList.iterator();
        while (iterator.hasNext()) {
            DownloadInfo info = iterator.next();
            if (info.getId() == downloadId) {
                downloadInfo = info;
                iterator.remove();
                break;
            }
        }

        DownloadData data = tasks.get(downloadId);
        if (data != null) {
            data.removeHandler.remove();
            tasks.remove(downloadId);
            downloadDB.delete(downloadId);
        } else {//没有在执行，直接删除数据库中的
            if (downloadInfo != null) {
                if (!TextUtils.isEmpty(downloadInfo.getTempFileName())) {
                    File file = new File(downloadInfo.getDir(), downloadInfo.getTempFileName());
                    if (file.exists()) {
                        file.delete();
                    }
                }
                //下载好的 downloadInfo.getFileName() 没做删除处理
            }
            downloadDB.delete(downloadId);
        }
        //通知移除
        proxyDownloadListener.onRemove(downloadId);
    }

    @Override
    public DownloadInfo getDownloadEntity(long downloadId) {
        for (DownloadInfo downloadInfo : downloadInfoList) {
            if (downloadInfo.getId() == downloadId) {
                return downloadInfo;
            }
        }
        return null;
    }

    @Override
    public DownloadParams getDownloadParams(long downloadId) {
        for (DownloadInfo downloadInfo : downloadInfoList) {
            if (downloadInfo.getId() == downloadId) {
                return downloadInfo.getDownloadParams();
            }
        }
        return null;
    }

    @Override
    public DownloadCursor getDownloadCursor() {
        return downloadCursor;
    }

    @Override
    public void registerDownloadListener(DownloadListener downloadListener) {
        downloadListeners.add(downloadListener);
        DownloadUtils.logD("registerDownloadListener :" + downloadListener + " pid:" + Process.myPid());
    }

    @Override
    public void unregisterDownloadListener(DownloadListener downloadListener) {
        downloadListeners.remove(downloadListener);
    }

    private static class DownloadData {
        private RequestHandle requestHandle;
        private AbsDownloadTask task;
        private RemoveHandler removeHandler;

        public DownloadData(RequestHandle requestHandle, AbsDownloadTask task, RemoveHandler removeHandler) {
            this.requestHandle = requestHandle;
            this.task = task;
            this.removeHandler = removeHandler;
        }
    }

    private static class RemoveHandlerImp implements RemoveHandler {
        private List<OnRemoveListener> list = new ArrayList<>();

        @Override
        public void addRemoveListener(OnRemoveListener onRemoveListener) {
            list.add(onRemoveListener);
        }

        @Override
        public void remove() {
            for (OnRemoveListener onRemoveListener : list) {
                onRemoveListener.onRemove();
            }
        }
    }

    private DownloadListener proxyDownloadListener = new DownloadListener() {

        @Override
        public void onPending(long downloadId) {
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onPending(downloadId);
            }
            DownloadListener downloadListener = listeners.get(downloadId);
            if (downloadListener != null) {
                downloadListener.onPending(downloadId);
            }
        }

        @Override
        public void onStart(long downloadId) {
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onStart(downloadId);
            }
            DownloadListener downloadListener = listeners.get(downloadId);
            if (downloadListener != null) {
                downloadListener.onStart(downloadId);
            }
        }

        @Override
        public void onDownloadIng(long downloadId, long current, long total) {
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onDownloadIng(downloadId, current, total);
            }
            DownloadListener downloadListener = listeners.get(downloadId);
            if (downloadListener != null) {
                downloadListener.onDownloadIng(downloadId, current, total);
            }
        }

        @Override
        public void onDownloadResetBegin(long downloadId, int reason) {
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onDownloadResetBegin(downloadId, reason);
            }
            DownloadListener downloadListener = listeners.get(downloadId);
            if (downloadListener != null) {
                downloadListener.onDownloadResetBegin(downloadId, reason);
            }
        }

        @Override
        public void onConnected(long downloadId, HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName) {
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onConnected(downloadId, httpInfo, saveDir, saveFileName, tempFileName);
            }
            DownloadListener downloadListener = listeners.get(downloadId);
            if (downloadListener != null) {
                downloadListener.onConnected(downloadId, httpInfo, saveDir, saveFileName, tempFileName);
            }
        }

        @Override
        public void onPaused(long downloadId) {
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onPaused(downloadId);
            }
            DownloadListener downloadListener = listeners.get(downloadId);
            if (downloadListener != null) {
                downloadListener.onPaused(downloadId);
            }
            tasks.remove(downloadId);
        }

        @Override
        public void onComplete(long downloadId) {
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onComplete(downloadId);
            }
            DownloadListener downloadListener = listeners.get(downloadId);
            if (downloadListener != null) {
                downloadListener.onComplete(downloadId);
                listeners.remove(downloadId);
            }
            tasks.remove(downloadId);
        }

        @Override
        public void onError(long downloadId, int errorCode, String errorMessage) {
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onError(downloadId, errorCode, errorMessage);
            }
            DownloadListener downloadListener = listeners.get(downloadId);
            if (downloadListener != null) {
                downloadListener.onError(downloadId, errorCode, errorMessage);
                listeners.remove(downloadId);
            }
            tasks.remove(downloadId);
        }

        @Override
        public void onRemove(long downloadId) {
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onRemove(downloadId);
            }
            DownloadListener downloadListener = listeners.get(downloadId);
            if (downloadListener != null) {
                downloadListener.onRemove(downloadId);
                listeners.remove(downloadId);
            }
            tasks.remove(downloadId);
        }
    };

    private DownloadCursor downloadCursor = new DownloadCursor() {
        @Override
        public int getCount() {
            return downloadInfoList.size();
        }

        @Override
        public DownloadInfo getDownloadInfo(int position) {
            return downloadInfoList.get(position);
        }

        @Override
        public int getPosition(long downloadId) {
            for (int i = 0; i < downloadInfoList.size(); i++) {
                DownloadInfo downloadInfo = downloadInfoList.get(i);
                if (downloadInfo.getId() == downloadId) {
                    return i;
                }
            }
            return -1;
        }
    };
}
