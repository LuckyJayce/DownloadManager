package com.shizhefei.download.imp.mutil;

import com.shizhefei.download.base.DownloadEntity;
import com.shizhefei.download.base.DownloadItem;
import com.shizhefei.download.base.DownloadParams;
import com.shizhefei.download.base.IDownloadDB;
import com.shizhefei.mvc.ProgressSender;
import com.shizhefei.task.ITask;

public class DownloadItemTask implements ITask<Void> {
    private final long downloadId;
    private final IDownloadDB downloadDB;
    private final DownloadEntity entity;
    private long itemOffset;
    private long itemEnd;
    private DownloadParams downloadParams;
    private DownloadEntity downloadEntity;

    public DownloadItemTask(long downloadId, DownloadParams downloadParams, DownloadEntity entity, DownloadItem downloadItem, IDownloadDB downloadDB) {
        this.downloadId = downloadId;
        this.downloadParams = downloadParams;
        this.downloadDB = downloadDB;
        this.entity = entity;
    }

    public DownloadItemTask(long downloadId, DownloadParams downloadParams, DownloadEntity entity, long itemOffset, long itemEnd, IDownloadDB downloadDB) {
        this.downloadId = downloadId;
        this.downloadParams = downloadParams;
        this.downloadDB = downloadDB;
        this.entity = entity;
        // createDownloadItem
    }

    @Override
    public Void execute(ProgressSender progressSender) throws Exception {
        return null;
    }

    @Override
    public void cancel() {

    }
}