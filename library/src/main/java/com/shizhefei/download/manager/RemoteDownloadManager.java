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

import com.shizhefei.download.aidl.DownloadInfoListAidl;
import com.shizhefei.download.aidl.DownloadListenerAidl;
import com.shizhefei.download.aidl.DownloadServerAidl;
import com.shizhefei.download.aidl.SpeedMonitorAidl;
import com.shizhefei.download.base.SpeedMonitor;
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
            DownloadUtils.logE(e, "RemoteDownloadManager findFirst error");
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
            DownloadUtils.logE(e, "RemoteDownloadManager find error");
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
            DownloadUtils.logE(e, "RemoteDownloadManager findFirst error");
        }
        return null;
    }

    @Override
    public long start(DownloadParams downloadParams) {
        checkHasExecuteBind();
        try {
            return eventServiceExecutor.start(downloadParams);
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager start error");
        }
        return -1;
    }

    @Override
    public void pause(long downloadId) {
        checkHasExecuteBind();
        try {
            eventServiceExecutor.pause(downloadId);
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager pause error");
        }
    }

    @Override
    public void pauseAll() {
        checkHasExecuteBind();
        try {
            eventServiceExecutor.pauseAll();
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager pauseAll error");
        }
    }

    @Override
    public boolean resume(long downloadId) {
        checkHasExecuteBind();
        try {
            if (eventServiceExecutor.resume(downloadId)) {
                return true;
            }
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager resume error");
        }
        return false;
    }

    @Override
    public void resumeAll() {
        checkHasExecuteBind();
        try {
            eventServiceExecutor.resumeAll();
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager resume error");
        }
    }

    @Override
    public void remove(long downloadId) {
        checkHasExecuteBind();
        try {
            eventServiceExecutor.remove(downloadId);
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager remove error");
        }
    }

    @Override
    public DownloadInfo getDownloadInfo(long id) {
        checkHasExecuteBind();
        try {
            return eventServiceExecutor.getDownloadInfo(id);
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager getDownloadInfo error");
        }
        return null;
    }

    @Override
    public DownloadParams getDownloadParams(long id) {
        checkHasExecuteBind();
        try {
            return eventServiceExecutor.getDownloadParams(id);
        } catch (Exception e) {
            DownloadUtils.logE(e, "RemoteDownloadManager getDownloadParams error");
        }
        return null;
    }

    @Override
    public SpeedMonitor getSpeedMonitor() {
        return new SpeedMonitor() {
            private SpeedMonitorAidl speedMonitorAidl;

            @Override
            public long getTotalSpeed() {
                try {
                    return get().getTotalSpeed();
                } catch (Exception e) {
                    DownloadUtils.logE(e, "RemoteDownloadManager SpeedMonitorAidl getTotalSpeed error");
                }
                return 0;
            }

            @Override
            public long getSpeed(long downloadId) {
                try {
                    return get().getSpeed(downloadId);
                } catch (Exception e) {
                    DownloadUtils.logE(e, "RemoteDownloadManager SpeedMonitorAidl  getSpeed error");
                }
                return 0;
            }

            private SpeedMonitorAidl get() {
                if (speedMonitorAidl == null) {
                    try {
                        speedMonitorAidl = eventServiceExecutor.getSpeedMonitor();
                    } catch (Exception e) {
                        DownloadUtils.logE(e, "RemoteDownloadManager SpeedMonitorAidl get error");
                    }
                }
                return speedMonitorAidl;
            }
        };
    }

    @Override
    public DownloadInfoList createDownloadInfoList() {
        checkHasExecuteBind();
        return new DownloadInfoList() {
            private DownloadInfoListAidl downloadInfoList;

            @Override
            public int getCount() {
                try {
                    return get().getCount();
                } catch (Exception e) {
                    DownloadUtils.logE(e, "RemoteDownloadManager DownloadInfoList getCount error");
                    downloadInfoList = null;
                }
                return 0;
            }

            @Override
            public DownloadInfo getDownloadInfo(int position) {
                try {
                    return get().getDownloadInfo(position);
                } catch (Exception e) {
                    DownloadUtils.logE(e, "RemoteDownloadManager DownloadInfoList getDownloadInfo error");
                    downloadInfoList = null;
                }
                return null;
            }

            @Override
            public int getPosition(long downloadId) {
                try {
                    return get().getPosition(downloadId);
                } catch (Exception e) {
                    DownloadUtils.logE(e, "RemoteDownloadManager DownloadInfoList getPosition error");
                    downloadInfoList = null;
                }
                return DownloadManager.INVALID_POSITION;
            }

            private DownloadInfoListAidl get() {
                if (downloadInfoList == null) {
                    try {
                        downloadInfoList = eventServiceExecutor.createDownloadInfoList();
                    } catch (Exception e) {
                        DownloadUtils.logE(e, "RemoteDownloadManager get DownloadInfoList error");
                    }
                }
                return downloadInfoList;
            }
        };
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
                    DownloadUtils.logE(e, "RemoteDownloadManager DownloadInfoList getCount error");
                    downloadInfoList = null;
                }
                return 0;
            }

            @Override
            public DownloadInfo getDownloadInfo(int position) {
                try {
                    return get().getDownloadInfo(position);
                } catch (Exception e) {
                    DownloadUtils.logE(e, "RemoteDownloadManager DownloadInfoList getDownloadInfo error");
                    downloadInfoList = null;
                }
                return null;
            }

            @Override
            public int getPosition(long downloadId) {
                try {
                    return get().getPosition(downloadId);
                } catch (Exception e) {
                    DownloadUtils.logE(e, "RemoteDownloadManager DownloadInfoList getPosition error");
                    downloadInfoList = null;
                }
                return DownloadManager.INVALID_POSITION;
            }

            private DownloadInfoListAidl get() {
                if (downloadInfoList == null) {
                    try {
                        downloadInfoList = eventServiceExecutor.createDownloadInfoListByStatus(statusFlags);
                    } catch (Exception e) {
                        DownloadUtils.logE(e, "RemoteDownloadManager get DownloadInfoListAidl error");
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
                DownloadUtils.logE(e, "RemoteDownloadManager onServiceConnected registerDownloadListener error");
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
                }
            });
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
