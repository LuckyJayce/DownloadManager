package com.shizhefei.download.task.m3u8;

import android.text.TextUtils;

import com.iheartradio.m3u8.Encoding;
import com.iheartradio.m3u8.Format;
import com.iheartradio.m3u8.PlaylistParser;
import com.iheartradio.m3u8.PlaylistWriter;
import com.iheartradio.m3u8.data.MediaPlaylist;
import com.iheartradio.m3u8.data.Playlist;
import com.iheartradio.m3u8.data.TrackData;
import com.iheartradio.m3u8.data.TrackInfo;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.entity.ErrorInfo;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.exception.DownloadException;
import com.shizhefei.download.exception.RemoveException;
import com.shizhefei.download.manager.DownloadManager;
import com.shizhefei.download.task.DownloadProgressSenderProxy;
import com.shizhefei.download.task.base.DownloadProgressListener;
import com.shizhefei.download.task.base.DownloadTaskImp;
import com.shizhefei.download.utils.DownloadUtils;
import com.shizhefei.download.utils.FileNameUtils;
import com.shizhefei.mvc.ProgressSender;
import com.shizhefei.task.ITask;
import com.shizhefei.task.function.Func1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

class M3u8DownloadTaskImp implements ITask<Void> {
    private final DownloadInfo downloadInfo;
    private final Func1<String, String> transformRealUrl;
    private long downloadId;
    private DownloadDB downloadDB;
    private ErrorInfo.Agency errorInfoAgency;
    private HttpInfo.Agency httpInfoAgency;
    private DownloadInfo.Agency downloadInfoAgency;
    private DownloadParams downloadParams;
    private volatile DownloadTaskImp downloadTask;
    private volatile boolean isRemove;
    private volatile boolean isCancel;
    private volatile boolean isRunning;
//    private ITask<Long> requestTotalTask;

    public M3u8DownloadTaskImp(long downloadId, DownloadParams downloadParams, DownloadDB downloadDB, boolean isOnlyRemove, Func1<String, String> transformRealUrl) {
        this.downloadId = downloadId;
        this.downloadParams = downloadParams;
        this.downloadDB = downloadDB;
        this.transformRealUrl = transformRealUrl;
        downloadInfoAgency = downloadDB.find(downloadId);
        if (downloadInfoAgency == null) {
            downloadInfoAgency = build(downloadParams);
        }
        errorInfoAgency = downloadInfoAgency.getErrorInfoAgency();
        httpInfoAgency = downloadInfoAgency.getHttpInfoAgency();
        downloadInfo = downloadInfoAgency.getInfo();
        if (!isOnlyRemove) {
            downloadInfoAgency.setStatus(DownloadManager.STATUS_PENDING);
            downloadInfoAgency.setStartTime(System.currentTimeMillis());
            downloadDB.replace(downloadParams, downloadInfoAgency.getInfo());
        }
    }

    public DownloadInfo getDownloadInfo() {
        return downloadInfo;
    }

    @Override
    public Void execute(ProgressSender progressSender) throws Exception {
        isRunning = true;
        try {
            executeDownload(progressSender);
        } catch (Exception exception) {
            if (exception instanceof DownloadException) {
                DownloadException downloadException = (DownloadException) exception;
                downloadInfoAgency.setStatus(DownloadManager.STATUS_ERROR);
                errorInfoAgency.set(downloadException.getErrorCode(), downloadException.getErrorMessage());
                downloadDB.updateError(downloadInfoAgency.getId(), errorInfoAgency.getInfo());
            } else {
                String message = exception.getMessage();
                if (TextUtils.isEmpty(message)) {
                    message = exception.getClass().getName();
                }
                downloadInfoAgency.setStatus(DownloadManager.STATUS_ERROR);
                errorInfoAgency.set(DownloadManager.ERROR_UNKNOW, message);
                downloadDB.updateError(downloadInfoAgency.getId(), errorInfoAgency.getInfo());
            }
            isRunning = false;
            throw exception;
        }
        if (isRemove) {
            removeFiles();
            isRunning = false;
            throw new RemoveException(downloadId);
        }
        if (isCancel) {
            downloadInfoAgency.setStatus(DownloadManager.STATUS_PAUSED);
            downloadDB.updateStatus(downloadInfoAgency.getId(), DownloadManager.STATUS_PAUSED);
        } else {
            downloadInfoAgency.setStatus(DownloadManager.STATUS_FINISHED);
            downloadDB.updateStatus(downloadInfoAgency.getId(), DownloadManager.STATUS_FINISHED);
        }
        isRunning = false;
        return null;
    }

