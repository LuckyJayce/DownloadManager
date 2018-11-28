package com.shizhefei.download.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.LongSparseArray;

import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.base.SpeedMonitor;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.base.DownloadTaskFactory;
import com.shizhefei.download.base.IdGenerator;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.idgenerator.DefaultIdGenerator;
import com.shizhefei.download.taskfactory.DefaultDownloadTaskFactory;
import com.shizhefei.download.base.DownloadInfoList;
import com.shizhefei.download.task.DownloadListenerProxy;
import com.shizhefei.download.utils.DownloadUtils;
import com.shizhefei.mvc.RequestHandle;
import com.shizhefei.task.TaskHandle;
import com.shizhefei.task.TaskHelper;

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
    private Set<DownloadListener> downloadListeners = new HashSet<>();
    private List<DownloadInfo> downloadInfoList;
    private Executor executor;
    private SpeedMonitorImp speedMonitor;

    public LocalDownloadManager(Context context, DownloadTaskFactory downloadTaskFactory, Executor executor) {
        this.downloadDB = new DownloadDB(context, AsyncTask.SERIAL_EXECUTOR);
        this.speedMonitor = new SpeedMonitorImp();
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
                case DownloadManager.STATUS_DOWNLOAD_RESET_SCHEDULE:
                    info.setStatus(DownloadManager.STATUS_PAUSED);
                    break;
            }
            if (!info.getHttpInfo().isAcceptRange()) {
                info.setCurrent(0);
            }
            downloadInfoList.add(info.getInfo());
            DownloadUtils.logD("LocalDownloadManager :%s", info);
        }
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
    public long start(DownloadParams downloadParams) {
        long downloadId = idGenerator.generateId(downloadParams);

        AbsDownloadTask downloadTask = downloadTaskFactory.buildDownloadTask(downloadId, false, downloadParams, downloadDB, executor);
        downloadInfoList.add(downloadTask.getDownloadInfo());

        TaskHandle taskHandle = taskHelper.execute(downloadTask, new DownloadListenerProxy(downloadId, proxyDownloadListener));
        tasks.put(downloadId, new DownloadData(taskHandle, downloadTask));
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
    public boolean resume(long downloadId) {
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

                AbsDownloadTask downloadTask = downloadTaskFactory.buildDownloadTask(downloadId, false, downloadParams, downloadDB, executor);
                downloadInfoList.set(index, downloadTask.getDownloadInfo());

                TaskHandle taskHandle = taskHelper.execute(downloadTask, new DownloadListenerProxy(downloadId, proxyDownloadListener));
                tasks.put(downloadId, new DownloadData(taskHandle, downloadTask));
                return true;
            }
        }
        return false;
    }

    @Override
    public void resumeAll() {
        for (DownloadInfo downloadInfo : downloadInfoList) {
            resume(downloadInfo.getId());
        }
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
            data.task.remove();
            tasks.remove(downloadId);
            downloadDB.delete(downloadId);
        } else {//没有在执行，直接删除数据库中的
            if (downloadInfo != null) {
                AbsDownloadTask downloadTask = downloadTaskFactory.buildDownloadTask(downloadId, true, downloadInfo.getDownloadParams(), downloadDB, executor);
                downloadTask.remove();
            }
        }
        //通知移除
        proxyDownloadListener.onRemove(downloadId);
    }

    @Override
    public DownloadInfo getDownloadInfo(long downloadId) {
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
    public SpeedMonitor getSpeedMonitor() {
        return speedMonitor;
    }

    @Override
    public DownloadInfoList createDownloadInfoList() {
        return downloadInfoListProxy;
    }

    public DownloadInfoList createDownloadInfoList(final int downloadStatus) {
        return new DownloadInfoList() {
            @Override
            public int getCount() {
                int count = 0;
                for (DownloadInfo downloadInfo : downloadInfoList) {
                    if (isNeed(downloadInfo)) {
                        count++;
                    }
                }
                return count;
            }

            @Override
            public DownloadInfo getDownloadInfo(int position) {
                int p = 0;
                for (DownloadInfo downloadInfo : downloadInfoList) {
                    if (isNeed(downloadInfo)) {
                        if (p == position) {
                            return downloadInfo;
                        }
                        p++;
                    }
                }
                return null;
            }

            @Override
            public int getPosition(long downloadId) {
                int p = 0;
                for (DownloadInfo downloadInfo : downloadInfoList) {
                    if (isNeed(downloadInfo)) {
                        if (downloadInfo.getId() == downloadId) {
                            return p;
                        }
                        p++;
                    } else if (downloadInfo.getId() == downloadId) {
                        break;
                    }
                }
                return DownloadManager.INVALID_POSITION;
            }

            private boolean isNeed(DownloadInfo downloadInfo) {
                return (downloadInfo.getStatus() & downloadStatus) != 0;
            }
        };
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

        public DownloadData(RequestHandle requestHandle, AbsDownloadTask task) {
            this.requestHandle = requestHandle;
            this.task = task;
        }
    }

    private DownloadListener proxyDownloadListener = new DownloadListener() {

        @Override
        public void onPending(long downloadId) {
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onPending(downloadId);
            }
        }

        @Override
        public void onStart(long downloadId, long current, long total) {
            speedMonitor.setProgress(downloadId, current, total);
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onStart(downloadId, current, total);
            }
        }

        @Override
        public void onDownloadIng(long downloadId, long current, long total) {
            speedMonitor.setProgress(downloadId, current, total);
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onDownloadIng(downloadId, current, total);
            }
        }

        @Override
        public void onDownloadResetSchedule(long downloadId, int reason, long current, long total) {
            speedMonitor.setProgress(downloadId, current, total);
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onDownloadResetSchedule(downloadId, reason, current, total);
            }
        }

        @Override
        public void onConnected(long downloadId, HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName, long current, long total) {
            speedMonitor.setProgress(downloadId, current, total);
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onConnected(downloadId, httpInfo, saveDir, saveFileName, tempFileName, current, total);
            }
        }

        @Override
        public void onPaused(long downloadId) {
            speedMonitor.stop(downloadId);
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onPaused(downloadId);
            }
            tasks.remove(downloadId);
        }

        @Override
        public void onComplete(long downloadId) {
            speedMonitor.stop(downloadId);
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onComplete(downloadId);
            }
            tasks.remove(downloadId);
        }

        @Override
        public void onError(long downloadId, int errorCode, String errorMessage) {
            speedMonitor.stop(downloadId);
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onError(downloadId, errorCode, errorMessage);
            }
            tasks.remove(downloadId);
        }

        @Override
        public void onRemove(long downloadId) {
            speedMonitor.stop(downloadId);
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onRemove(downloadId);
            }
            tasks.remove(downloadId);
        }
    };

    private DownloadInfoList downloadInfoListProxy = new DownloadInfoList() {
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
            return DownloadManager.INVALID_POSITION;
        }
    };

    private static class SpeedMonitorImp implements SpeedMonitor {
        private int mMinIntervalUpdateSpeed = 1000;

        private LongSparseArray<SpeedData> speedDataArray = new LongSparseArray<>();

        @Override
        public long getTotalSpeed() {
            int size = speedDataArray.size();
            long speed = 0;
            for (int i = 0; i < size; i++) {
                speed += speedDataArray.valueAt(i).speed;
            }
            return speed;
        }

        @Override
        public long getSpeed(long downloadId) {
            SpeedData speedData = speedDataArray.get(downloadId);
            if (speedData != null) {
                return speedData.speed;
            }
            return 0;
        }

        void setProgress(long downloadId, long current, long total) {
            SpeedData speedData = speedDataArray.get(downloadId);
            if (speedData == null) {
                speedData = new SpeedData();
                speedDataArray.put(downloadId, speedData);
            }
            if (mMinIntervalUpdateSpeed <= 0) {
                return;
            }
            boolean isUpdateData = false;

            if (speedData.lastRefreshTime == 0) {
                isUpdateData = true;
            } else {
                long interval = SystemClock.uptimeMillis() - speedData.lastRefreshTime;
                if (interval >= mMinIntervalUpdateSpeed || (speedData.speed == 0 && interval > 0)) {
                    long speed = (int) ((current - speedData.current) / interval * 1000);//因为单位是毫秒所以 interval要乘以1000
                    speed = Math.max(0, speed);
                    speedData.speed = speed;
                    isUpdateData = true;
                }
            }

            if (isUpdateData) {
                speedData.current = current;
                speedData.lastRefreshTime = SystemClock.uptimeMillis();
            }
        }

        void stop(long downloadId) {
            speedDataArray.remove(downloadId);
        }

        private class SpeedData {
            private long current;
            private long speed;
            private long lastRefreshTime;
        }
    }
}
