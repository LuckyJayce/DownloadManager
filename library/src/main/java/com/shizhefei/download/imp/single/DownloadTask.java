package com.shizhefei.download.imp.single;

import android.Manifest;
import android.text.TextUtils;

import com.shizhefei.download.DownloadProgressSender;
import com.shizhefei.download.base.AbsDownloadTask;
import com.shizhefei.download.base.DownloadEntity;
import com.shizhefei.download.base.DownloadParams;
import com.shizhefei.download.base.IDownloadDB;
import com.shizhefei.download.exception.DownloadException;
import com.shizhefei.download.exception.ErrorEntity;
import com.shizhefei.download.utils.DownloadLogUtils;
import com.shizhefei.download.utils.FileNameUtils;
import com.shizhefei.download.utils.FileDownloadUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class DownloadTask extends AbsDownloadTask {
    static final int BUFFER_SIZE = 1024 * 4;
    private final long downloadId;
    private final IDownloadDB downloadDB;
    private DownloadParams downloadParams;
    private DownloadEntity downloadEntity;
    private HttpURLConnection httpURLConnection;
    private volatile boolean cancel;

    public DownloadTask(long downloadId, DownloadParams downloadParams, IDownloadDB downloadDB) {
        this.downloadId = downloadId;
        this.downloadParams = downloadParams;
        this.downloadDB = downloadDB;
        downloadEntity = downloadDB.find(downloadId);
        if (downloadEntity == null) {
            downloadEntity = buildEntity(downloadId, downloadParams);
            downloadDB.save(downloadParams, downloadEntity);
        }
    }

    @Override
    public DownloadParams getDownloadParams() {
        return downloadParams;
    }

    @Override
    public DownloadEntity getDownloadEntity() {
        return downloadEntity;
    }

    @Override
    public void execute(DownloadProgressSender sender) throws Exception {
        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;
        try {
            if (downloadEntity.getStatus() != DownloadEntity.STATUS_NEW) {

            }
            File saveDir = new File(downloadParams.getDir());
            String saveFileName = downloadParams.getFileName();
            String url = downloadParams.getUrl();

            checkupWifiConnect();

            //添加header
            Map<String, List<String>> headers = downloadParams.getHeaders();
            DownloadLogUtils.d("<---- %s request header %s", downloadId, headers);
            httpURLConnection = (HttpURLConnection) new URL(downloadParams.getUrl()).openConnection();
            if (headers != null) {
                for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                    for (String value : header.getValue()) {
                        httpURLConnection.addRequestProperty(header.getKey(), value);
                    }
                }
            }

            long currentOffset = downloadEntity.getCurrent();
            long endOffset = FileDownloadUtils.RANGE_INFINITE;
            if (downloadEntity.getCurrent() >= 0) {
                FileDownloadUtils.addRangeHeader(httpURLConnection, currentOffset, endOffset);
            }

            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.connect();

            int httpCode = httpURLConnection.getResponseCode();
            boolean acceptPartial = FileDownloadUtils.isAcceptRange(httpCode, httpURLConnection);
            if (FileDownloadUtils.isPreconditionFailed(httpURLConnection, httpCode, downloadEntity, acceptPartial)) {
                downloadEntity.setCurrent(0);
                ErrorEntity errorEntity = new ErrorEntity(ErrorEntity.ERROR_PRECONDITION_FAILED, "PreconditionFailed");
                downloadEntity.setErrorInfo(errorEntity);
                downloadDB.update(downloadEntity);
                throw new DownloadException(errorEntity);
            }

            if (cancel) {
                downloadEntity.setStatus(DownloadEntity.STATUS_PAUSED);
                return;
            }

            if (httpCode != HttpURLConnection.HTTP_PARTIAL && httpCode != HttpURLConnection.HTTP_OK) {
                String httpMessage = httpURLConnection.getResponseMessage();
                ErrorEntity errorEntity = new ErrorEntity(ErrorEntity.ERROR_HTTP, httpMessage, httpCode, httpMessage);
                downloadEntity.setErrorInfo(errorEntity);
                throw new DownloadException(errorEntity);
            }

            long contentLength = httpURLConnection.getContentLength();
            if (contentLength == 0) {
                ErrorEntity errorEntity = new ErrorEntity(ErrorEntity.ERROR_EMPTY_SIZE, FileDownloadUtils.
                        formatString(
                                "there isn't any content need to download on %d-%d with the "
                                        + "content-length is 0",
                                downloadId));
                downloadEntity.setErrorInfo(errorEntity);
                throw new DownloadException(errorEntity);
            }
            if (downloadEntity.getTotal() > 0 && contentLength != downloadEntity.getTotal()) {
                final String range;
                if (endOffset == FileDownloadUtils.RANGE_INFINITE) {
                    range = FileDownloadUtils.formatString("range[%d-)", currentOffset);
                } else {
                    range = FileDownloadUtils.formatString("range[%d-%d)", currentOffset, endOffset);
                }
                ErrorEntity errorEntity = new ErrorEntity(ErrorEntity.ERROR_SIZE_CHANGE, FileDownloadUtils.
                        formatString("require %s with contentLength(%d), but the "
                                        + "backend response contentLength is %d on "
                                        + "downloadId[%d]-connectionIndex[%d], please ask your backend "
                                        + "dev to fix such problem.",
                                range, downloadEntity.getTotal(), contentLength, downloadId));
                throw new DownloadException(errorEntity);
            }


            String contentType = httpURLConnection.getContentType();
            if (downloadParams.isOverride() && !TextUtils.isEmpty(saveFileName)) {
                saveFileName = downloadParams.getFileName();
            } else {
                String disposition = httpURLConnection.getHeaderField("Content-Disposition");
                saveFileName = FileNameUtils.getFileName(saveDir, saveFileName, url, contentType, disposition, downloadId);
            }
            String tempFileName = FileNameUtils.toValidFileName(saveDir, saveFileName + ".temp");

            downloadEntity.setTotal(contentLength);
            downloadEntity.setFilename(saveFileName);
            downloadEntity.setTempFileName(tempFileName);
            sender.onHttpConnect(httpCode, saveFileName, tempFileName, contentType, 0L, contentLength);


            File saveFileTemp = new File(saveDir, tempFileName);
            File saveFile = new File(saveDir, saveFileName);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            if (saveFile.exists()) {
                saveFile.delete();
            }

            randomAccessFile = new RandomAccessFile(saveFileTemp, "r");
            inputStream = httpURLConnection.getInputStream();
            randomAccessFile.seek(downloadEntity.getCurrent());
            if (contentLength != FileDownloadUtils.TOTAL_VALUE_IN_CHUNKED_RESOURCE) {
                randomAccessFile.setLength(contentLength);
            }

            if (cancel) {
                downloadEntity.setStatus(DownloadEntity.STATUS_PAUSED);
                return;
            }

            inputStream = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            long currentSize = 0;
            while ((length = inputStream.read(buffer)) != -1 && !cancel) {
                randomAccessFile.write(buffer, 0, length);
                currentSize += length;
                sender.onProgress(currentSize, contentLength);
                checkupWifiConnect();
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
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
                && !FileDownloadUtils.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
            throw new DownloadException(new ErrorEntity(ErrorEntity.ERROR_PERMISSION, Manifest.permission.ACCESS_NETWORK_STATE));
        }
        if (downloadParams.isWifiRequired() && FileDownloadUtils.isNetworkNotOnWifiType()) {
            throw new DownloadException(new ErrorEntity(ErrorEntity.ERROR_WIFIREQUIRED, "Only allows downloading this task on the wifi network type"));
        }
    }

    @Override
    public void cancel() {
        cancel = true;
        if (httpURLConnection != null) {
            httpURLConnection.disconnect();
        }
    }

    private DownloadEntity buildEntity(long downloadId, DownloadParams downloadParams) {
        DownloadEntity downloadEntity = new DownloadEntity();
        downloadEntity.setId(downloadId);
        downloadEntity.setUrl(downloadParams.getUrl());
        downloadEntity.setCurrent(0);
        downloadEntity.setTotal(0);
        downloadEntity.setFilename(downloadParams.getFileName());
        downloadEntity.setDir(downloadParams.getDir());
        downloadEntity.setStartTime(System.currentTimeMillis());
        return downloadEntity;
    }
}
