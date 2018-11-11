package com.shizhefei.downloadmanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.shizhefei.downloadmanager.base.DownloadEntity;
import com.shizhefei.downloadmanager.base.DownloadListener;
import com.shizhefei.downloadmanager.base.DownloadParams;
import com.shizhefei.downloadmanager.imp.DownloadManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DownloadParams downloadParams = new DownloadParams();
        DownloadManager.getLocal().start(downloadParams, new DownloadListener() {
            @Override
            public void onComplete(long downloadId) {
                DownloadEntity entity = DownloadManager.getLocal().getDownloadEntity(downloadId);
                DownloadParams params = DownloadManager.getLocal().getDownloadParams(downloadId);
            }
        });

    }
}
