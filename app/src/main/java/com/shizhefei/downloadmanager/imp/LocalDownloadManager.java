package com.shizhefei.downloadmanager.imp;

import android.util.LongSparseArray;
import android.util.Pair;

import com.shizhefei.downloadmanager.ErrorInfo;
import com.shizhefei.downloadmanager.base.AbsDownloadTask;
import com.shizhefei.downloadmanager.base.DownloadEntity;
import com.shizhefei.downloadmanager.base.DownloadListener;
import com.shizhefei.downloadmanager.base.DownloadParams;
import com.shizhefei.downloadmanager.base.DownloadTaskFactory;
import com.shizhefei.downloadmanager.base.IdGenerator;
import com.shizhefei.mvc.RequestHandle;
import com.shizhefei.task.IAsyncTask;
import com.shizhefei.task.TaskHandle;
import com.shizhefei.task.TaskHelper;
import com.shizhefei.task.tasks.Tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocalDownloadManager extends DownloadManager {
    private DownloadTaskFactory downloadTaskFactory;
    private IdGenerator idGenerator;
    private TaskHelper taskHelper = new TaskHelper();
    private LongSparseArray<Pair<RequestHandle, AbsDownloadTask>> tasks = new LongSparseArray<>();
    private LongSparseArray<DownloadListener> listeners = new LongSparseArray<>();
    private Set<DownloadListener> downloadListeners = new HashSet<>();
    private List<AbsDownloadTask> downloadEntities = new ArrayList<>();

    public LocalDownloadManager(DownloadTaskFactory downloadTaskFactory, IdGenerator idGenerator) {
        this.downloadTaskFactory = downloadTaskFactory;
        this.idGenerator = idGenerator;
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

        AbsDownloadTask downloadTask = downloadTaskFactory.buildDownloadTask(downloadId, downloadParams);
        downloadEntities.add(downloadTask);

        IAsyncTask<Void> finalTask = Tasks.donOnCallback(Tasks.async(downloadTask), downloadListener);

        TaskHandle taskHandle = taskHelper.execute(finalTask, proxyDownloadListener);

        tasks.put(downloadId, new Pair<RequestHandle, AbsDownloadTask>(taskHandle, downloadTask));

        return downloadId;
    }

    @Override
    public void pause(long downloadId) {
        Pair<RequestHandle, AbsDownloadTask> pair = tasks.get(downloadId);
        if (pair != null) {
            pair.first.cancle();
            tasks.remove(downloadId);
        }
    }

    @Override
    public void cancel(long downloadId) {
        pause(downloadId);
    }

    @Override
    public DownloadEntity getDownloadEntity(long id) {
        Pair<RequestHandle, AbsDownloadTask> data = tasks.get(id);
        if (data != null) {
            return data.second.getDownloadEntity();
        }
        return null;
    }

    @Override
    public DownloadParams getDownloadParams(long id) {
        Pair<RequestHandle, AbsDownloadTask> data = tasks.get(id);
        if (data != null) {
            return data.second.getDownloadParams();
        }
        return null;
    }

    @Override
    public DownloadEntity get(int position) {
        AbsDownloadTask downloadTask = downloadEntities.get(position);
        if (downloadTask != null) {
            return downloadTask.getDownloadEntity();
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

    private DownloadListener proxyDownloadListener = new DownloadListener() {
        @Override
        public void onPreExecute(long downloadId) {
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onPreExecute(downloadId);
            }
            DownloadListener downloadListener = listeners.get(downloadId);
            if (downloadListener != null) {
                downloadListener.onPreExecute(downloadId);
            }
        }

        @Override
        public void onProgress(long downloadId, int percent, long current, long total) {
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onProgress(downloadId, percent, downloadId, total);
            }
            DownloadListener downloadListener = listeners.get(downloadId);
            if (downloadListener != null) {
                downloadListener.onProgress(downloadId, percent, downloadId, total);
            }
        }

        @Override
        public void onConnected(long downloadId) {
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onConnected(downloadId);
            }
            DownloadListener downloadListener = listeners.get(downloadId);
            if (downloadListener != null) {
                downloadListener.onConnected(downloadId);
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
        public void onBlockComplete(long downloadId) {
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onBlockComplete(downloadId);
            }
            DownloadListener downloadListener = listeners.get(downloadId);
            if (downloadListener != null) {
                downloadListener.onBlockComplete(downloadId);
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
        public void onError(long downloadId, ErrorInfo errorInfo) {
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onError(downloadId, errorInfo);
            }
            DownloadListener downloadListener = listeners.get(downloadId);
            if (downloadListener != null) {
                downloadListener.onError(downloadId, errorInfo);
                listeners.remove(downloadId);
            }
        }

        @Override
        public void onCancel(long downloadId) {
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onCancel(downloadId);
            }
            DownloadListener downloadListener = listeners.get(downloadId);
            if (downloadListener != null) {
                downloadListener.onCancel(downloadId);
                listeners.remove(downloadId);
            }
        }
    };
}
