package com.shizhefei.download.imp.single;

import android.Manifest;
import android.text.TextUtils;

import com.shizhefei.download.base.RemoveHandler;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.base.DownloadParams;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.exception.DownloadException;
import com.shizhefei.download.entity.ErrorInfo;
import com.shizhefei.download.base.DownloadProgressSenderProxy;
import com.shizhefei.download.utils.DownloadLogUtils;
import com.shizhefei.download.utils.FileNameUtils;
import com.shizhefei.download.utils.FileDownloadUtils;
import com.shizhefei.mvc.ProgressSender;
import com.shizhefei.task.ITask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
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
        DownloadProgressSenderProxy senderProxy = new DownloadProgressSenderProxy(downloadId, sender);

        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;
        File saveFileTemp = null;
        File saveFile = null;
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

            long currentOffset = downloadInfo.getCurrent();
            long endOffset = FileDownloadUtils.RANGE_INFINITE;
            if (downloadInfo.getCurrent() >= 0) {
                FileDownloadUtils.addRangeHeader(httpURLConnection, currentOffset, endOffset);
            }

            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.connect();

            int httpCode = httpURLConnection.getResponseCode();
            boolean acceptPartial = FileDownloadUtils.isAcceptRange(httpCode, httpURLConnection);
            if (FileDownloadUtils.isPreconditionFailed(httpURLConnection, httpCode, downloadInfo, acceptPartial)) {
                throw new DownloadException(ErrorInfo.ERROR_PRECONDITION_FAILED, "PreconditionFailed");
            }

            if (cancel) {
                return null;
            }

            if (httpCode != HttpURLConnection.HTTP_PARTIAL && httpCode != HttpURLConnection.HTTP_OK) {
                String httpMessage = httpURLConnection.getResponseMessage();
                throw new DownloadException(ErrorInfo.ERROR_HTTP, httpMessage);
            }

            long contentLength = httpURLConnection.getContentLength();
            if (contentLength == 0) {
                throw new DownloadException(ErrorInfo.ERROR_EMPTY_SIZE, FileDownloadUtils.
                        formatString(
                                "there isn't any content need to download on %d-%d with the "
                                        + "content-length is 0",
                                downloadId));
            }
            if (downloadInfo.getTotal() > 0 && contentLength != downloadInfo.getTotal()) {
                final String range;
                if (endOffset == FileDownloadUtils.RANGE_INFINITE) {
                    range = FileDownloadUtils.formatString("range[%d-)", currentOffset);
                } else {
                    range = FileDownloadUtils.formatString("range[%d-%d)", currentOffset, endOffset);
                }
                throw new DownloadException(ErrorInfo.ERROR_SIZE_CHANGE, FileDownloadUtils.
                        formatString("require %s with contentLength(%d), but the "
                                        + "backend response contentLength is %d on "
                                        + "downloadId[%d]-connectionIndex[%d], please ask your backend "
                                        + "dev to fix such problem.",
                                range, downloadInfo.getTotal(), contentLength, downloadId));
            }

            String contentType = httpURLConnection.getContentType();
            if (downloadParams.isOverride() && !TextUtils.isEmpty(saveFileName)) {
                saveFileName = downloadParams.getFileName();
            } else {
                String disposition = httpURLConnection.getHeaderField("Content-Disposition");
                saveFileName = FileNameUtils.getFileName(saveDir, saveFileName, url, contentType, disposition, downloadId);
            }
            String tempFileName = downloadInfo.getTempFileName();
            if (TextUtils.isEmpty(tempFileName)) {
                tempFileName = FileNameUtils.toValidFileName(saveDir, saveFileName + ".temp");
            }

            String newEtag = FileDownloadUtils.findEtag(downloadId, httpURLConnection);
            HttpInfo.Agency agency = new HttpInfo.Agency();
            agency.setHttpCode(httpCode);
            agency.setETag(newEtag);
            agency.setContentType(contentType);
            agency.setContentLength(contentLength);
            senderProxy.sendConnected(httpCode, agency.getInfo(), saveDir.getPath(), saveFileName, tempFileName);

            saveFileTemp = new File(saveDir, tempFileName);
            saveFile = new File(saveDir, saveFileName);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            if (saveFile.exists()) {
                saveFile.delete();
            }

            randomAccessFile = new RandomAccessFile(saveFileTemp, "r");
            inputStream = httpURLConnection.getInputStream();
            randomAccessFile.seek(downloadInfo.getCurrent());
            if (contentLength != FileDownloadUtils.TOTAL_VALUE_IN_CHUNKED_RESOURCE) {
                randomAccessFile.setLength(contentLength);
            }

            if (cancel) {
                return null;
            }

            inputStream = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            long currentSize = 0;
            while ((length = inputStream.read(buffer)) != -1 && !cancel) {
                randomAccessFile.write(buffer, 0, length);
                currentSize += length;
                senderProxy.sendProgress(currentSize, contentLength);
                checkupWifiConnect();
            }
            //TODO 如果强制取消删除文件和临时文件
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
        if (remove) {
            if (saveFileTemp != null) {
                saveFileTemp.delete();
            }
            if (saveFile != null) {
                saveFile.delete();
            }
            senderProxy.sendRemove();
        }
        return null;
    }

    private void checkupWifiConnect() throws Exception {
        if (downloadParams.isWifiRequired()
                && !FileDownloadUtils.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
            throw new DownloadException(ErrorInfo.ERROR_PERMISSION, Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (downloadParams.isWifiRequired() && FileDownloadUtils.isNetworkNotOnWifiType()) {
            throw new DownloadException(ErrorInfo.ERROR_WIFIREQUIRED, "Only allows downloading this task on the wifi network type");
        }
    }

    @Override
    public void cancel() {
        cancel = true;
        if (httpURLConnection != null) {
            httpURLConnection.disconnect();
        }
    }

    @Override
    public void onRemove() {
        remove = true;
        cancel();
    }
}
