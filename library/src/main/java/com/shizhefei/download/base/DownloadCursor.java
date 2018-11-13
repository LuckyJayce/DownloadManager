package com.shizhefei.download.base;

import com.shizhefei.download.entity.DownloadInfo;

public interface DownloadCursor {
    int getCount();

    DownloadInfo getDownloadInfo(int position);

    int getPosition(long downloadId);
}
