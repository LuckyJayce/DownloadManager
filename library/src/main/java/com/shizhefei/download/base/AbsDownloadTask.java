package com.shizhefei.download.base;

import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.task.IAsyncTask;

public abstract class AbsDownloadTask implements IAsyncTask<Void> {

    public abstract DownloadInfo getDownloadInfo();

    public abstract String getDownloadTaskName();
}
