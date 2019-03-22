package com.shizhefei.download.aidl;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.aidl.DownloadListenerAidl;
import com.shizhefei.download.aidl.DownloadInfoListAidl;
import com.shizhefei.download.aidl.SpeedMonitorAidl;

interface DownloadServerAidl {

       oneway void setIsWifiRequired(long downloadId, boolean isWifiRequired);

       DownloadInfo findFirstByUrl(String url);

       List<DownloadInfo> findByUrl(String url);

       DownloadInfo findFirstByUrlAndFileName(String url, String dir, String fileName);

       DownloadInfoListAidl createDownloadInfoListByStatus(int statusFlags);

       DownloadInfoListAidl createDownloadInfoList();

       /**
        * @param downloadParams
        * @param downloadListener
        * @return 返回下载的id
        */
       long start(in DownloadParams downloadParams);

       /**
        * 执行之前失败,停止且现在未在执行中的任务
        * 如果已经在执行则不会重新执行
        *
        * @param downloadId
        * @return
        */
       boolean resume(long downloadId);

       oneway void resumeAll();

       /**
        * 停止下载任务
        *
        * @param downloadId
        */
       oneway void pause(long downloadId);

       oneway void pauseAll();

       /**
        * 停止并删除下载任务
        *
        * @param downloadId
        */
       oneway void remove(long downloadId);

	   oneway void removeAll();

	   oneway void removeByStatus(int statusFlags);

       DownloadInfo getDownloadInfo(long downloadId);

       DownloadParams getDownloadParams(long downloadId);

       SpeedMonitorAidl getSpeedMonitor();

       oneway void registerDownloadListener(in DownloadListenerAidl downloadListener);

       oneway void unregisterDownloadListener(in DownloadListenerAidl downloadListener);
}
