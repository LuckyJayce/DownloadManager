package com.shizhefei.downloadmanager;

import android.app.Application;

import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.base.DownloadParams;
import com.shizhefei.download.base.DownloadTaskFactory;
import com.shizhefei.download.base.IdGenerator;
import com.shizhefei.download.imp.DownloadManager;

public class AppContext extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DownloadManager.init(this, new DownloadTaskFactory() {
            @Override
            public AbsDownloadTask buildDownloadTask(long downloadId, DownloadParams downloadParams) {
                return null;
            }

            @Override
            public AbsDownloadTask buildDownloadTask(long downloadId) {
                return null;
            }
        }, new IdGenerator() {
            @Override
            public long generateId(DownloadParams downloadParams) {
                return 0;
            }
        });
    }
}
