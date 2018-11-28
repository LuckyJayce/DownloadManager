// SpeedMonitorAidl.aidl
package com.shizhefei.download.aidl;

interface SpeedMonitorAidl {
    long getTotalSpeed();

    long getSpeed(long downloadId);
}
