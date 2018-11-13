package com.shizhefei.download.entity;

public class HttpInfo {
    private int httpCode;
    private String contentType;
    private long contentLength;
    private String eTag;

    public String getETag() {
        return eTag;
    }

    private void setETag(String eTag) {
        this.eTag = eTag;
    }

    private HttpInfo() {
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
        return null;
    }

    public void setByJson(String json) {
    }

    public static class Agency {
        private HttpInfo httpInfo;

        public Agency() {
            this.httpInfo = new HttpInfo();
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
