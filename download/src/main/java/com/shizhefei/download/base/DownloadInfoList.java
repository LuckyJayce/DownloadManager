package com.shizhefei.download.base;

import com.shizhefei.download.entity.DownloadInfo;

public interface DownloadInfoList {
    int getCount();

    DownloadInfo getDownloadInfo(int position);

    int getPosition(long downloadId);
}
