package com.shizhefei.download.task.m3u8;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class M3u8ExtInfo {
    private ItemInfo m3u8Info;
    private ItemInfo currentItemInfo;
    private JSONObject jsonObject;
    private JSONObject m3u8ItemInfoJSONObject;
    private JSONObject currentItemInfoJSONObject;

    public M3u8ExtInfo(String extInfo) {
        if (!TextUtils.isEmpty(extInfo)) {
            try {
                jsonObject = new JSONObject(extInfo);
                m3u8ItemInfoJSONObject = jsonObject.optJSONObject("m3u8ItemInfo");
                m3u8Info = toItemInfo(m3u8ItemInfoJSONObject);
                currentItemInfoJSONObject = jsonObject.optJSONObject("currentItemInfo");
                currentItemInfo = toItemInfo(currentItemInfoJSONObject);
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
        itemInfo.setEtag(jsonObject.optString("eTag"));
        itemInfo.setAcceptRange(jsonObject.optBoolean("isAcceptRange"));
        return itemInfo;
    }

    private void fillToJSONObject(JSONObject jsonObject, ItemInfo itemInfo) throws JSONException {
        jsonObject.put("url", itemInfo.getUrl());
        jsonObject.put("current", itemInfo.getCurrent());
        jsonObject.put("total", itemInfo.getTotal());
        jsonObject.put("fileName", itemInfo.getFileName());
        jsonObject.put("tempFileName", itemInfo.getTempFileName());
        jsonObject.put("index", itemInfo.getIndex());
        jsonObject.put("isAcceptRange", itemInfo.isAcceptRange());
        jsonObject.put("eTag", itemInfo.getEtag());
    }

    public ItemInfo getM3u8Info() {
        return m3u8Info;
    }

    public void setM3u8Info(ItemInfo m3u8Info) {
        this.m3u8Info = m3u8Info;
    }

    public ItemInfo getCurrentItemInfo() {
        return currentItemInfo;
    }

    public void setCurrentItemInfo(ItemInfo currentItemInfo) {
        this.currentItemInfo = currentItemInfo;
    }

    public String getJson() {
        try {
            if (jsonObject == null) {
                jsonObject = new JSONObject();
            }
            if (currentItemInfo == null) {
                jsonObject.remove("currentItemInfo");
            } else {
                if (currentItemInfoJSONObject == null) {
                    currentItemInfoJSONObject = new JSONObject();
                }
                fillToJSONObject(currentItemInfoJSONObject, currentItemInfo);
                jsonObject.put("currentItemInfo", currentItemInfoJSONObject);
            }
            if (m3u8Info == null) {
                jsonObject.remove("m3u8ItemInfo");
            } else {
                if (m3u8ItemInfoJSONObject == null) {
                    m3u8ItemInfoJSONObject = new JSONObject();
                }
                fillToJSONObject(m3u8ItemInfoJSONObject, m3u8Info);
                jsonObject.put("m3u8ItemInfo", m3u8ItemInfoJSONObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static class ItemInfo {
        private int index;
        private String url;
        private long current;
        private long total;
        private String fileName;
        private String tempFileName;
        private boolean isAcceptRange;
        private String etag;

        public boolean isAcceptRange() {
            return isAcceptRange;
        }

        public void setAcceptRange(boolean acceptRange) {
            isAcceptRange = acceptRange;
        }

        public String getEtag() {
            return etag;
        }

        public void setEtag(String etag) {
            this.etag = etag;
        }

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
                    "index=" + index +
                    ", url='" + url + '\'' +
                    ", current=" + current +
                    ", total=" + total +
                    ", fileName='" + fileName + '\'' +
                    ", tempFileName='" + tempFileName + '\'' +
                    ", isAcceptRange=" + isAcceptRange +
                    ", etag='" + etag + '\'' +
                    '}';
        }
    }
}
