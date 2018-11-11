package com.shizhefei.download.imp.mutil;

import com.shizhefei.download.DownloadProgressSender;
import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.base.DownloadEntity;
import com.shizhefei.download.base.DownloadParams;
import com.shizhefei.download.base.IDownloadDB;

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
