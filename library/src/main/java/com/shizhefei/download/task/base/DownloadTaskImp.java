package com.shizhefei.download.task.base;

import android.Manifest;
import android.os.SystemClock;
import android.text.TextUtils;

import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.exception.DownloadException;
import com.shizhefei.download.manager.DownloadManager;
import com.shizhefei.download.utils.DownloadUtils;
import com.shizhefei.download.utils.FileNameUtils;
import com.shizhefei.download.utils.UrlBuilder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class DownloadTaskImp {
    private static final int BUFFER_SIZE = 1024 * 4;
    private final long downloadId;
    private final boolean isAcceptRange;
    private final String etag;
    private final String url;
    private final String dir;
    private long current;
    private long total;
    private String tempFileName;
    private String saveFileName;
    private HttpURLConnection httpURLConnection;
    private volatile boolean cancel;
    private volatile boolean isRunning;
    private volatile boolean remove;
    private DownloadParams downloadParams;
    private File saveFileTemp = null;
    private File saveFile = null;

    //    public DownloadTask(long downloadId, String dir, String url, long current, long total, boolean isOverride, String saveFileName, String tempFileName, String eTag, boolean isAcceptRange, boolean isWifiRequired,Map<String, List<String>> headers) {
    public DownloadTaskImp(long downloadId, DownloadParams downloadParams, String url, String dir, long current, long total, String saveFileName, String tempFileName, boolean isAcceptRange, String etag) {
        this.downloadId = downloadId;
        this.url = url;
        this.dir = dir;
        this.current = current;
        this.total = total;
        this.tempFileName = tempFileName;
        this.downloadParams = downloadParams;
        this.saveFileName = saveFileName;
        this.isAcceptRange = isAcceptRange;
        this.etag = etag;
    }

    public Void execute(DownloadProgressListener downloadListener) throws DownloadException {
        isRunning = true;
        DownloadException exception = null;
        try {
            executeDownload(downloadListener);
        } catch (FileNotFoundException e) {
            String errorMessage = DownloadUtils.formatStringE(e, "FileNotFoundException dir=%s saveFileTemp=%s saveFile=%s", dir, saveFileTemp, saveFile);
            exception = new DownloadException(downloadId, DownloadManager.ERROR_FILENOTFOUNDEXCEPTION, errorMessage, e.getCause());
        } catch (MalformedURLException e) {
            String errorMessage = DownloadUtils.formatStringE(e, "MalformedURLException url=%s", url);
            exception = new DownloadException(downloadId, DownloadManager.ERROR_MALFORMEDURLEXCEPTION, errorMessage, e.getCause());
        } catch (ProtocolException e) {
            String errorMessage = DownloadUtils.formatStringE(e, "ProtocolException url=%s", url);
            exception = new DownloadException(downloadId, DownloadManager.ERROR_PROTOCOLEXCEPTION, errorMessage, e.getCause());
        } catch (IOException e) {
            String errorMessage = DownloadUtils.formatStringE(e, "IOException dir=%s saveFileTemp=%s saveFile=%s", dir, saveFileTemp, saveFile);
            exception = new DownloadException(downloadId, DownloadManager.ERROR_IOEXCEPTION, errorMessage, e.getCause());
        } catch (DownloadException e) {
            exception = e;
        } catch (Exception e) {
            String errorMessage = DownloadUtils.formatStringE(e, "UNKNOWN");
            exception = new DownloadException(downloadId, DownloadManager.ERROR_UNKNOW, errorMessage, e.getCause());
        }
        if (cancel) {//停止下载
            if (remove) {//移除下载
                removeFiles();
            }
        } else if (exception != null) {//下载时出现异常，下载失败
            DownloadUtils.logE(exception, "DownloadTask exception");
            isRunning = false;
            throw exception;
        } else {// 下载成功
            if (saveFile.exists()) {
                saveFile.delete();
            }
            boolean success = saveFileTemp.renameTo(saveFile);
            DownloadUtils.logD("DownloadTask renameTo success " + success);
        }
        isRunning = false;
        return null;
    }

    private void executeDownload(DownloadProgressListener downloadListener) throws Exception {
        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;
        try {
            File saveDir = new File(dir);
            checkupWifiConnect();

            //添加header
            Map<String, List<String>> headers = downloadParams.getHeaders();
            DownloadUtils.logD("downloadId：%d request header %s", downloadId, headers);
            Map<String, List<String>> params = downloadParams.getParams();
            String finalUrl;
            if (params != null) {
                UrlBuilder urlBuilder = new UrlBuilder(url);
                for (Map.Entry<String, List<String>> stringListEntry : params.entrySet()) {
                    urlBuilder.param(stringListEntry.getKey(), stringListEntry.getValue());
                }
                finalUrl = urlBuilder.build();
            } else {
                finalUrl = url;
            }

            httpURLConnection = (HttpURLConnection) new URL(finalUrl).openConnection();
            if (headers != null) {
                for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                    for (String value : header.getValue()) {
                        httpURLConnection.addRequestProperty(header.getKey(), value);
                    }
                }
            }

            if (!TextUtils.isEmpty(tempFileName)) {
                File temp = new File(saveDir, tempFileName);
                if (current > 0 && !temp.exists()) {
                    downloadListener.onDownloadResetSchedule(downloadId, DownloadManager.DOWNLOAD_RESET_SCHEDULE_REASON_FILE_REMOVE, current, total);
                    current = 0;
                }
            }

            if (current > 0) {
                if (isAcceptRange) {
                    DownloadUtils.addRangeHeader(httpURLConnection, current, DownloadUtils.RANGE_INFINITE);
                } else {
                    downloadListener.onDownloadResetSchedule(downloadId, DownloadManager.DOWNLOAD_RESET_SCHEDULE_REASON_UNSUPPORT_RANGE, current, total);
                    current = 0;
                }
            }

            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.connect();

            final String oldETag = etag;
            String newETag = DownloadUtils.findEtag(downloadId, httpURLConnection);
            if (current > 0) {
                if (oldETag != null && !oldETag.equals(newETag)) {
                    downloadListener.onDownloadResetSchedule(downloadId, DownloadManager.DOWNLOAD_RESET_SCHEDULE_REASON_ETAG_CHANGE, current, total);
                    current = 0;
                }
            }

            int httpCode = httpURLConnection.getResponseCode();
            boolean isAcceptRange = DownloadUtils.isAcceptRange(httpCode, httpURLConnection);

            if (cancel) {
                return;
            }

            if (httpCode != HttpURLConnection.HTTP_PARTIAL && httpCode != HttpURLConnection.HTTP_OK) {
                String httpMessage = "httpCode:" + httpCode + " message:" + httpURLConnection.getResponseMessage() + " url:" + url;
                throw new DownloadException(downloadId, DownloadManager.ERROR_HTTP, httpMessage);
            }

            long contentLength = httpURLConnection.getContentLength();
            if (contentLength == 0) {
                String errorMessage = DownloadUtils.formatString("there isn't any content need to download on %d-%logD with the content-length is 0", downloadId);
                throw new DownloadException(downloadId, DownloadManager.ERROR_EMPTY_SIZE, errorMessage);
            }
            if (current <= 0) {
                current = 0;
                total = contentLength;
            }

            String contentType = httpURLConnection.getContentType();
            if (downloadParams.isOverride() && !TextUtils.isEmpty(saveFileName)) {
                saveFileName = downloadParams.getFileName();
            } else {
                String disposition = httpURLConnection.getHeaderField("Content-Disposition");
                saveFileName = FileNameUtils.getFileName(saveDir, saveFileName, url, contentType, disposition, downloadId);
            }
            if (TextUtils.isEmpty(tempFileName)) {
                tempFileName = FileNameUtils.toValidFileName(saveDir, saveFileName + ".temp_" + downloadId);
            }

            HttpInfo.Agency agency = new HttpInfo.Agency();
            agency.setContentLength(total);
            agency.setETag(newETag);
            agency.setContentType(contentType);
            agency.setHttpCode(httpCode);
            agency.setAcceptRange(isAcceptRange);
            HttpInfo info = agency.getInfo();
            downloadListener.onConnected(downloadId, info, saveDir.getPath(), saveFileName, tempFileName, current, total);

            saveFileTemp = new File(saveDir, tempFileName);
            saveFile = new File(saveDir, saveFileName);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }

            randomAccessFile = new RandomAccessFile(saveFileTemp, "rw");
            inputStream = httpURLConnection.getInputStream();
            if (current > 0) {
                randomAccessFile.seek(current);
            }

            if (cancel) {
                return;
            }

            inputStream = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            long time = SystemClock.elapsedRealtime();
            while ((length = inputStream.read(buffer)) != -1 && !cancel) {
                randomAccessFile.write(buffer, 0, length);
                current += length;

                long currentTime = SystemClock.elapsedRealtime();
                if ((time + DownloadManager.getDownloadConfig().getMinDownloadProgressTime() < currentTime) || (current >= total)) {
                    time = currentTime;
                    downloadListener.onDownloadIng(downloadId, current, total);
                }
                checkupWifiConnect();
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkupWifiConnect() throws Exception {
        if (downloadParams.isWifiRequired()
                && !DownloadUtils.checkPermission(DownloadManager.getApplicationContext(), Manifest.permission.ACCESS_NETWORK_STATE)) {
            throw new DownloadException(downloadId, DownloadManager.ERROR_PERMISSION, Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (downloadParams.isWifiRequired() && DownloadUtils.isNetworkNotOnWifiType()) {
            throw new DownloadException(downloadId, DownloadManager.ERROR_WIFIREQUIRED, "Only allows downloading this task on the wifi network type");
        }
    }

    public void cancel() {
        cancel = true;
    }

    public void remove() {
        DownloadUtils.logD("DownloadTask remove %d", downloadId);
        remove = true;
        cancel = true;
        if (!isRunning) {
            removeFiles();
        }
    }

    private void removeFiles() {
        try {
            if (!TextUtils.isEmpty(saveFileName)) {
                new File(dir, saveFileName).delete();
            }
            if (!TextUtils.isEmpty(tempFileName)) {
                new File(dir, tempFileName).delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
