package com.shizhefei.download.entity;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.shizhefei.download.manager.DownloadManager;
import com.shizhefei.download.utils.DownloadUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO url.openConnection(proxy)
 */
public class DownloadParams implements Parcelable {
    private String url;
    private String dir;
    private String fileName;
    private boolean override;
    private Map<String, List<String>> params;
    private Map<String, List<String>> headers;
    private int blockSize;
    private boolean isWifiRequired;
    private long totalSize;
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
        totalSize = in.readLong();
        try {
            params = DownloadUtils.parseJson(new JSONObject(in.readString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            headers = DownloadUtils.parseJson(new JSONObject(in.readString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            extData = DownloadUtils.parseJson2(new JSONObject(in.readString()));
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

    public Map<String, String> getExtDataMap() {
        return extData;
    }

    public String getExtData(String key) {
        return extData.get(key);
    }

    void setExtData(Map<String, String> extData) {
        this.extData = extData;
    }

    public long getTotalSize() {
        return totalSize;
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

    void setUrl(String url) {
        this.url = url;
    }

    public boolean isOverride() {
        return override;
    }

    public String getDir() {
        return dir;
    }

    void setDir(String dir) {
        this.dir = dir;
    }

    public String getFileName() {
        return fileName;
    }

    void setFileName(String fileName) {
        this.fileName = fileName;
        this.override = false;
    }

    public Map<String, List<String>> getParams() {
        return params;
    }

    void setParams(Map<String, List<String>> params) {
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
            jsonObject.put("totalSize", totalSize);
            jsonObject.put("params", DownloadUtils.toJsonObject(params));
            jsonObject.put("headers", DownloadUtils.toJsonObject(headers));
            jsonObject.put("extData", DownloadUtils.toJsonObject2(extData));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(dir);
        dest.writeString(fileName);
        dest.writeByte((byte) (override ? 1 : 0));
        dest.writeInt(blockSize);
        dest.writeByte((byte) (isWifiRequired ? 1 : 0));
        dest.writeLong(totalSize);
        dest.writeString(DownloadUtils.toJsonObject(params).toString());
        dest.writeString(DownloadUtils.toJsonObject(headers).toString());
        dest.writeString(DownloadUtils.toJsonObject2(extData).toString());
    }

    @Override
    public String toString() {
        return "DownloadParams{" +
                "url='" + url + '\'' +
                ", dir='" + dir + '\'' +
                ", fileName='" + fileName + '\'' +
                ", override=" + override +
                ", params=" + params +
                ", headers=" + headers +
                ", blockSize=" + blockSize +
                ", isWifiRequired=" + isWifiRequired +
                ", totalSize=" + totalSize +
                ", extData=" + extData +
                '}';
    }

    public static final class Builder {
        private String url;
        private String dir;
        private String fileName;
        private boolean override;
        private Map<String, List<String>> params;
        private Map<String, List<String>> headers;
        private int blockSize;
        private long totalSize;
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
            addHeader("User-Agent", downloadConfig.getUserAgent());
        }

        public Builder(String databaseParamsJson) {
            try {
                JSONObject jsonObject = new JSONObject(databaseParamsJson);
                this.url = jsonObject.optString("url");
                this.dir = jsonObject.optString("dir");
                this.fileName = jsonObject.optString("fileName");
                this.override = jsonObject.optBoolean("override");
                this.isWifiRequired = jsonObject.optBoolean("isWifiRequired");
                this.blockSize = jsonObject.optInt("blockSize");
                this.totalSize = jsonObject.optLong("totalSize");
                this.params = DownloadUtils.parseJson(jsonObject.optJSONObject("params"));
                this.headers = DownloadUtils.parseJson(jsonObject.optJSONObject("headers"));
                this.extData = DownloadUtils.parseJson2(jsonObject.optJSONObject("extData"));
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

        public Builder setTotalSize(long totalSize) {
            this.totalSize = totalSize;
            return this;
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

        public Builder addExtData(String key, String value) {
            extData.put(key, value);
            return this;
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
            downloadParams.totalSize = totalSize;
            return downloadParams;
        }
    }
}
