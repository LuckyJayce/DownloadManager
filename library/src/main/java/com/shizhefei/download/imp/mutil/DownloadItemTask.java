package com.shizhefei.download.imp.mutil;

import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.entity.DownloadItem;
import com.shizhefei.download.base.DownloadParams;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.mvc.ProgressSender;
import com.shizhefei.task.ITask;

public class DownloadItemTask implements ITask<Void> {
    private final long downloadId;
    private final DownloadDB downloadDB;
    private final DownloadInfo entity;
    private long itemOffset;
    private long itemEnd;
    private DownloadParams downloadParams;
    private DownloadInfo downloadInfo;

    public DownloadItemTask(long downloadId, DownloadParams downloadParams, DownloadInfo entity, DownloadItem downloadItem, DownloadDB downloadDB) {
        this.downloadId = downloadId;
        this.downloadParams = downloadParams;
        this.downloadDB = downloadDB;
        this.entity = entity;
    }

    public DownloadItemTask(long downloadId, DownloadParams downloadParams, DownloadInfo entity, long itemOffset, long itemEnd, DownloadDB downloadDB) {
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
