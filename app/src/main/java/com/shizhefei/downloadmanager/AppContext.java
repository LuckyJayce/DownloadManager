package com.shizhefei.downloadmanager;

import android.app.Application;

import com.shizhefei.download.entity.DownloadConfig;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.manager.DownloadManager;

public class AppContext extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DownloadConfig.Builder builder = new DownloadConfig.Builder().setWifiRequired(false);
        DownloadManager.init(this,  builder.build());
        DownloadManager.getRemote().bindService();
    }
}
