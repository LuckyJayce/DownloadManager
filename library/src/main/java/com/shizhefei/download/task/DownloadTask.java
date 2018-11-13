package com.shizhefei.download.task;

import android.Manifest;
import android.text.TextUtils;

import com.shizhefei.download.base.RemoveHandler;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.exception.DownloadException;
import com.shizhefei.download.exception.RemoveException;
import com.shizhefei.download.manager.DownloadManager;
import com.shizhefei.download.prxoy.DownloadProgressSenderProxy;
import com.shizhefei.download.utils.DownloadLogUtils;
import com.shizhefei.download.utils.FileNameUtils;
import com.shizhefei.download.utils.DownloadUtils;
import com.shizhefei.mvc.ProgressSender;
import com.shizhefei.task.ITask;

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

public class DownloadTask implements ITask<Void>, RemoveHandler.OnRemoveListener {
    private static final int BUFFER_SIZE = 1024 * 4;
    private final long downloadId;
    private final DownloadInfo downloadInfo;
    private DownloadParams downloadParams;
    private HttpURLConnection httpURLConnection;
    private volatile boolean cancel;
    private volatile boolean remove;

    public DownloadTask(long downloadId, DownloadParams downloadParams, DownloadInfo downloadInfo) {
        this.downloadId = downloadId;
        this.downloadParams = downloadParams;
        this.downloadInfo = downloadInfo;
    }

