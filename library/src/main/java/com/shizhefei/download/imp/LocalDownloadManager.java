package com.shizhefei.download.imp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.LongSparseArray;

import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.base.RemoveHandler;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.base.DownloadParams;
import com.shizhefei.download.base.DownloadTaskFactory;
import com.shizhefei.download.base.IdGenerator;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.prxoy.DownloadListenerProxy;
import com.shizhefei.mvc.RequestHandle;
import com.shizhefei.task.TaskHandle;
import com.shizhefei.task.TaskHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocalDownloadManager extends DownloadManager {
    private final DownloadDB downloadDB;
    private DownloadTaskFactory downloadTaskFactory;
    private IdGenerator idGenerator;
    private TaskHelper taskHelper = new TaskHelper();
    private LongSparseArray<DownloadData> tasks = new LongSparseArray<>();
    private LongSparseArray<DownloadListener> listeners = new LongSparseArray<>();
    private Set<DownloadListener> downloadListeners = new HashSet<>();
    private List<AbsDownloadTask> downloadEntities = new ArrayList<>();

    public LocalDownloadManager(Context context, DownloadTaskFactory downloadTaskFactory) {
        this.downloadDB = new DownloadDB(context, AsyncTask.SERIAL_EXECUTOR);
        if (downloadTaskFactory == null) {
            this.downloadTaskFactory = new DefaultDownloadTaskFactory();
        } else {
            this.downloadTaskFactory = downloadTaskFactory;
        }
        this.idGenerator = new IdGenerator() {
            @Override
            public long generateId(DownloadParams downloadParams) {
                return downloadDB.addAndGetDownloadId();
            }
        };
        //TODO 把数据库中的task查找出来赋值给tasks
    }

    @Override
    public long start(DownloadParams downloadParams) {
        return start(downloadParams, null);
    }

    @Override
    public long start(DownloadParams downloadParams, DownloadListener downloadListener) {
        long downloadId = idGenerator.generateId(downloadParams);
        if (downloadListener != null) {
            listeners.put(downloadId, downloadListener);
        }

        RemoveHandler removeHandler = new RemoveHandlerImp();
        AbsDownloadTask downloadTask = downloadTaskFactory.buildDownloadTask(downloadId, downloadParams, downloadDB, removeHandler);
        downloadEntities.add(downloadTask);

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
    public void cancel(long downloadId) {
        DownloadData data = tasks.get(downloadId);
        if (data != null) {
            data.removeHandler.remove();
            tasks.remove(downloadId);
        }
    }

    @Override
    public DownloadInfo getDownloadEntity(long downloadId) {
        DownloadData data = tasks.get(downloadId);
        if (data != null) {
            return data.task.getDownloadInfo();
        }
        return null;
    }

    @Override
    public DownloadParams getDownloadParams(long id) {
        DownloadData data = tasks.get(id);
        if (data != null) {
            return data.task.getDownloadParams();
        }
        return null;
    }

    @Override
    public DownloadInfo get(int position) {
        AbsDownloadTask downloadTask = downloadEntities.get(position);
        if (downloadTask != null) {
            return downloadTask.getDownloadInfo();
        }
        return null;
    }

    @Override
    public int getTaskCount() {
        return downloadEntities.size();
    }

    @Override
    public void registerDownloadListener(DownloadListener downloadListener) {
        downloadListeners.add(downloadListener);
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
        public void onDownloadIng(long downloadId, long current, long total) {
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onDownloadIng(downloadId, downloadId, total);
            }
            DownloadListener downloadListener = listeners.get(downloadId);
            if (downloadListener != null) {
                downloadListener.onDownloadIng(downloadId, downloadId, total);
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
        }
    };
}
