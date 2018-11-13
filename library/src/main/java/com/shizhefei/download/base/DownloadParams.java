package com.shizhefei.download.base;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.shizhefei.download.imp.DownloadConfig;
import com.shizhefei.download.imp.DownloadManager;
import com.shizhefei.download.utils.DownloadJsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadParams implements Parcelable {
    private String url;
    private String dir;
    private String fileName;
    private boolean override;
    private Map<String, List<String>> params;
    private Map<String, List<String>> headers;
    private int blockSize;
    private boolean isWifiRequired;
    //额外的一些自定义数据，方便使用者自定义，存储数据库时序列化成json，这里只存基本参数
    private Map<String, String> extData;

    private DownloadParams() {
    }

    protected DownloadParams(Parcel in) {
        url = in.readString();
        dir = in.readString();
        fileName = in.readString();
        override = in.readByte() != 0;
        blockSize = in.readInt();
        isWifiRequired = in.readByte() != 0;
        try {
            params = DownloadJsonUtils.parse(new JSONObject(in.readString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            headers = DownloadJsonUtils.parse(new JSONObject(in.readString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            extData = DownloadJsonUtils.parse2(new JSONObject(in.readString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (params == null) {
            params = new HashMap<>();
        }
        if (headers == null) {
            headers = new HashMap<>();
        }
        if (extData == null) {
            extData = new HashMap<>();
        }
    }

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

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("url", url);
            jsonObject.put("dir", dir);
            jsonObject.put("fileName", fileName);
            jsonObject.put("override", override);
            jsonObject.put("isWifiRequired", isWifiRequired);
            jsonObject.put("blockSize", blockSize);
            jsonObject.put("params", DownloadJsonUtils.toJsonObject(params));
            jsonObject.put("headers", DownloadJsonUtils.toJsonObject(headers));
            jsonObject.put("extData", DownloadJsonUtils.toJsonObject2(extData));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public static final Creator<DownloadParams> CREATOR = new Creator<DownloadParams>() {
        @Override
        public DownloadParams createFromParcel(Parcel in) {
            return new DownloadParams(in);
        }

        @Override
        public DownloadParams[] newArray(int size) {
            return new DownloadParams[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(dir);
        dest.writeString(fileName);
        dest.writeByte((byte) (override ? 1 : 0));
        dest.writeInt(blockSize);
        dest.writeByte((byte) (isWifiRequired ? 1 : 0));
        dest.writeString(DownloadJsonUtils.toJsonObject(params).toString());
        dest.writeString(DownloadJsonUtils.toJsonObject(headers).toString());
        dest.writeString(DownloadJsonUtils.toJsonObject2(extData).toString());
    }


    public static final class Builder {
        private String url;
        private String dir;
        private String fileName;
        private boolean override;
        private Map<String, List<String>> params;
        private Map<String, List<String>> headers;
        private int blockSize;
        private boolean isWifiRequired;
        //额外的一些自定义数据，方便使用者自定义，存储数据库时序列化成json，这里只存基本参数
        private Map<String, String> extData;

        public Builder() {
            params = new HashMap<>();
            headers = new HashMap<>();
            extData = new HashMap<>();
            DownloadConfig downloadConfig = DownloadManager.getDownloadConfig();
            dir = downloadConfig.getDir();
            isWifiRequired = downloadConfig.isWifiRequired();
            blockSize = downloadConfig.getBlockSize();
        }

        public Builder(String json) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                this.url = jsonObject.optString("url");
                this.dir = jsonObject.optString("dir");
                this.fileName = jsonObject.optString("fileName");
                this.override = jsonObject.optBoolean("override");
                this.isWifiRequired = jsonObject.optBoolean("isWifiRequired");
                this.blockSize = jsonObject.optInt("blockSize");
                this.params = DownloadJsonUtils.parse(jsonObject.optJSONObject("params"));
                this.headers = DownloadJsonUtils.parse(jsonObject.optJSONObject("headers"));
                this.extData = DownloadJsonUtils.parse2(jsonObject.optJSONObject("extData"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (this.params == null) {
                this.params = new HashMap<>();
            }
            if (this.headers == null) {
                this.headers = new HashMap<>();
            }
            if (this.extData == null) {
                this.extData = new HashMap<>();
            }
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setDir(String dir) {
            this.dir = dir;
            return this;
        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder setOverride(boolean override) {
            this.override = override;
            return this;
        }

        public Builder addParam(String name, String value) {
            List<String> values = params.get(name);
            if (values == null) {
                values = new ArrayList<>(1);
                params.put(name, values);
            }
            values.add(value);
            return this;
        }

        public Builder addHeader(String name, String value) {
            List<String> values = headers.get(name);
            if (values == null) {
                values = new ArrayList<>(1);
                headers.put(name, values);
            }
            values.add(value);
            return this;
        }

        public Builder setBlockSize(int blockSize) {
            this.blockSize = blockSize;
            return this;
        }

        public Builder setIsWifiRequired(boolean isWifiRequired) {
            this.isWifiRequired = isWifiRequired;
            return this;
        }

        public void addExtData(String key, String value) {
            extData.put(key, value);
        }

        public DownloadParams build() {
            if (TextUtils.isEmpty(dir)) {
                throw new RuntimeException("download dir null");
            }
            DownloadParams downloadParams = new DownloadParams();
            downloadParams.setUrl(url);
            downloadParams.setDir(dir);
            downloadParams.setParams(params);
            downloadParams.setHeaders(headers);
            downloadParams.setBlockSize(blockSize);
            downloadParams.fileName = this.fileName;
            downloadParams.extData = this.extData;
            downloadParams.override = this.override;
            downloadParams.isWifiRequired = this.isWifiRequired;
            return downloadParams;
        }
    }
}
