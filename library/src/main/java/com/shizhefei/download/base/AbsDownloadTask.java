package com.shizhefei.download.base;

import com.shizhefei.download.DownloadProgressSender;
import com.shizhefei.mvc.ProgressSender;
import com.shizhefei.task.ITask;

import java.net.HttpURLConnection;

public abstract class AbsDownloadTask implements ITask<Void> {

    public abstract DownloadParams getDownloadParams();

    public abstract DownloadEntity getDownloadEntity();

    @Override
    public Void execute(ProgressSender progressSender) throws Exception {
        return null;
    }

    public abstract void execute(DownloadProgressSender sender) throws Exception;
}