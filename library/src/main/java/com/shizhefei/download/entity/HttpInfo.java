package com.shizhefei.download.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class HttpInfo implements Parcelable {
    private int httpCode;
    private String contentType;
    private long contentLength;
    private String eTag;

    private HttpInfo() {
    }

    protected HttpInfo(Parcel in) {
        httpCode = in.readInt();
        contentType = in.readString();
        contentLength = in.readLong();
        eTag = in.readString();
    }

    public String getETag() {
        return eTag;
    }

    private void setETag(String eTag) {
        this.eTag = eTag;
    }


    public int getHttpCode() {
        return httpCode;
    }

    private void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public String getContentType() {
        return contentType;
    }

    private void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    private void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("httpCode", httpCode);
            jsonObject.put("contentType", contentType);
            jsonObject.put("contentLength", contentLength);
            jsonObject.put("eTag", eTag);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static final Creator<HttpInfo> CREATOR = new Creator<HttpInfo>() {
        @Override
        public HttpInfo createFromParcel(Parcel in) {
            return new HttpInfo(in);
        }

        @Override
        public HttpInfo[] newArray(int size) {
            return new HttpInfo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(httpCode);
        dest.writeString(contentType);
        dest.writeLong(contentLength);
        dest.writeString(eTag);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static class Agency {
        private HttpInfo httpInfo;

        public Agency() {
            this.httpInfo = new HttpInfo();
        }

        public void setByJson(String json) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                httpInfo.setHttpCode(jsonObject.optInt("httpCode"));
                httpInfo.setContentType(jsonObject.optString("contentType"));
                httpInfo.setContentLength(jsonObject.optLong("contentLength"));
                httpInfo.setETag(jsonObject.optString("eTag"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public HttpInfo getInfo() {
            return httpInfo;
        }

        public String getETag() {
            return httpInfo.getETag();
        }

        public void setETag(String eTag) {
            httpInfo.setETag(eTag);
        }

        public int getHttpCode() {
            return httpInfo.getHttpCode();
        }

        public void setHttpCode(int httpCode) {
            httpInfo.setHttpCode(httpCode);
        }

        public String getContentType() {
            return httpInfo.getContentType();
        }

        public void setContentType(String contentType) {
            httpInfo.setContentType(contentType);
        }

        public long getContentLength() {
            return httpInfo.getContentLength();
        }

        public void setContentLength(long contentLength) {
            httpInfo.setContentLength(contentLength);
        }
    }
}
