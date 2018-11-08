package com.shizhefei.downloadmanager.base;

import com.shizhefei.downloadmanager.DownloadProgressSender;
import com.shizhefei.mvc.ProgressSender;
import com.shizhefei.task.ITask;

public abstract class AbsDownloadTask implements ITask<Void> {

    @Override
    public Void execute(ProgressSender progressSender) throws Exception {
        return null;
    }

    public abstract void execute(DownloadProgressSender sender) throws Exception;
}
