package com.shizhefei.download.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LongSparseArray;

import com.shizhefei.download.aidl.DownloadListenerAidl;
import com.shizhefei.download.aidl.DownloadServerAidl;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.base.DownloadCursor;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.service.DownloadService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RemoteDownloadManager extends DownloadManager {
    private volatile DownloadServerAidl eventServiceExecutor;
    private LongSparseArray<DownloadListener> listeners = new LongSparseArray<>();
    private Set<DownloadListener> downloadListeners = new HashSet<>();

    @Nullable
    @Override
    public DownloadInfo findFirst(String url) {
        try {
            return eventServiceExecutor.findFirstByUrl(url);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    @Override
    public List<DownloadInfo> find(String url) {
        try {
            return eventServiceExecutor.findByUrl(url);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(0);
    }

    @Nullable
    @Override
    public DownloadInfo findFirst(String url, String dir, String fileName) {
        try {
            return eventServiceExecutor.findFirstByUrlAndFileName(url, dir, fileName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long start(DownloadParams downloadParams, DownloadListener downloadListener) {
        try {
            long downloadId = eventServiceExecutor.start(downloadParams);
            listeners.put(downloadId, downloadListener);
            return downloadId;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public long start(DownloadParams downloadParams) {
        try {
            return eventServiceExecutor.start(downloadParams);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean restartPauseOrFail(long downloadId, DownloadListener downloadListener) {
        try {
            if (eventServiceExecutor.restartPauseOrFail(downloadId)) {
                listeners.put(downloadId, downloadListener);
                return true;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void pause(long downloadId) {
        try {
            eventServiceExecutor.pause(downloadId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pauseAll() {
        try {
            eventServiceExecutor.pauseAll();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(long downloadId) {
        try {
            eventServiceExecutor.remove(downloadId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DownloadInfo getDownloadEntity(long id) {
        try {
            return eventServiceExecutor.getDownloadEntity(id);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public DownloadParams getDownloadParams(long id) {
        try {
            return eventServiceExecutor.getDownloadParams(id);
        } catch (RemoteException e) {
            e.printStackTrace();
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
    }

    @Override
    public void unregisterDownloadListener(DownloadListener downloadListener) {
        downloadListeners.remove(downloadListener);
    }

    public boolean isConnected() {
        return eventServiceExecutor != null;
    }

    public void bindService() {
        if (!isConnected()) {
            Intent intent = new Intent(DownloadManager.getApplicationContext(), DownloadService.class);
            DownloadManager.getApplicationContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void unBindService() {
        if (!isConnected()) {
            DownloadManager.getApplicationContext().unbindService(serviceConnection);
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            eventServiceExecutor = DownloadServerAidl.Stub.asInterface(service);
            try {
                eventServiceExecutor.registerDownloadListener(downloadListenerProxy);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            eventServiceExecutor = null;
        }
    };

    private DownloadListenerAidl.Stub downloadListenerProxy = new DownloadListenerAidl.Stub() {
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

    private DownloadCursor downloadCursor = new DownloadCursor() {
        @Override
        public int getCount() {
            try {
                return eventServiceExecutor.getCount();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        public DownloadInfo getDownloadInfo(int position) {
            try {
                return eventServiceExecutor.getDownloadInfo(position);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public int getPosition(long downloadId) {
            try {
                return eventServiceExecutor.getPosition(downloadId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return 0;
        }
    };
}
