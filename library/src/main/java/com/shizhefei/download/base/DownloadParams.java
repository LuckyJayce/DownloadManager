package com.shizhefei.download.base;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class DownloadParams {
    private String url;
    private String dir;
    private String fileName;
    private boolean override;
    private Map<String, List<String>> params;
    private Map<String, List<String>> headers;
    private int blockSize;
    private boolean isWifiRequired;
    //额外的一些自定义数据，方便使用者自定义，存储数据库时序列化成json，这里只存基本参数
    private JSONObject exInfo = new JSONObject();

    public boolean isWifiRequired() {
        return isWifiRequired;
    }

    public void setWifiRequired(boolean wifiRequired) {
        isWifiRequired = wifiRequired;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isOverride() {
        return override;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * 设置下载保存文件名
     *
     * @param fileName   下载保存的文件名
     * @param isOverride 是否覆盖已有的文件
     */
    public void setSaveFileName(String fileName, boolean isOverride) {
        this.fileName = fileName;
        this.override = isOverride;
    }

    public void setSaveFileName(String fileName) {
        this.fileName = fileName;
        this.override = false;
    }

    public Map<String, List<String>> getParams() {
        return params;
    }

    public void setParams(Map<String, List<String>> params) {
        this.params = params;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public void putExInfo(String key, int value) {
        try {
            exInfo.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void putExInfo(String key, long value) {

    }

    public String toJson(){
        return null;
    }

    public void setByJson(String json) {
    }
}
