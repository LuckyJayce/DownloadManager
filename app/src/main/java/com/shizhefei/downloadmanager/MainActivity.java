package com.shizhefei.downloadmanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.base.DownloadParams;
import com.shizhefei.download.imp.DownloadManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DownloadParams downloadParams = new DownloadParams();
        long start = DownloadManager.getLocal().start(downloadParams, new DownloadListener() {
            @Override
            public void onComplete(long downloadId) {
                DownloadInfo entity = DownloadManager.getLocal().getDownloadEntity(downloadId);
                DownloadParams params = DownloadManager.getLocal().getDownloadParams(downloadId);
            }
        });

    }
}
