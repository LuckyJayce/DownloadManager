package com.shizhefei.download.task.base;

import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.task.DownloadProgressSenderProxy;
import com.shizhefei.mvc.ProgressSender;
import com.shizhefei.task.ITask;

public class DownloadTask implements ITask<Void> {
    private final long current;
    private final long total;
    private final long downloadId;
    private DownloadTaskImp taskImp;

    public DownloadTask(long downloadId, DownloadInfo downloadInfo) {
        current = downloadInfo.getCurrent();
        total = downloadInfo.getTotal();
        this.downloadId = downloadId;
        DownloadParams downloadParams = downloadInfo.getDownloadParams();
        taskImp = new DownloadTaskImp(downloadId, downloadParams, downloadParams.getUrl(), downloadInfo.getDir(), downloadInfo.getCurrent(), downloadInfo.getTotal(), downloadInfo.getFileName(), downloadInfo.getTempFileName(), downloadInfo.getHttpInfo().isAcceptRange(), downloadInfo.getHttpInfo().getETag());
    }

    public void onRemove() {
        taskImp.onRemove();
    }

    @Override
    public Void execute(ProgressSender progressSender) throws Exception {
        final DownloadProgressSenderProxy downloadProgressSenderProxy = new DownloadProgressSenderProxy(downloadId, progressSender);
        downloadProgressSenderProxy.sendStart(current, total);
        return taskImp.execute(new DownloadProgressListener() {
            @Override
            public void onDownloadResetSchedule(long downloadId, int reason, long current, long total) {
                downloadProgressSenderProxy.sendDownloadResetSchedule(current, total, reason);
            }

            @Override
            public void onConnected(long downloadId, HttpInfo info, String saveDir, String saveFileName, String tempFileName, long current, long total) {
                downloadProgressSenderProxy.sendConnected(info, saveDir, saveFileName, tempFileName, current, total);
            }

            @Override
            public void onDownloadIng(long downloadId, long current, long total) {
                downloadProgressSenderProxy.sendDownloading(current, total);
            }
        });
    }

    @Override
    public void cancel() {
        taskImp.cancel();
    }
}
