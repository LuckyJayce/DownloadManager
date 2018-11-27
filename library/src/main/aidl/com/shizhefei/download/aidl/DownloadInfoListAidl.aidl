package com.shizhefei.download.aidl;

import com.shizhefei.download.entity.DownloadInfo;

interface DownloadInfoListAidl {
    int getCount();

    DownloadInfo getDownloadInfo(int position);

    int getPosition(long downloadId);
}
