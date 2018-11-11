package com.shizhefei.downloadmanager.imp.mutil;

import com.shizhefei.downloadmanager.DownloadProgressSender;
import com.shizhefei.downloadmanager.base.AbsDownloadTask;
import com.shizhefei.downloadmanager.base.DownloadEntity;
import com.shizhefei.downloadmanager.base.DownloadParams;
import com.shizhefei.downloadmanager.base.IDownloadDB;

public class DownloadMutilTask extends AbsDownloadTask {
    private final long downloadId;
    private final IDownloadDB downloadDB;
    private DownloadParams downloadParams;
    private DownloadEntity downloadEntity;

    public DownloadMutilTask(long downloadId, DownloadParams downloadParams, IDownloadDB downloadDB) {
        this.downloadId = downloadId;
        this.downloadParams = downloadParams;
        this.downloadDB = downloadDB;
        downloadEntity = downloadDB.find(downloadId);
        if (downloadEntity == null) {
            downloadEntity = buildEntity(downloadParams);
        }
    }

    @Override
    public DownloadParams getDownloadParams() {
        return null;
    }

    @Override
    public DownloadEntity getDownloadEntity() {
        return null;
    }

    @Override
    public void execute(DownloadProgressSender sender) throws Exception {

    }

    @Override
    public void cancel() {

    }

    private DownloadEntity buildEntity(DownloadParams downloadParams) {
        //TODO
        return new DownloadEntity();
    }
}
