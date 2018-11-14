package com.shizhefei.download.aidl;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.aidl.DownloadListenerAidl;

interface DownloadServerAidl {

       DownloadInfo findFirstByUrl(String url);

       List<DownloadInfo> findByUrl(String url);

       DownloadInfo findFirstByUrlAndFileName(String url, String dir, String fileName);

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
       boolean restartPauseOrFail(long downloadId);

       /**
        * 停止下载任务
        *
        * @param downloadId
        */
       void pause(long downloadId);

       void pauseAll();

       /**
        * 停止并删除下载任务
        *
        * @param downloadId
        */
       void remove(long downloadId);

       DownloadInfo getDownloadEntity(long downloadId);

       DownloadParams getDownloadParams(long downloadId);

       void registerDownloadListener(in DownloadListenerAidl downloadListener);

       void unregisterDownloadListener(in DownloadListenerAidl downloadListener);

       int getCount();

       DownloadInfo getDownloadInfo(int position);

       int getPosition(long downloadId);
}
