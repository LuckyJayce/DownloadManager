package com.shizhefei.download.base;

import com.shizhefei.download.entity.DownloadParams;

public interface IdGenerator {
    long generateId(DownloadParams downloadParams);
}
