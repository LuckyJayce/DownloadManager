package com.shizhefei.download.base;

import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.entity.DownloadInfo;
import java.util.concurrent.Executor;

public interface DownloadTaskFactory {
    AbsDownloadTask buildDownloadTask(long downloadId, boolean isOnlyRemove, DownloadInfo.Agency downloadInfoAgency, DownloadDB downloadDB, Executor executor);
}
