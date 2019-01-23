package com.shizhefei.download.task.single;

import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.mvc.RequestHandle;
import com.shizhefei.mvc.ResponseSender;
import com.shizhefei.task.function.Func1;
import com.shizhefei.task.tasks.Tasks;

import java.util.concurrent.Executor;

public class SingleThreadDownloadTask extends AbsDownloadTask {
    private final SingleThreadDownloadImp downloadTask;
    private final Executor executor;
    public static final String DOWNLOAD_TASK_NAME = "SingleThreadDownloadTask";

    public SingleThreadDownloadTask(long downloadId, DownloadInfo.Agency downloadInfoAgency, DownloadDB downloadDB, Executor executor, boolean isOnlyRemove) {
       this(downloadId, downloadInfoAgency, downloadDB, executor, isOnlyRemove, null);
    }

    public SingleThreadDownloadTask(long downloadId, DownloadInfo.Agency downloadInfoAgency, DownloadDB downloadDB, Executor executor, boolean isOnlyRemove, Func1<String, String> transformRealUrl) {
        this.executor = executor;
        downloadTask = new SingleThreadDownloadImp(downloadId, downloadInfoAgency, downloadDB, isOnlyRemove, transformRealUrl);
    }

    @Override
    public RequestHandle execute(ResponseSender<Void> sender) throws Exception {
        return Tasks.async(downloadTask, executor).execute(sender);
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return downloadTask.getDownloadInfo();
    }

    @Override
    public String getDownloadTaskName() {
        return DOWNLOAD_TASK_NAME;
    }

    @Override
    public void remove() {
        downloadTask.remove();
    }
}
