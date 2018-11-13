package com.shizhefei.downloadmanager;

import android.app.Application;

import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.base.DownloadParams;
import com.shizhefei.download.base.DownloadTaskFactory;
import com.shizhefei.download.base.IdGenerator;
import com.shizhefei.download.imp.DownloadConfig;
import com.shizhefei.download.imp.DownloadManager;

public class AppContext extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DownloadConfig.Builder builder = new DownloadConfig.Builder().setWifiRequired(false);
        DownloadManager.init(this, builder.build());
    }
}
