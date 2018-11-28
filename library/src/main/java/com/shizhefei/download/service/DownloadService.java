package com.shizhefei.download.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.shizhefei.download.aidl.DownloadInfoListAidl;
import com.shizhefei.download.aidl.DownloadListenerAidl;
import com.shizhefei.download.aidl.DownloadServerAidl;
import com.shizhefei.download.aidl.SpeedMonitorAidl;
import com.shizhefei.download.base.DownloadInfoList;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.manager.DownloadManager;

import java.util.List;

public class DownloadService extends Service {

    private RemoteCallbackList<DownloadListenerAidl> callbackList = new RemoteCallbackList<>();
    private DownloadManager downloadManager;

    @Override
    public void onCreate() {
        super.onCreate();
        downloadManager = DownloadManager.getLocal();
        downloadManager.registerDownloadListener(proxyDownloadListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return server;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        downloadManager.unregisterDownloadListener(proxyDownloadListener);
    }

    //由AIDL文件生成的BookManager
    private final DownloadServerAidl.Stub server = new DownloadServerAidl.Stub() {

        @Override
        public DownloadInfo findFirstByUrl(String url) throws RemoteException {
            return downloadManager.findFirst(url);
        }

        @Override
        public List<DownloadInfo> findByUrl(String url) throws RemoteException {
            return downloadManager.find(url);
        }

        @Override
        public DownloadInfo findFirstByUrlAndFileName(String url, String dir, String fileName) throws RemoteException {
            return downloadManager.findFirst(url, dir, fileName);
        }

        @Override
        public DownloadInfoListAidl createDownloadInfoListByStatus(int statusFlags) throws RemoteException {
            final DownloadInfoList downloadInfoList = downloadManager.createDownloadInfoList(statusFlags);
            return new DownloadInfoListAidl.Stub() {
                @Override
                public int getCount() throws RemoteException {
                    return downloadInfoList.getCount();
                }

                @Override
                public DownloadInfo getDownloadInfo(int position) throws RemoteException {
                    return downloadInfoList.getDownloadInfo(position);
                }

                @Override
                public int getPosition(long downloadId) throws RemoteException {
                    return downloadInfoList.getPosition(downloadId);
                }
            };
        }

        @Override
        public DownloadInfoListAidl createDownloadInfoList() throws RemoteException {
            final DownloadInfoList downloadInfoList = downloadManager.createDownloadInfoList();
            return new DownloadInfoListAidl.Stub() {
                @Override
                public int getCount() throws RemoteException {
                    return downloadInfoList.getCount();
                }

                @Override
                public DownloadInfo getDownloadInfo(int position) throws RemoteException {
                    return downloadInfoList.getDownloadInfo(position);
                }

                @Override
                public int getPosition(long downloadId) throws RemoteException {
                    return downloadInfoList.getPosition(downloadId);
                }
            };
        }

        @Override
        public long start(DownloadParams downloadParams) throws RemoteException {
            return downloadManager.start(downloadParams);
        }

        @Override
        public boolean resume(long downloadId) throws RemoteException {
            return downloadManager.resume(downloadId);
        }

        @Override
        public void pause(long downloadId) throws RemoteException {
            downloadManager.pause(downloadId);
        }

        @Override
        public void pauseAll() throws RemoteException {
            downloadManager.pauseAll();
        }

        @Override
        public void resumeAll() throws RemoteException {
            downloadManager.resumeAll();
        }

        @Override
        public void remove(long downloadId) throws RemoteException {
            downloadManager.remove(downloadId);
        }

        @Override
        public DownloadInfo getDownloadInfo(long downloadId) throws RemoteException {
            return downloadManager.getDownloadInfo(downloadId);
        }

        @Override
        public DownloadParams getDownloadParams(long downloadId) throws RemoteException {
            return downloadManager.getDownloadParams(downloadId);
        }

        @Override
        public SpeedMonitorAidl getSpeedMonitor() throws RemoteException {
            return new SpeedMonitorAidl.Stub() {
                @Override
                public long getTotalSpeed() throws RemoteException {
                    return downloadManager.getSpeedMonitor().getTotalSpeed();
                }

                @Override
                public long getSpeed(long downloadId) throws RemoteException {
                    return downloadManager.getSpeedMonitor().getSpeed(downloadId);
                }
            };
        }

        @Override
        public void registerDownloadListener(DownloadListenerAidl downloadListener) throws RemoteException {
            callbackList.register(downloadListener);
        }

        @Override
        public void unregisterDownloadListener(DownloadListenerAidl downloadListener) throws RemoteException {
            callbackList.unregister(downloadListener);
        }
    };

    private DownloadListener proxyDownloadListener = new DownloadListener() {

        @Override
        public void onPending(long downloadId) {
            final int n = callbackList.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    callbackList.getBroadcastItem(i).onPending(downloadId);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                callbackList.finishBroadcast();
            }
        }

        @Override
        public void onStart(long downloadId, long current, long total) {
            final int n = callbackList.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    callbackList.getBroadcastItem(i).onStart(downloadId, current, total);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                callbackList.finishBroadcast();
            }
        }

        @Override
        public void onProgressUpdate(long downloadId, long current, long total) {
            final int n = callbackList.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    callbackList.getBroadcastItem(i).onProgressUpdate(downloadId, current, total);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                callbackList.finishBroadcast();
            }
        }

        @Override
        public void onDownloadResetSchedule(long downloadId, int reason, long current, long total) {
            final int n = callbackList.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    callbackList.getBroadcastItem(i).onDownloadResetSchedule(downloadId, reason, current, total);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                callbackList.finishBroadcast();
            }
        }

        @Override
        public void onConnected(long downloadId, HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName, long current, long total) {
            final int n = callbackList.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    callbackList.getBroadcastItem(i).onConnected(downloadId, httpInfo, saveDir, saveFileName, tempFileName, current, total);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                callbackList.finishBroadcast();
            }
        }

        @Override
        public void onPaused(long downloadId) {
            final int n = callbackList.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    callbackList.getBroadcastItem(i).onPaused(downloadId);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                callbackList.finishBroadcast();
            }
        }

        @Override
        public void onComplete(long downloadId) {
            final int n = callbackList.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    callbackList.getBroadcastItem(i).onComplete(downloadId);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                callbackList.finishBroadcast();
            }
        }

        @Override
        public void onError(long downloadId, int errorCode, String errorMessage) {
            final int n = callbackList.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    callbackList.getBroadcastItem(i).onError(downloadId, errorCode, errorMessage);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                callbackList.finishBroadcast();
            }
        }

        @Override
        public void onRemove(long downloadId) {
            final int n = callbackList.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    callbackList.getBroadcastItem(i).onRemove(downloadId);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                callbackList.finishBroadcast();
            }
        }
    };
}
