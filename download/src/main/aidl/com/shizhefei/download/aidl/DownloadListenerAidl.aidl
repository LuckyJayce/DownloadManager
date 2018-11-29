package com.shizhefei.download.aidl;
import com.shizhefei.download.entity.HttpInfo;

interface DownloadListenerAidl {

     oneway void onPending(long downloadId);

     oneway void onStart(long downloadId, long current, long total);

     oneway void onDownloadResetSchedule(long downloadId, int reason, long current, long total);
   
     oneway void onProgressUpdate(long downloadId, long current, long total);
   
     oneway void onConnected(long downloadId, in HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName, long current, long total);
   
     oneway void onPaused(long downloadId);
   
     oneway void onComplete(long downloadId);
   
     oneway void onError(long downloadId, int errorCode, String errorMessage);
   
     oneway void onRemove(long downloadId);
}