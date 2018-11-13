package com.shizhefei.download.imp.mutil;


import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.base.DownloadParams;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.mvc.RequestHandle;
import com.shizhefei.mvc.ResponseSender;

public class DownloadMutilTask extends AbsDownloadTask {

    @Override
    public DownloadParams getDownloadParams() {
        return null;
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return null;
    }

    @Override
    public RequestHandle execute(ResponseSender<Void> sender) throws Exception {
        return null;
    }
}
