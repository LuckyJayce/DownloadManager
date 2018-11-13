package com.shizhefei.download.entity;

public class DownloadItem {
    private long downloadId;
    private long itemId;//自增长id
    private long itemCurrent;
    private long itemOffset;
    private long itemEnd;
    private int itemStatus;

    public int getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(int itemStatus) {
        this.itemStatus = itemStatus;
    }

    public long getItemTotal() {
        return itemEnd - itemOffset;
    }

    public long getItemCurrent() {
        return itemCurrent;
    }

    public void setItemCurrent(long itemCurrent) {
        this.itemCurrent = itemCurrent;
    }

    public long getItemOffset() {
        return itemOffset;
    }

    public void setItemOffset(long itemOffset) {
        this.itemOffset = itemOffset;
    }

    public long getItemEnd() {
        return itemEnd;
    }

    public void setItemEnd(long itemEnd) {
        this.itemEnd = itemEnd;
    }
}
