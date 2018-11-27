package com.shizhefei.download.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LongSparseArray;

import com.shizhefei.download.aidl.DownloadInfoListAidl;
import com.shizhefei.download.aidl.DownloadListenerAidl;
import com.shizhefei.download.aidl.DownloadServerAidl;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.base.DownloadInfoList;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.service.DownloadService;
import com.shizhefei.download.utils.DownloadUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RemoteDownloadManager extends DownloadManager {
    private volatile DownloadServerAidl eventServiceExecutor;
    private LongSparseArray<DownloadListener> listeners = new LongSparseArray<>();
    private Set<DownloadListener> downloadListeners = new HashSet<>();
    private Handler handler;
    private boolean executeBind;

    public RemoteDownloadManager() {
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public DownloadInfo findFirst(String url) {
        checkHasExecuteBind();
        try {
            return eventServiceExecutor.findFirstByUrl(url);
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager error");
        }
        return null;
    }

    @NonNull
    @Override
    public List<DownloadInfo> find(String url) {
        checkHasExecuteBind();
        try {
            return eventServiceExecutor.findByUrl(url);
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager error");
        }
        return new ArrayList<>(0);
    }

    @Nullable
    @Override
    public DownloadInfo findFirst(String url, String dir, String fileName) {
        checkHasExecuteBind();
        try {
            return eventServiceExecutor.findFirstByUrlAndFileName(url, dir, fileName);
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager error");
        }
        return null;
    }

    @Override
    public long start(DownloadParams downloadParams, DownloadListener downloadListener) {
        checkHasExecuteBind();
        try {
            long downloadId = eventServiceExecutor.start(downloadParams);
            listeners.put(downloadId, downloadListener);
            return downloadId;
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager error");
        }
        return -1;
    }

    @Override
    public long start(DownloadParams downloadParams) {
        checkHasExecuteBind();
        try {
            return eventServiceExecutor.start(downloadParams);
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager error");
        }
        return -1;
    }

    @Override
    public boolean restartPauseOrFail(long downloadId, DownloadListener downloadListener) {
        checkHasExecuteBind();
        try {
            if (eventServiceExecutor.restartPauseOrFail(downloadId)) {
                listeners.put(downloadId, downloadListener);
                return true;
            }
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager error");
        }
        return false;
    }

    @Override
    public void pause(long downloadId) {
        checkHasExecuteBind();
        try {
            eventServiceExecutor.pause(downloadId);
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager error");
        }
    }

    @Override
    public void pauseAll() {
        checkHasExecuteBind();
        try {
            eventServiceExecutor.pauseAll();
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager error");
        }
    }

    @Override
    public void remove(long downloadId) {
        checkHasExecuteBind();
        try {
            eventServiceExecutor.remove(downloadId);
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager error");
        }
    }

    @Override
    public DownloadInfo getDownloadInfo(long id) {
        checkHasExecuteBind();
        try {
            return eventServiceExecutor.getDownloadInfo(id);
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager error");
        }
        return null;
    }

    @Override
    public DownloadParams getDownloadParams(long id) {
        checkHasExecuteBind();
        try {
            return eventServiceExecutor.getDownloadParams(id);
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager error");
        }
        return null;
    }

    @Override
    public DownloadInfoList createDownloadInfoList() {
        checkHasExecuteBind();
        return downloadInfoList;
    }

    @Override
    public DownloadInfoList createDownloadInfoList(final int statusFlags) {
        checkHasExecuteBind();
        return new DownloadInfoList() {
            private DownloadInfoListAidl downloadInfoList;

            @Override
            public int getCount() {
                try {
                    return get().getCount();
                } catch (Exception e) {
                    DownloadUtils.logE(e, "RemoteDownloadManager error");
                    downloadInfoList = null;
                }
                return 0;
            }

            @Override
            public DownloadInfo getDownloadInfo(int position) {
                try {
                    return get().getDownloadInfo(position);
                } catch (Exception e) {
                    DownloadUtils.logE(e, "RemoteDownloadManager error");
                    downloadInfoList = null;
                }
                return null;
            }

            @Override
            public int getPosition(long downloadId) {
                try {
                    return get().getPosition(downloadId);
                } catch (Exception e) {
                    DownloadUtils.logE(e, "RemoteDownloadManager error");
                    downloadInfoList = null;
                }
                return DownloadManager.INVALID_POSITION;
            }

            private DownloadInfoListAidl get() {
                if (downloadInfoList == null) {
                    try {
                        downloadInfoList = eventServiceExecutor.createDownloadInfoList(statusFlags);
                    } catch (Exception e) {
                        DownloadUtils.logE(e, "RemoteDownloadManager error");
                    }
                }
                return downloadInfoList;
            }
        };
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
        executeBind = true;
        if (!isConnected()) {
            Intent intent = new Intent(DownloadManager.getApplicationContext(), DownloadService.class);
            DownloadManager.getApplicationContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void unBindService() {
        executeBind = false;
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
            } catch (Exception e) {
                DownloadUtils.logE(e, "RemoteDownloadManager error");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            eventServiceExecutor = null;
            if (executeBind) {
                bindService();
            }
        }
    };

    private DownloadListenerAidl.Stub downloadListenerProxy = new DownloadListenerAidl.Stub() {
        @Override
        public void onPending(final long downloadId) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DownloadListener downloadListener : downloadListeners) {
                        downloadListener.onPending(downloadId);
                    }
                    DownloadListener downloadListener = listeners.get(downloadId);
                    if (downloadListener != null) {
                        downloadListener.onPending(downloadId);
                    }
                }
            });
        }

        @Override
        public void onStart(final long downloadId, final long current, final long total) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DownloadListener downloadListener : downloadListeners) {
                        downloadListener.onStart(downloadId, current, total);
                    }
                    DownloadListener downloadListener = listeners.get(downloadId);
                    if (downloadListener != null) {
                        downloadListener.onStart(downloadId, current, total);
                    }
                }
            });
        }

        @Override
        public void onDownloadIng(final long downloadId, final long current, final long total) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DownloadListener downloadListener : downloadListeners) {
                        downloadListener.onDownloadIng(downloadId, current, total);
                    }
                    DownloadListener downloadListener = listeners.get(downloadId);
                    if (downloadListener != null) {
                        downloadListener.onDownloadIng(downloadId, current, total);
                    }
                }
            });
        }

        @Override
        public void onDownloadResetSchedule(final long downloadId, final int reason, final long current, final long total) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DownloadListener downloadListener : downloadListeners) {
                        downloadListener.onDownloadResetSchedule(downloadId, reason, current, total);
                    }
                    DownloadListener downloadListener = listeners.get(downloadId);
                    if (downloadListener != null) {
                        downloadListener.onDownloadResetSchedule(downloadId, reason, current, total);
                    }
                }
            });
        }

        @Override
        public void onConnected(final long downloadId, final HttpInfo httpInfo, final String saveDir, final String saveFileName, final String tempFileName, final long current, final long total) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DownloadListener downloadListener : downloadListeners) {
                        downloadListener.onConnected(downloadId, httpInfo, saveDir, saveFileName, tempFileName, current, total);
                    }
                    DownloadListener downloadListener = listeners.get(downloadId);
                    if (downloadListener != null) {
                        downloadListener.onConnected(downloadId, httpInfo, saveDir, saveFileName, tempFileName, current, total);
                    }
                }
            });
        }

        @Override
        public void onPaused(final long downloadId) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DownloadListener downloadListener : downloadListeners) {
                        downloadListener.onPaused(downloadId);
                    }
                    DownloadListener downloadListener = listeners.get(downloadId);
                    if (downloadListener != null) {
                        downloadListener.onPaused(downloadId);
                    }
                }
            });
        }

        @Override
        public void onComplete(final long downloadId) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DownloadListener downloadListener : downloadListeners) {
                        downloadListener.onComplete(downloadId);
                    }
                    DownloadListener downloadListener = listeners.get(downloadId);
                    if (downloadListener != null) {
                        downloadListener.onComplete(downloadId);
                        listeners.remove(downloadId);
                    }
                }
            });
        }

        @Override
        public void onError(final long downloadId, final int errorCode, final String errorMessage) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DownloadListener downloadListener : downloadListeners) {
                        downloadListener.onError(downloadId, errorCode, errorMessage);
                    }
                    DownloadListener downloadListener = listeners.get(downloadId);
                    if (downloadListener != null) {
                        downloadListener.onError(downloadId, errorCode, errorMessage);
                        listeners.remove(downloadId);
                    }
                }
            });
        }

        @Override
        public void onRemove(final long downloadId) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DownloadListener downloadListener : downloadListeners) {
                        downloadListener.onRemove(downloadId);
                    }
                    DownloadListener downloadListener = listeners.get(downloadId);
                    if (downloadListener != null) {
                        downloadListener.onRemove(downloadId);
                        listeners.remove(downloadId);
                    }
                }
            });
        }
    };

    private DownloadInfoList downloadInfoList = new DownloadInfoList() {
        private DownloadInfo DOWNLOAD_INFO_NONE = new DownloadInfo.Agency(new DownloadParams.Builder().build()).getInfo();

        @Override
        public int getCount() {
            try {
                return eventServiceExecutor.getCount();
            } catch (Exception e) {
                DownloadUtils.logE(e, "RemoteDownloadManager error");
            }
            return 0;
        }

        @Override
        public DownloadInfo getDownloadInfo(int position) {
            try {
                return eventServiceExecutor.getDownloadInfo(position);
            } catch (Exception e) {
                DownloadUtils.logE(e, "RemoteDownloadManager error");
            }
            return DOWNLOAD_INFO_NONE;
        }

        @Override
        public int getPosition(long downloadId) {
            try {
                return eventServiceExecutor.getPosition(downloadId);
            } catch (Exception e) {
                DownloadUtils.logE(e, "RemoteDownloadManager error");
            }
            return DownloadManager.INVALID_POSITION;
        }
    };

    private void checkHasExecuteBind() {
        if (!executeBind) {
            throw new RuntimeException("请先调用bind，建议在Application的onCreate调用，需要调用DownloadManager.getRemote()的进程调用，其它进程可以不必调用");
        }
        if (eventServiceExecutor == null) {
            bindService();
        }
    }
}
