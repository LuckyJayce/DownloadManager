package com.shizhefei.downloadmanager;

public class Test {
    public static final int STATUS_PENDING = 1;//在队列中，还没开始
    public static final int STATUS_START = 1 << 1;//开始
    public static final int STATUS_CONNECTED = 1 << 2;//连接上服务器
    public static final int STATUS_DOWNLOAD_RESET_BEGIN = 1 << 3;//连接上服务器
    public static final int STATUS_DOWNLOAD_ING = 1 << 4;
    public static final int STATUS_PAUSED = 1 << 5;//连接上服务器
    public static final int STATUS_FINISHED = 1 << 6;
    public static final int STATUS_ERROR = 1 << 7;

    public static void main(String[] args) {
        System.out.println(Integer.toBinaryString(STATUS_PENDING));
        System.out.println(Integer.toBinaryString(STATUS_START));
        System.out.println(Integer.toBinaryString(STATUS_CONNECTED));
        System.out.println(Integer.toBinaryString(STATUS_DOWNLOAD_RESET_BEGIN));
        System.out.println(Integer.toBinaryString(STATUS_DOWNLOAD_ING));
        System.out.println(Integer.toBinaryString(STATUS_PAUSED));
        System.out.println(Integer.toBinaryString(STATUS_FINISHED));
        System.out.println(Integer.toBinaryString(STATUS_ERROR));

        System.out.println(Integer.toBinaryString(~STATUS_FINISHED));

        System.out.println(Integer.toBinaryString((~STATUS_FINISHED) & STATUS_FINISHED));
        System.out.println(Integer.toBinaryString((~STATUS_FINISHED) & STATUS_ERROR));
        System.out.println(Integer.toBinaryString((~STATUS_FINISHED) & STATUS_PAUSED));
    }
}