    private void executeDownload(ProgressSender progressSender) throws Exception {
        final DownloadProgressSenderProxy progressSenderProxy = new DownloadProgressSenderProxy(downloadId, progressSender);

        progressSenderProxy.sendStart(downloadInfo.getCurrent(), downloadInfo.getTotal());

        final M3u8ExtInfo m3U8ExtInfo = new M3u8ExtInfo(downloadInfo.getExtInfo());
        DownloadUtils.logD("M3u8DownloadTaskImp downloadId=%d downloadInfo.getExtInfo():%s", downloadId, downloadInfo.getExtInfo());

        M3u8ExtInfo.ItemInfo m3u8Info = m3U8ExtInfo.getM3u8Info();
        String dir = downloadInfo.getDir();
        String fileName;
        if (!TextUtils.isEmpty(downloadInfo.getFileName())) {
            fileName = downloadInfo.getFileName();
        } else {
            fileName = downloadId + "_" + downloadInfoAgency.getStartTime() + ".m3u8";
        }
        final File m3u8File = new File(dir, fileName);
        DownloadUtils.logD("M3u8DownloadTaskImp  downloadId=%d fileName=%s m3u8File=%s m3u8Info=%s", downloadId, fileName, m3u8File, m3u8Info);
        Playlist playlist = null;
        if (m3u8Info != null) {
            if (!m3u8File.exists()) {
                DownloadUtils.logD("M3u8DownloadTaskImp downloadId=%d !m3u8File.exists()", downloadId);
                downloadTask = buildFromExtInfo(downloadId, dir, downloadParams, m3u8Info);
            } else {
                FileInputStream inputStream = null;
                try {
                    //解析m3u8文件
                    inputStream = new FileInputStream(m3u8File);
                    PlaylistParser parser = new PlaylistParser(inputStream, Format.EXT_M3U, Encoding.UTF_8);
                    playlist = parser.parse();
                } catch (Exception e) {
                    DownloadUtils.logD("M3u8DownloadTaskImp m3u8 file parse fail re-download m3u8 file m3u8File", downloadId);
                    //解析失败重新下载
                    m3u8Info = new M3u8ExtInfo.ItemInfo();
                    m3u8Info.setCurrent(0);
                    m3u8Info.setTotal(0);
                    m3u8Info.setFileName(m3u8File.getName());
                    m3u8Info.setUrl(downloadParams.getUrl());
                    m3U8ExtInfo.setM3u8Info(m3u8Info);
                    m3U8ExtInfo.setCurrentItemInfo(null);
                    downloadTask = buildFromExtInfo(downloadId, dir, downloadParams, m3u8Info);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            m3u8Info = new M3u8ExtInfo.ItemInfo();
            m3u8Info.setCurrent(0);
            m3u8Info.setTotal(0);
            m3u8Info.setFileName(m3u8File.getName());
            m3u8Info.setUrl(downloadParams.getUrl());
            m3U8ExtInfo.setM3u8Info(m3u8Info);
            m3U8ExtInfo.setCurrentItemInfo(null);
            downloadTask = buildFromExtInfo(downloadId, dir, downloadParams, m3u8Info);
        }
        //下载m3u8文件
        if (!isCancel) {
            if (downloadTask != null) {
                DownloadUtils.logD("M3u8DownloadTaskImp  downloadId=%d start download m3u8 file", downloadId);
                downloadDB.update(downloadInfoAgency.getInfo());
                downloadTask.execute(new DownloadProgressListener() {
                    @Override
                    public void onDownloadResetSchedule(long downloadId, int reason, long current, long total) {
                        downloadInfoAgency.setCurrent(current);
                        downloadInfoAgency.setExtInfo(m3U8ExtInfo.getJson());
                        downloadDB.updateDownloadResetSchedule(downloadInfoAgency.getId(), downloadInfoAgency.getCurrent(), downloadInfoAgency.getTotal(), downloadInfoAgency.getExtInfo());
                        progressSenderProxy.sendDownloadResetSchedule(current, total, reason);
                    }

                    @Override
                    public void onConnected(long downloadId, HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName, long current, long total) {
                        downloadInfoAgency.setStatus(DownloadManager.STATUS_CONNECTED);
                        downloadInfoAgency.setFilename(saveFileName);
                        downloadInfoAgency.setTempFileName(tempFileName);
                        downloadInfoAgency.setExtInfo(m3U8ExtInfo.getJson());

                        httpInfoAgency.setByInfo(httpInfo);
                        downloadDB.update(downloadInfoAgency.getInfo());
                        progressSenderProxy.sendConnected(httpInfo, saveDir, saveFileName, tempFileName, current, total);
                    }

                    @Override
                    public void onDownloadIng(long downloadId, long current, long total) {
                        downloadInfoAgency.setStatus(DownloadManager.STATUS_PROGRESS);
                        downloadInfoAgency.setCurrent(current);
                        downloadInfoAgency.setExtInfo(m3U8ExtInfo.getJson());
                        downloadDB.updateProgress(downloadInfoAgency.getId(), current, downloadInfoAgency.getTotal(), downloadInfoAgency.getExtInfo());
                        if (!isCancel) {
                            progressSenderProxy.sendDownloading(current, total);
                        }
                    }
                });
            }
        }
        if (!isCancel) {
            //解析m3u8文件
            if (playlist == null) {
                try {
                    FileInputStream inputStream = new FileInputStream(m3u8File);
                    PlaylistParser parser = new PlaylistParser(inputStream, Format.EXT_M3U, Encoding.UTF_8);
                    playlist = parser.parse();
                } catch (Exception e) {
                    throw new DownloadException(downloadId, DownloadManager.ERROR_M3U8_FILE_PARSE_FAIL, "m38u file parse fail", e);
                }
            }
            if (playlist.hasMediaPlaylist()) {
                //循环下载ts文件
                MediaPlaylist mediaPlaylist = playlist.getMediaPlaylist();
                final List<TrackData> tracks = mediaPlaylist.getTracks();

//                 通过head method的方式主逐一请求获取ts大小，但是时间太长不可取
//                if (downloadInfoAgency.getTotal() <= 0) {
//                    requestTotalTask = M38uSizeUtils.totalSize(downloadId, downloadInfo.getUrl(), tracks, downloadParams);
//                    Long totalSize = requestTotalTask.execute(null);
//                    if (totalSize > 0) {
//                        //加上m3u8文件大小
//                        totalSize += downloadInfoAgency.getCurrent();
//                        downloadInfoAgency.setTotal(totalSize);
//                        downloadDB.update(downloadInfoAgency.getInfo());
//                        progressSenderProxy.sendDownloading(downloadInfoAgency.getCurrent(), totalSize);
//                    }
//                }

                M3u8ExtInfo.ItemInfo currentItemInfo = m3U8ExtInfo.getCurrentItemInfo();
                int startIndex = 0;
                if (currentItemInfo != null) {
                    startIndex = currentItemInfo.getIndex();
                }
                DownloadUtils.logD("M3u8DownloadTaskImp downloadId=%d currentItemInfo=%s", downloadId, currentItemInfo);
                final int tracksSize = tracks.size();
                for (int i = startIndex; i < tracksSize && !isCancel; i++) {
                    final TrackData trackData = tracks.get(i);
                    if (currentItemInfo == null || currentItemInfo.getCurrent() == 0) {
                        currentItemInfo = buildItemInfo(i, trackData);
                        m3U8ExtInfo.setCurrentItemInfo(currentItemInfo);
                        downloadInfoAgency.setExtInfo(m3U8ExtInfo.getJson());
                        downloadDB.updateExtInfo(downloadInfoAgency.getId(), downloadInfoAgency.getExtInfo());
                        DownloadUtils.logD("M3u8DownloadTaskImp  downloadId=%d currentItemInfo=%s", downloadId, currentItemInfo);
                    }
                    downloadTask = buildFromExtInfo(downloadId, dir, downloadParams, currentItemInfo);
                    final long startTotalCurrent = downloadInfoAgency.getCurrent();
                    final long startItemCurrent = currentItemInfo.getCurrent();
                    final long startItemOffset = startTotalCurrent - startItemCurrent;
                    final M3u8ExtInfo.ItemInfo finalCurrentItemInfo = currentItemInfo;
                    final int index = i;
                    DownloadUtils.logD("M3u8DownloadTaskImp onDownloadItem star startTotalCurrent=%d startItemCurrent=%d startItemOffset=%d index=%d  ------------", startTotalCurrent, startItemCurrent, startItemOffset, i);
                    downloadTask.execute(new DownloadProgressListener() {
                        @Override
                        public void onDownloadResetSchedule(long downloadId, int reason, long current, long total) {
                            DownloadUtils.logD("M3u8DownloadTaskImp onDownloadItem onDownloadResetSchedule-- startItemOffset=%d current=%d resultCurrent=%d", startItemOffset, current, (startItemOffset + current));
                            downloadInfoAgency.setStatus(DownloadManager.STATUS_DOWNLOAD_RESET_SCHEDULE);
                            downloadInfoAgency.setCurrent(startItemOffset);
                            finalCurrentItemInfo.setCurrent(current);
                            finalCurrentItemInfo.setTotal(total);
                            downloadInfoAgency.setExtInfo(m3U8ExtInfo.getJson());
                            downloadDB.updateDownloadResetSchedule(downloadInfoAgency.getId(), downloadInfoAgency.getCurrent(), downloadInfoAgency.getTotal(), downloadInfoAgency.getExtInfo());
                            progressSenderProxy.sendDownloadResetSchedule(downloadInfoAgency.getCurrent(), downloadInfoAgency.getTotal(), reason);
                        }

                        @Override
                        public void onConnected(long downloadId, HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName, long current, long total) {
                            downloadInfoAgency.setStatus(DownloadManager.STATUS_CONNECTED);
                            finalCurrentItemInfo.setFileName(saveFileName);
                            finalCurrentItemInfo.setTempFileName(tempFileName);
                            finalCurrentItemInfo.setAcceptRange(httpInfo.isAcceptRange());
                            finalCurrentItemInfo.setEtag(httpInfo.getETag());

                            //根据已经知道大小的ts文件大小和时长估算文件总大小
                            if (downloadInfoAgency.getTotal() <= 0) {
                                TrackInfo trackInfo = trackData.getTrackInfo();
                                if (trackInfo.duration != 0) {
                                    if (index == tracks.size() - 1) {//已经下载最后一个ts，所以不需要估算已经是精确值
                                        long estimateTotal = startItemOffset + total;
                                        downloadInfoAgency.setTotal(estimateTotal);
                                        downloadInfoAgency.setEstimateTotal(estimateTotal);
                                    } else {
                                        //TODO 可以考虑加上每个item的平均值采样，优化更准确
                                        int totalD = 0;
                                        int currentTotalD = 0;
                                        for (int i = 0; i < tracksSize; i++) {
                                            totalD += tracks.get(i).getTrackInfo().duration;
                                        }
                                        for (int i = 0; i <= index; i++) {
                                            currentTotalD += tracks.get(i).getTrackInfo().duration;
                                        }
                                        long endItemOffset = startItemOffset + total;
                                        long estimateTotal = (long) (1.0 * totalD / currentTotalD * endItemOffset + m3u8File.length());
                                        if (downloadInfoAgency.getEstimateTotal() == 0 || downloadInfo.getCurrent() >= estimateTotal || Math.abs(downloadInfoAgency.getEstimateTotal() - estimateTotal) > 5 * 1024 * 1024) {//如果估算范围在5M以内就不更新估算的值
                                            downloadInfoAgency.setEstimateTotal(estimateTotal);
                                        }
                                        DownloadUtils.logD("testTotal=%d totalD=%d currentTotalD=%d endItemOffset=%d", estimateTotal, totalD, currentTotalD, endItemOffset);
                                    }
                                }
                            }

                            downloadInfoAgency.setExtInfo(m3U8ExtInfo.getJson());
                            httpInfoAgency.setAcceptRange(true);
                            downloadDB.update(downloadInfoAgency.getInfo());
                            progressSenderProxy.sendConnected(httpInfo, saveDir, downloadInfoAgency.getFilename(), downloadInfoAgency.getTempFileName(), downloadInfoAgency.getCurrent(), downloadInfoAgency.getTotal());
                        }

                        @Override
                        public void onDownloadIng(long downloadId, long current, long total) {
                            downloadInfoAgency.setStatus(DownloadManager.STATUS_PROGRESS);
                            downloadInfoAgency.setCurrent(startItemOffset + current);
                            finalCurrentItemInfo.setCurrent(current);
                            finalCurrentItemInfo.setTotal(total);
                            downloadInfoAgency.setExtInfo(m3U8ExtInfo.getJson());
                            downloadDB.updateProgress(downloadInfoAgency.getId(), downloadInfoAgency.getCurrent(), downloadInfoAgency.getTotal(), downloadInfoAgency.getExtInfo());
                            if (!isCancel) {
                                progressSenderProxy.sendDownloading(downloadInfoAgency.getCurrent(), downloadInfoAgency.getTotal());
                            }
                        }
                    });
                    DownloadUtils.logD("M3u8DownloadTaskImp onDownloadItem end- startTotalCurrent=%d total=%d", downloadInfoAgency.getCurrent(), currentItemInfo.getTotal());
                    currentItemInfo = null;
                }
                if (!isCancel) {
                    DownloadUtils.logD("M3u8DownloadTaskImp  downloadId=%d write m3u8", downloadId, currentItemInfo);
                    // 修改为ts为相对路径, 因为ts的uri可能是http开头的
                    PlaylistWriter playlistWriter = new PlaylistWriter(new FileOutputStream(m3u8File), Format.EXT_M3U, Encoding.UTF_8);
                    List<TrackData> tracksOutput = new ArrayList<>();
                    for (int i = 0; i < tracks.size(); i++) {
                        TrackData trackData = tracks.get(i);
                        tracksOutput.add(trackData.buildUpon().withUri(getItemFileName(trackData.getUri(), i)).build());
                    }
                    Playlist playlistOutput = playlist.buildUpon()
                            .withMediaPlaylist(mediaPlaylist.buildUpon().withTracks(tracksOutput).build())
                            .build();
                    playlistWriter.write(playlistOutput);
                }
            }
        }
    }

    public void remove() {
        isRemove = true;
        isCancel = true;
//        if (requestTotalTask != null) {
//            requestTotalTask.cancel();
//        }
        if (downloadTask != null) {
            downloadTask.remove();
        }
        if (!isRunning) {
            removeFiles();
        }
    }

    private void removeFiles() {
        try {
            if (!downloadInfo.getDir().equals(DownloadManager.getDownloadConfig().getDir())) {
                DownloadUtils.deleteDirectory(new File(downloadInfo.getDir()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        downloadDB.delete(downloadId);
    }

    @Override
    public void cancel() {
        isCancel = true;
//        if (requestTotalTask != null) {
//            requestTotalTask.cancel();
//        }
        downloadInfoAgency.setStatus(DownloadManager.STATUS_PAUSED);
        downloadDB.updateStatus(downloadInfoAgency.getId(), DownloadManager.STATUS_PAUSED);
        if (downloadTask != null) {
            downloadTask.cancel();
        }
    }

    static String getTsUrl(String m38uUrl, String tsUri) {
        if (tsUri.startsWith("http://") || tsUri.startsWith("https://"))
            return tsUri;
        int index = m38uUrl.lastIndexOf("/");
        String urlPre = m38uUrl.substring(0, index + 1);
        return urlPre + tsUri;
    }

    private DownloadInfo.Agency build(DownloadParams downloadParams) {
        DownloadInfo.Agency downloadInfoAgency = new DownloadInfo.Agency(downloadParams);
        downloadInfoAgency.setId(downloadId);
        downloadInfoAgency.setUrl(downloadParams.getUrl());
        downloadInfoAgency.setCurrent(0);
        if (downloadParams.getTotalSize() > 0) {
            downloadInfoAgency.setTotal(downloadParams.getTotalSize());
        } else {
            downloadInfoAgency.setTotal(0);
        }
        downloadInfoAgency.setStatus(DownloadManager.STATUS_PENDING);
        downloadInfoAgency.setFilename(downloadParams.getFileName());
        downloadInfoAgency.setStartTime(System.currentTimeMillis());
        downloadInfoAgency.setDownloadTaskName(M3u8DownloadTask.DOWNLOAD_TASK_NAME);
        downloadInfoAgency.setDir(downloadParams.getDir() + File.separator + downloadId + "_" + downloadInfoAgency.getStartTime());
        return downloadInfoAgency;
    }

    private M3u8ExtInfo.ItemInfo buildItemInfo(int index, TrackData trackData) {
        String url = getTsUrl(downloadInfo.getUrl(), trackData.getUri());
        M3u8ExtInfo.ItemInfo currentItemInfo = new M3u8ExtInfo.ItemInfo();
        currentItemInfo.setCurrent(0);
        currentItemInfo.setTotal(0);
        currentItemInfo.setIndex(index);
        String itemFileName = getItemFileName(trackData.getUri(), index);
        currentItemInfo.setFileName(itemFileName);
        currentItemInfo.setUrl(url);
        return currentItemInfo;
    }

    private DownloadTaskImp buildFromExtInfo(long downloadId, String dir, DownloadParams downloadParams, M3u8ExtInfo.ItemInfo itemInfo) throws Exception {
        String url;
        if (transformRealUrl != null) {
            url = transformRealUrl.call(itemInfo.getUrl());
        } else {
            url = itemInfo.getUrl();
        }
        return new DownloadTaskImp(downloadId, downloadParams, url, dir, itemInfo.getCurrent(), itemInfo.getTotal(), itemInfo.getFileName(), itemInfo.getTempFileName(), downloadInfo.getHttpInfo().isAcceptRange(), downloadInfo.getHttpInfo().getETag());
    }

    private String getItemFileName(String uri, int elementIndex) {
        String path = URI.create(uri).getPath();
        int index = path.lastIndexOf("/");
        if (index > 0) {
            path = path.substring(index, path.length());
        }
        return FileNameUtils.toValidFileName(elementIndex + "_" + path);
    }
}
