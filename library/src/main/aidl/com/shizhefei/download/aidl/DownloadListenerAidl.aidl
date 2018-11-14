package com.shizhefei.download.aidl;
import com.shizhefei.download.entity.HttpInfo;

interface DownloadListenerAidl {

     void onStart(long downloadId);
   
     void onPending(long downloadId);
   
     void onDownloadResetBegin(long downloadId, int reason);
   
     void onDownloadIng(long downloadId, long current, long total);
   
     void onConnected(long downloadId, in HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName);
   
     void onPaused(long downloadId);
   
     void onComplete(long downloadId);
   
     void onError(long downloadId, int errorCode, String errorMessage);
   
     void onRemove(long downloadId);
}