    @Override
    public Void execute(ProgressSender sender) throws Exception {
        long current = downloadInfo.getCurrent();
        long total = downloadInfo.getTotal();
        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;
        File saveFileTemp = null;
        File saveFile = null;

        DownloadProgressSenderProxy senderProxy = new DownloadProgressSenderProxy(downloadId, sender);
        senderProxy.sendStart(current, total);

        try {
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

            String tempFileName = downloadInfo.getTempFileName();
            if (!TextUtils.isEmpty(tempFileName)) {
                File temp = new File(saveDir, tempFileName);
                if (current > 0 && !temp.exists()) {
                    senderProxy.sendDownloadFromBegin(current, total, DownloadManager.DOWNLOAD_FROM_BEGIN_REASON_FILE_REMOVE);
                    current = 0;
                }
            }

            if (current > 0) {
                if (downloadInfo.getHttpInfo().isAcceptRange()) {
                    DownloadUtils.addRangeHeader(httpURLConnection, current, DownloadUtils.RANGE_INFINITE);
                } else {
                    senderProxy.sendDownloadFromBegin(current, total, DownloadManager.DOWNLOAD_FROM_BEGIN_UNSUPPORT_RANGE);
                    current = 0;
                }
            }

            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.connect();

            final String oldETag = downloadInfo.getHttpInfo().getETag();
            String newETag = DownloadUtils.findEtag(downloadInfo.getId(), httpURLConnection);
            if (current > 0) {
                if (oldETag != null && !oldETag.equals(newETag)) {
                    senderProxy.sendDownloadFromBegin(current, total, DownloadManager.DOWNLOAD_FROM_BEGIN_ETAG_CHANGE);
                    current = 0;
                }
            }

            int httpCode = httpURLConnection.getResponseCode();
            boolean isAcceptRange = DownloadUtils.isAcceptRange(httpCode, httpURLConnection);

            if (cancel) {
                return null;
            }

            if (httpCode != HttpURLConnection.HTTP_PARTIAL && httpCode != HttpURLConnection.HTTP_OK) {
                String httpMessage = httpURLConnection.getResponseMessage();
                throw new DownloadException(downloadId, DownloadManager.ERROR_HTTP, httpMessage);
            }

            long contentLength = httpURLConnection.getContentLength();
            if (contentLength == 0) {
                String errorMessage = DownloadUtils.formatString("there isn't any content need to download on %d-%d with the content-length is 0", downloadId);
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
                tempFileName = FileNameUtils.toValidFileName(saveDir, saveFileName + ".temp");
            }

            HttpInfo.Agency agency = new HttpInfo.Agency();
            agency.setContentLength(total);
            agency.setETag(newETag);
            agency.setContentType(contentType);
            agency.setHttpCode(httpCode);
            agency.setAcceptRange(isAcceptRange);
            HttpInfo info = agency.getInfo();
            senderProxy.sendConnected(info, saveDir.getPath(), saveFileName, tempFileName, current, total);

            saveFileTemp = new File(saveDir, tempFileName);
            saveFile = new File(saveDir, saveFileName);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            if (saveFile.exists()) {
                saveFile.delete();
            }

            randomAccessFile = new RandomAccessFile(saveFileTemp, "rw");
            inputStream = httpURLConnection.getInputStream();
            if (current > 0) {
                randomAccessFile.seek(current);
            }

            if (cancel) {
                return null;
            }

            inputStream = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = inputStream.read(buffer)) != -1 && !cancel) {
                randomAccessFile.write(buffer, 0, length);
                current += length;
                senderProxy.sendDownloading(current, total);
                checkupWifiConnect();
            }
        } catch (FileNotFoundException e) {
            String errorMessage = DownloadUtils.formatStringE(e, "FileNotFoundException dir=%s saveFileTemp=%s saveFile=%s", downloadParams.getDir(), saveFileTemp, saveFile);
            throw new DownloadException(downloadId, DownloadManager.ERROR_FILENOTFOUNDEXCEPTION, errorMessage, e.getCause());
        } catch (MalformedURLException e) {
            String errorMessage = DownloadUtils.formatStringE(e, "MalformedURLException url=%s", downloadParams.getUrl());
            throw new DownloadException(downloadId, DownloadManager.ERROR_MALFORMEDURLEXCEPTION, errorMessage, e.getCause());
        } catch (ProtocolException e) {
            String errorMessage = DownloadUtils.formatStringE(e, "ProtocolException url=%s", downloadParams.getUrl());
            throw new DownloadException(downloadId, DownloadManager.ERROR_PROTOCOLEXCEPTION, errorMessage, e.getCause());
        } catch (IOException e) {
            String errorMessage = DownloadUtils.formatStringE(e, "IOException dir=%s saveFileTemp=%s saveFile=%s", downloadParams.getDir(), saveFileTemp, saveFile);
            throw new DownloadException(downloadId, DownloadManager.ERROR_IOEXCEPTION, errorMessage, e.getCause());
        } catch (DownloadException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = DownloadUtils.formatStringE(e, "UNKNOWN");
            throw new DownloadException(downloadId, DownloadManager.ERROR_UNKNOW, errorMessage, e.getCause());
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
        if (remove) {
            if (saveFileTemp != null && saveFileTemp.exists()) {
                saveFileTemp.delete();
                DownloadLogUtils.d("saveFileTemp delete saveFileTemp %s" + saveFileTemp);
            }
            if (saveFile != null && saveFile.exists()) {
                saveFile.delete();
                DownloadLogUtils.d("saveFile delete saveFile %s" + saveFile);
            }
            senderProxy.sendRemove(current, total);
            throw new RemoveException(downloadId);
        }
        if (!cancel && !remove) {
            boolean success = saveFileTemp.renameTo(saveFile);
            DownloadLogUtils.d("success " + success);
        }
        return null;
    }

    private void checkupWifiConnect() throws Exception {
        if (downloadParams.isWifiRequired()
                && !DownloadUtils.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
            throw new DownloadException(downloadId, DownloadManager.ERROR_PERMISSION, Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (downloadParams.isWifiRequired() && DownloadUtils.isNetworkNotOnWifiType()) {
            throw new DownloadException(downloadId, DownloadManager.ERROR_WIFIREQUIRED, "Only allows downloading this task on the wifi network type");
        }
    }

    @Override
    public void cancel() {
        cancel = true;
    }

    @Override
    public void onRemove() {
        DownloadLogUtils.d("onRemove %d", downloadId);
        remove = true;
        cancel = true;
    }
}
