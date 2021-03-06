package com.shizhefei.download.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.LongSparseArray;

import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.base.DownloadInfoList;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.base.DownloadTaskFactory;
import com.shizhefei.download.base.IdGenerator;
import com.shizhefei.download.base.SpeedMonitor;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.idgenerator.DefaultIdGenerator;
import com.shizhefei.download.task.DownloadListenerProxy;
import com.shizhefei.download.taskfactory.DefaultDownloadTaskFactory;
import com.shizhefei.download.utils.DownloadUtils;
import com.shizhefei.mvc.RequestHandle;
import com.shizhefei.task.TaskHandle;
import com.shizhefei.task.TaskHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

public class LocalDownloadManager extends DownloadManager {
    private final DownloadDB downloadDB;
    private DownloadTaskFactory downloadTaskFactory;
    private IdGenerator idGenerator;
    private TaskHelper taskHelper = new TaskHelper();
    private LongSparseArray<DownloadData> tasks = new LongSparseArray<>();
    private Set<DownloadListener> downloadListeners = new LinkedHashSet<>();
    private List<DownloadInfo.Agency> downloadInfoAgencyList;
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
        downloadInfoAgencyList = new ArrayList<>(infoList.size());
        for (DownloadInfo.Agency infoAgency : infoList) {
            switch (infoAgency.getStatus()) {
                case DownloadManager.STATUS_CONNECTED:
                case DownloadManager.STATUS_PROGRESS:
                case DownloadManager.STATUS_PENDING:
                case DownloadManager.STATUS_START:
                case DownloadManager.STATUS_DOWNLOAD_RESET_SCHEDULE:
                    infoAgency.setStatus(DownloadManager.STATUS_PAUSED);
                    break;
            }
            if (!infoAgency.getHttpInfo().isAcceptRange()) {
                infoAgency.setCurrent(0);
            }
            downloadInfoAgencyList.add(infoAgency);
            DownloadUtils.logD("LocalDownloadManager :%s", infoAgency.getInfo());
        }
    }

    @NonNull
    @Override
    public List<DownloadInfo> find(String url) {
        List<DownloadInfo> list = new ArrayList<>(2);
        for (DownloadInfo.Agency infoAgency : downloadInfoAgencyList) {
            if (infoAgency.getDownloadParams().getUrl().equals(url)) {
                list.add(infoAgency.getInfo());
            }
        }
        return list;
    }

    @Override
    public void setIsWifiRequired(long downloadId, boolean isWifiRequired) {
        DownloadInfo.Agency infoAgency = getDownloadInfoAgency(downloadId);
        if (infoAgency != null) {
            DownloadParams downloadParams = infoAgency.getDownloadParams();
            downloadParams.setWifiRequired(isWifiRequired);
            downloadDB.updateDownloadParams(downloadId, downloadParams);
        }
    }

    @Override
    public DownloadInfo findFirst(String url) {
        for (DownloadInfo.Agency infoAgency : downloadInfoAgencyList) {
            if (infoAgency.getDownloadParams().getUrl().equals(url)) {
                return infoAgency.getInfo();
            }
        }
        return null;
    }

    @Override
    public DownloadInfo findFirst(String url, String dir, String fileName) {
        for (DownloadInfo.Agency infoAgency : downloadInfoAgencyList) {
            DownloadParams downloadParams = infoAgency.getDownloadParams();
            if (downloadParams.getUrl().equals(url) &&
                    downloadParams.getDir().equals(dir)
                    && (downloadParams.getFileName() != null && downloadParams.getFileName().equals(fileName))) {
                return infoAgency.getInfo();
            }
        }
        return null;
    }

    @Override
    public long start(DownloadParams downloadParams) {
        long downloadId = idGenerator.generateId(downloadParams);

        DownloadInfo.Agency infoAgency = buildDownloadInfoAgency(downloadId, downloadParams);

        AbsDownloadTask downloadTask = downloadTaskFactory.buildDownloadTask(downloadId, false, infoAgency, downloadDB, executor);
        downloadInfoAgencyList.add(infoAgency);

        TaskHandle taskHandle = taskHelper.execute(downloadTask, new DownloadListenerProxy(downloadId, proxyDownloadListener));
        tasks.put(downloadId, new DownloadData(taskHandle, infoAgency, downloadTask));
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
        DownloadInfo.Agency infoAgency = null;
        for (DownloadInfo.Agency agency : downloadInfoAgencyList) {
            if (agency.getId() == downloadId) {
                infoAgency = agency;
                break;
            }
        }

        if (infoAgency != null) {
            int status = infoAgency.getStatus();
            if (status == DownloadManager.STATUS_PAUSED || status == DownloadManager.STATUS_ERROR) {

                AbsDownloadTask downloadTask = downloadTaskFactory.buildDownloadTask(downloadId, false, infoAgency, downloadDB, executor);
                TaskHandle taskHandle = taskHelper.execute(downloadTask, new DownloadListenerProxy(downloadId, proxyDownloadListener));
                tasks.put(downloadId, new DownloadData(taskHandle, infoAgency, downloadTask));
                return true;
            }
        }
        return false;
    }

    @Override
    public void resumeAll() {
        for (DownloadInfo.Agency infoAgency : downloadInfoAgencyList) {
            resume(infoAgency.getId());
        }
    }

    @Override
    public void remove(long downloadId) {
        DownloadInfo.Agency infoAgency = null;
        Iterator<DownloadInfo.Agency> iterator = downloadInfoAgencyList.iterator();
        while (iterator.hasNext()) {
            DownloadInfo.Agency info = iterator.next();
            if (info.getId() == downloadId) {
                infoAgency = info;
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
            if (infoAgency != null) {
                AbsDownloadTask downloadTask = downloadTaskFactory.buildDownloadTask(downloadId, true, infoAgency, downloadDB, executor);
                downloadTask.remove();
            }
        }
        //通知移除
        proxyDownloadListener.onRemove(downloadId);
    }

    @Override
    public void removeAll() {
        ArrayList<DownloadInfo.Agency> list = new ArrayList<>(downloadInfoAgencyList);
        for (DownloadInfo.Agency agency : list) {
            remove(agency.getId());
        }
    }

    @Override
    public void removeByStatus(int statusFlag) {
        ArrayList<DownloadInfo.Agency> list = new ArrayList<>(downloadInfoAgencyList);
        for (DownloadInfo.Agency agency : list) {
            if (matches(agency, statusFlag)) {
                remove(agency.getId());
            }
        }
    }

    @Override
    public DownloadInfo getDownloadInfo(long downloadId) {
        for (DownloadInfo.Agency infoAgency : downloadInfoAgencyList) {
            if (infoAgency.getId() == downloadId) {
                return infoAgency.getInfo();
            }
        }
        return null;
    }

    private DownloadInfo.Agency getDownloadInfoAgency(long downloadId) {
        for (DownloadInfo.Agency infoAgency : downloadInfoAgencyList) {
            if (infoAgency.getId() == downloadId) {
                return infoAgency;
            }
        }
        return null;
    }

    @Override
    public DownloadParams getDownloadParams(long downloadId) {
        for (DownloadInfo.Agency infoAgency : downloadInfoAgencyList) {
            if (infoAgency.getId() == downloadId) {
                return infoAgency.getDownloadParams();
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
                for (DownloadInfo.Agency infoAgency : downloadInfoAgencyList) {
                    if (matches(infoAgency, downloadStatus)) {
                        count++;
                    }
                }
                return count;
            }

            @Override
            public DownloadInfo getDownloadInfo(int position) {
                int p = 0;
                for (DownloadInfo.Agency infoAgency : downloadInfoAgencyList) {
                    if (matches(infoAgency, downloadStatus)) {
                        if (p == position) {
                            return infoAgency.getInfo();
                        }
                        p++;
                    }
                }
                return null;
            }

            @Override
            public int getPosition(long downloadId) {
                int p = 0;
                for (DownloadInfo.Agency infoAgency : downloadInfoAgencyList) {
                    if (matches(infoAgency, downloadStatus)) {
                        if (infoAgency.getId() == downloadId) {
                            return p;
                        }
                        p++;
                    } else if (infoAgency.getId() == downloadId) {
                        break;
                    }
                }
                return DownloadManager.INVALID_POSITION;
            }
        };
    }

    private boolean matches(DownloadInfo.Agency infoAgency, int downloadStatus) {
        return (infoAgency.getStatus() & downloadStatus) != 0;
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
        private final DownloadInfo.Agency infoAgency;
        private RequestHandle requestHandle;
        private AbsDownloadTask task;

        public DownloadData(RequestHandle requestHandle, DownloadInfo.Agency infoAgency, AbsDownloadTask task) {
            this.infoAgency = infoAgency;
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
        public void onProgressUpdate(long downloadId, long current, long total) {
            speedMonitor.setProgress(downloadId, current, total);
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onProgressUpdate(downloadId, current, total);
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
        public void onBlockStart(long downloadId, String blockName, String blockInfo, long current, long total, long blockCurrent, long blockTotal) {
            super.onBlockStart(downloadId, blockName, blockInfo, current, total, blockCurrent, blockTotal);
            speedMonitor.setProgress(downloadId, current, total);
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onBlockStart(downloadId, blockName, blockInfo, current, total, blockCurrent, blockTotal);
            }
        }

        @Override
        public void onBlockComplete(long downloadId, String blockName, String blockInfo, long current, long total, long blockCurrent, long blockTotal) {
            super.onBlockComplete(downloadId, blockName, blockInfo, current, total, blockCurrent, blockTotal);
            speedMonitor.setProgress(downloadId, current, total);
            for (DownloadListener downloadListener : downloadListeners) {
                downloadListener.onBlockComplete(downloadId, blockName, blockInfo, current, total, blockCurrent, blockTotal);
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
            return downloadInfoAgencyList.size();
        }

        @Override
        public DownloadInfo getDownloadInfo(int position) {
            return downloadInfoAgencyList.get(position).getInfo();
        }

        @Override
        public int getPosition(long downloadId) {
            for (int i = 0; i < downloadInfoAgencyList.size(); i++) {
                DownloadInfo.Agency infoAgency = downloadInfoAgencyList.get(i);
                if (infoAgency.getId() == downloadId) {
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

    private DownloadInfo.Agency buildDownloadInfoAgency(long downloadId, DownloadParams downloadParams) {
        DownloadInfo.Agency downloadInfoAgency = new DownloadInfo.Agency(downloadParams);
        downloadInfoAgency.setId(downloadId);
        downloadInfoAgency.setUrl(downloadParams.getUrl());
        downloadInfoAgency.setCurrent(0);
        if (downloadParams.getTotalSize() > 0) {
            downloadInfoAgency.setTotal(downloadParams.getTotalSize());
        } else {
            downloadInfoAgency.setTotal(0);
        }
        downloadInfoAgency.setStartTime(System.currentTimeMillis());
        downloadInfoAgency.setStatus(DownloadManager.STATUS_PENDING);
        downloadInfoAgency.setFilename(downloadParams.getFileName());
        downloadInfoAgency.setDir(downloadParams.getDir());
        return downloadInfoAgency;
    }
}
