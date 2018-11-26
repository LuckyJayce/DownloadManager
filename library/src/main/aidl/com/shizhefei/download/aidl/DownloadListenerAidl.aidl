package com.shizhefei.download.aidl;
import com.shizhefei.download.entity.HttpInfo;

interface DownloadListenerAidl {

     void onPending(long downloadId);

     void onStart(long downloadId, long current, long total);

     void onDownloadResetBegin(long downloadId, int reason, long current, long total);
   
     void onDownloadIng(long downloadId, long current, long total);
   
     void onConnected(long downloadId, in HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName, long current, long total);
   
     void onPaused(long downloadId);
   
     void onComplete(long downloadId);
   
     void onError(long downloadId, int errorCode, String errorMessage);
   
     void onRemove(long downloadId);
}