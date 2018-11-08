package com.shizhefei.downloadmanager.imp;

import com.shizhefei.downloadmanager.DownloadProgressSender;
import com.shizhefei.downloadmanager.base.AbsDownloadTask;
import com.shizhefei.downloadmanager.base.DownloadEntity;
import com.shizhefei.downloadmanager.base.DownloadParams;

public class DownloadTask extends AbsDownloadTask {
    private DownloadParams downloadParams;
    private DownloadEntity downloadEntity;

    public DownloadParams getDownloadParams() {
        return downloadParams;
    }

    public DownloadEntity getDownloadEntity() {
        return downloadEntity;
    }


    public DownloadTask(DownloadParams downloadParams) {
        this.downloadParams = downloadParams;
    }

    @Override
    public void execute(DownloadProgressSender sender) throws Exception {

    }

    @Override
    public void cancel() {

    }
}
