package com.shizhefei.download.task.m3u8;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class M3u8ExtInfo {
    private ItemInfo m3u8Info;
    private ItemInfo currentItemInfo;
    private JSONObject jsonObject;

    public M3u8ExtInfo(String extInfo) {
        if (!TextUtils.isEmpty(extInfo)) {
            try {
                jsonObject = new JSONObject(extInfo);
                m3u8Info = toItemInfo(jsonObject.optJSONObject("m3u8ItemInfo"));
                currentItemInfo = toItemInfo(jsonObject.optJSONObject("currentItemInfo"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private ItemInfo toItemInfo(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        ItemInfo itemInfo = new ItemInfo();
        itemInfo.setUrl(jsonObject.optString("url"));
        itemInfo.setCurrent(jsonObject.optLong("current"));
        itemInfo.setTotal(jsonObject.optLong("total"));
        itemInfo.setFileName(jsonObject.optString("fileName"));
        itemInfo.setTempFileName(jsonObject.optString("tempFileName"));
        itemInfo.setIndex(jsonObject.optInt("index"));
        return itemInfo;
    }

    private JSONObject toJson(ItemInfo itemInfo) throws JSONException {
        if (itemInfo == null) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url", itemInfo.getUrl());
        jsonObject.put("current", itemInfo.getCurrent());
        jsonObject.put("total", itemInfo.getTotal());
        jsonObject.put("fileName", itemInfo.getFileName());
        jsonObject.put("tempFileName", itemInfo.getTempFileName());
        jsonObject.put("index", itemInfo.getIndex());
        return jsonObject;
    }

    public ItemInfo getM3u8Info() {
        return m3u8Info;
    }

    public void setM3u8Info(ItemInfo m3u8Info) {
        this.m3u8Info = m3u8Info;
        try {
            if (m3u8Info == null) {
                jsonObject.remove("m3u8ItemInfo");
            } else {
                jsonObject.put("m3u8ItemInfo", toJson(m3u8Info));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ItemInfo getCurrentItemInfo() {
        return currentItemInfo;
    }

    public void setCurrentItemInfo(ItemInfo currentItemInfo) {
        this.currentItemInfo = currentItemInfo;
        try {
            if (currentItemInfo == null) {
                jsonObject.remove("currentItemInfo");
            } else {
                jsonObject.put("currentItemInfo", toJson(currentItemInfo));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getJson() {
        return jsonObject.toString();
    }

    public static class ItemInfo {
        private String url;
        private long current;
        private long total;
        private String fileName;
        private String tempFileName;
        private int index;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public long getCurrent() {
            return current;
        }

        public void setCurrent(long current) {
            this.current = current;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getTempFileName() {
            return tempFileName;
        }

        public void setTempFileName(String tempFileName) {
            this.tempFileName = tempFileName;
        }

        @Override
        public String toString() {
            return "ItemInfo{" +
                    "url='" + url + '\'' +
                    ", current=" + current +
                    ", total=" + total +
                    ", fileName='" + fileName + '\'' +
                    ", tempFileName='" + tempFileName + '\'' +
                    ", index=" + index +
                    '}';
        }
    }
}
