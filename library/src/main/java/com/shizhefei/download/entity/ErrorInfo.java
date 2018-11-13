package com.shizhefei.download.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by luckyjayce on 18-3-14.
 */

public class ErrorInfo implements Parcelable {

    private int errorCode;
    private String errorMessage;
    private int httpCode;
    private String httpMessage;

    private ErrorInfo() {
    }

    protected ErrorInfo(Parcel in) {
        errorCode = in.readInt();
        errorMessage = in.readString();
        httpCode = in.readInt();
        httpMessage = in.readString();
    }

    public int getErrorCode() {
        return errorCode;
    }

    private void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getHttpCode() {
        return httpCode;
    }

    private void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public String getHttpMessage() {
        return httpMessage;
    }

    private void setHttpMessage(String httpMessage) {
        this.httpMessage = httpMessage;
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("errorCode", errorCode);
            jsonObject.put("errorMessage", errorMessage);
            jsonObject.put("httpCode", httpCode);
            jsonObject.put("httpMessage", httpMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static final Creator<ErrorInfo> CREATOR = new Creator<ErrorInfo>() {
        @Override
        public ErrorInfo createFromParcel(Parcel in) {
            return new ErrorInfo(in);
        }

        @Override
        public ErrorInfo[] newArray(int size) {
            return new ErrorInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(errorCode);
        dest.writeString(errorMessage);
        dest.writeInt(httpCode);
        dest.writeString(httpMessage);
    }

    public static class Agency {
        private ErrorInfo errorInfo;

        public Agency() {
            this.errorInfo = new ErrorInfo();
        }

        public void set(int errorCode, String errorMessage) {
            errorInfo.setErrorCode(errorCode);
            errorInfo.setErrorMessage(errorMessage);
        }

        public void setByJson(String json) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                errorInfo.setErrorCode(jsonObject.optInt("errorCode"));
                errorInfo.setErrorMessage(jsonObject.optString("errorMessage"));
                errorInfo.setHttpCode(jsonObject.optInt("httpCode"));
                errorInfo.setHttpMessage(jsonObject.optString("httpMessage"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public int getErrorCode() {
            return errorInfo.getErrorCode();
        }

        public void setErrorCode(int errorCode) {
            errorInfo.setErrorCode(errorCode);
        }

        public String getErrorMessage() {
            return errorInfo.getErrorMessage();
        }

        public void setErrorMessage(String errorMessage) {
            errorInfo.setErrorMessage(errorMessage);
        }

        public int getHttpCode() {
            return errorInfo.getHttpCode();
        }

        public void setHttpCode(int httpCode) {
            errorInfo.setHttpCode(httpCode);
        }

        public String getHttpMessage() {
            return errorInfo.getHttpMessage();
        }

        public void setHttpMessage(String httpMessage) {
            errorInfo.setHttpMessage(httpMessage);
        }

        public void set(int errorHttp, String httpMessage, int httpCode, String httpMessage1) {

        }

        public ErrorInfo getInfo() {
            return errorInfo;
        }

        public ErrorInfo newInfo() {
            return null;
        }
    }
}
