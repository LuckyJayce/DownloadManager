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
    private boolean acceptRange;

    private HttpInfo() {
    }


    protected HttpInfo(Parcel in) {
        httpCode = in.readInt();
        contentType = in.readString();
        contentLength = in.readLong();
        eTag = in.readString();
        acceptRange = in.readByte() != 0;
    }

    public boolean isAcceptRange() {
        return acceptRange;
    }

    private void setAcceptRange(boolean acceptRange) {
        this.acceptRange = acceptRange;
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
            jsonObject.put("acceptRange", acceptRange);
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(httpCode);
        dest.writeString(contentType);
        dest.writeLong(contentLength);
        dest.writeString(eTag);
        dest.writeByte((byte) (acceptRange ? 1 : 0));
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
                httpInfo.setAcceptRange(jsonObject.optBoolean("acceptRange", false));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setByInfo(HttpInfo info) {
            httpInfo.setContentLength(info.getContentLength());
            httpInfo.setContentType(info.getContentType());
            httpInfo.setETag(info.getETag());
            httpInfo.setHttpCode(info.getHttpCode());
            httpInfo.setAcceptRange(info.isAcceptRange());
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

        public void setAcceptRange(boolean acceptRange) {
            httpInfo.setAcceptRange(acceptRange);
        }

        public boolean isAcceptRange() {
            return httpInfo.isAcceptRange();
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
