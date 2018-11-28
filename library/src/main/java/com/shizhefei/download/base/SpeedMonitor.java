package com.shizhefei.download.base;

public interface SpeedMonitor {
    long getTotalSpeed();

    long getSpeed(long downloadId);
}
