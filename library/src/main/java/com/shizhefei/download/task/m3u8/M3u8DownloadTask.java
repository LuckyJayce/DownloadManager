package com.shizhefei.download.task.m3u8;

import android.text.TextUtils;

import com.iheartradio.m3u8.Encoding;
import com.iheartradio.m3u8.Format;
import com.iheartradio.m3u8.PlaylistParser;
import com.iheartradio.m3u8.PlaylistWriter;
import com.iheartradio.m3u8.data.MediaPlaylist;
import com.iheartradio.m3u8.data.Playlist;
import com.iheartradio.m3u8.data.TrackData;
import com.shizhefei.download.base.RemoveHandler;
import com.shizhefei.download.db.DownloadDB;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.entity.ErrorInfo;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.manager.DownloadManager;
import com.shizhefei.download.task.base.DownloadProgressListener;
import com.shizhefei.download.task.base.DownloadTaskImp;
import com.shizhefei.download.utils.FileNameUtils;
import com.shizhefei.mvc.ProgressSender;
import com.shizhefei.task.ITask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class M3u8DownloadTask implements ITask<Void>, RemoveHandler.OnRemoveListener {
    private final Executor executor;
    private final DownloadInfo downloadInfo;
    private long downloadId;
    private DownloadDB downloadDB;
    private ErrorInfo.Agency errorInfoAgency;
    private HttpInfo.Agency httpInfoAgency;
    private RemoveHandler removeHandler;
    private DownloadInfo.Agency downloadInfoAgency;
    private DownloadParams downloadParams;
    private volatile DownloadTaskImp downloadTask;
    private volatile boolean isRemove;
    private volatile boolean isCancel;

    public M3u8DownloadTask(long downloadId, DownloadParams downloadParams, DownloadDB downloadDB, RemoveHandler removeHandler, Executor executor) {
        this.downloadId = downloadId;
        this.downloadParams = downloadParams;
        this.downloadDB = downloadDB;
        this.executor = executor;
        downloadInfoAgency = downloadDB.find(downloadId);
        if (downloadInfoAgency == null) {
            downloadInfoAgency = build(downloadParams);
        }
        errorInfoAgency = downloadInfoAgency.getErrorInfoAgency();
        httpInfoAgency = downloadInfoAgency.getHttpInfoAgency();
        downloadInfo = downloadInfoAgency.getInfo();
        this.removeHandler = removeHandler;
    }

    public DownloadInfo getDownloadInfo() {
        return downloadInfo;
    }

    @Override
    public void onRemove() {
        isRemove = true;
        isCancel = true;
        if (downloadTask != null) {
            downloadTask.onRemove();
        }
    }

    @Override
    public Void execute(ProgressSender progressSender) throws Exception {
        final M3u8ExtInfo m3U8ExtInfo = new M3u8ExtInfo(downloadInfo.getExtInfo());
        M3u8ExtInfo.ItemInfo m3u8Info = m3U8ExtInfo.getM3u8Info();
        String dir = downloadInfo.getDir();
        String fileName;
        if (!TextUtils.isEmpty(downloadInfo.getFileName())) {
            fileName = downloadInfo.getFileName();
        } else {
            fileName = downloadId + "_" + downloadInfoAgency.getStartTime() + ".m3u8";
        }
        File m3u8File = new File(dir, fileName);
        if (m3u8Info != null) {
            if (!m3u8File.exists()) {
                downloadTask = buildFromExtInfo(downloadId, dir, downloadParams, m3u8Info);
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
        if (!isCancel) {
            if (downloadTask != null) {
                downloadTask.execute(new DownloadProgressListener() {
                    @Override
                    public void onDownloadResetBegin(long downloadId, int reason, long current, long total) {
                        downloadInfoAgency.setCurrent(current);
                        downloadInfoAgency.setExtInfo(m3U8ExtInfo.getJson());
                        downloadDB.update(downloadInfoAgency.getInfo());
                    }

                    @Override
                    public void onConnected(long downloadId, HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName, long current, long total) {
                        downloadInfoAgency.setStatus(DownloadManager.STATUS_CONNECTED);
                        downloadInfoAgency.setFilename(saveFileName);
                        downloadInfoAgency.setTempFileName(tempFileName);
                        downloadInfoAgency.setExtInfo(m3U8ExtInfo.getJson());

                        httpInfoAgency.setByInfo(httpInfo);
                        downloadDB.update(downloadInfoAgency.getInfo());
                    }

                    @Override
                    public void onDownloadIng(long downloadId, long current, long total) {
                        downloadInfoAgency.setStatus(DownloadManager.STATUS_DOWNLOAD_ING);
                        downloadInfoAgency.setCurrent(current);
                        downloadInfoAgency.setExtInfo(m3U8ExtInfo.getJson());
                        downloadDB.update(downloadId, current, total);
                    }
                });
            }
        }
        if (!isCancel) {
            FileInputStream inputStream = new FileInputStream(m3u8File);
            PlaylistParser parser = new PlaylistParser(inputStream, Format.EXT_M3U, Encoding.UTF_8);
            Playlist playlist = parser.parse();
            if (playlist.hasMediaPlaylist()) {
                MediaPlaylist mediaPlaylist = playlist.getMediaPlaylist();
                List<TrackData> tracks = mediaPlaylist.getTracks();
                M3u8ExtInfo.ItemInfo currentItemInfo = m3U8ExtInfo.getCurrentItemInfo();
                int startIndex = 0;
                if (currentItemInfo != null) {
                    startIndex = currentItemInfo.getIndex();
                }
                for (int i = startIndex; i < tracks.size() && !isCancel; i++) {
                    if (currentItemInfo == null) {
                        currentItemInfo = buildItemInfo(i, tracks.get(i));
                        m3U8ExtInfo.setCurrentItemInfo(currentItemInfo);
                    }
                    downloadTask = buildFromExtInfo(downloadId, dir, downloadParams, currentItemInfo);
                    final long starCurrent = currentItemInfo.getCurrent();
                    downloadTask.execute(new DownloadProgressListener() {
                        @Override
                        public void onDownloadResetBegin(long downloadId, int reason, long current, long total) {
                            downloadInfoAgency.setCurrent(downloadInfoAgency.getCurrent() - starCurrent + current);
                            downloadInfoAgency.setExtInfo(m3U8ExtInfo.getJson());
                            downloadDB.update(downloadInfoAgency.getInfo());
                        }

                        @Override
                        public void onConnected(long downloadId, HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName, long current, long total) {
                            downloadInfoAgency.setStatus(DownloadManager.STATUS_CONNECTED);
                            downloadInfoAgency.setFilename(saveFileName);
                            downloadInfoAgency.setTempFileName(tempFileName);
                            downloadInfoAgency.setExtInfo(m3U8ExtInfo.getJson());
                            httpInfoAgency.setByInfo(httpInfo);
                            downloadDB.update(downloadInfoAgency.getInfo());
                        }

                        @Override
                        public void onDownloadIng(long downloadId, long current, long total) {
                            downloadInfoAgency.setStatus(DownloadManager.STATUS_CONNECTED);
                            downloadInfoAgency.setCurrent(downloadInfoAgency.getCurrent() - starCurrent + current);
                            downloadInfoAgency.setExtInfo(m3U8ExtInfo.getJson());
                            downloadDB.update(downloadInfoAgency.getInfo());
                        }
                    });
                    currentItemInfo = null;
                }
                if (!isCancel) {
                    // 修改为ts为相对路径, 因为ts的uri可能是http开头的
                    PlaylistWriter playlistWriter = new PlaylistWriter(new FileOutputStream(m3u8File), Format.EXT_M3U, Encoding.UTF_8);
                    List<TrackData> tracksOutput = new ArrayList<>();
                    for (int i = 0; i < tracks.size(); i++) {
                        TrackData trackData = tracks.get(i);
                        tracksOutput.add(trackData.buildUpon().withUri(getElementName(trackData.getUri(), i)).build());
                    }
                    Playlist playlistOutput = playlist.buildUpon()
                            .withMediaPlaylist(mediaPlaylist.buildUpon().withTracks(tracksOutput).build())
                            .build();
                    playlistWriter.write(playlistOutput);
                }
            }
        }
        return null;
    }

    @Override
    public void cancel() {
        isCancel = true;
        if (downloadTask != null) {
            downloadTask.cancel();
        }
    }

    private String getTsUrl(String tsUri) {
        if (tsUri.startsWith("http://") || tsUri.startsWith("https://"))
            return tsUri;
        return downloadInfo.getUrl() + tsUri;
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
        downloadInfoAgency.setDir(downloadParams.getDir() + File.separator + downloadId + "_" + downloadInfoAgency.getStartTime());
        return downloadInfoAgency;
    }

    private M3u8ExtInfo.ItemInfo buildItemInfo(int index, TrackData trackData) {
        String url = getTsUrl(trackData.getUri());
        M3u8ExtInfo.ItemInfo currentItemInfo = new M3u8ExtInfo.ItemInfo();
        currentItemInfo.setCurrent(0);
        currentItemInfo.setTotal(0);
        currentItemInfo.setIndex(index);
        String elementName = getElementName(trackData.getUri(), index);
        currentItemInfo.setFileName(elementName);
        currentItemInfo.setUrl(url);
        return currentItemInfo;
    }

    private DownloadTaskImp buildFromExtInfo(long downloadId, String dir, DownloadParams downloadParams, M3u8ExtInfo.ItemInfo itemInfo) {
        return new DownloadTaskImp(downloadId, downloadParams, itemInfo.getUrl(), dir, itemInfo.getCurrent(), itemInfo.getTotal(), itemInfo.getFileName(), itemInfo.getTempFileName(), downloadInfo.getHttpInfo().isAcceptRange(), downloadInfo.getHttpInfo().getETag());
    }

    private String getElementName(String uri, int elementIndex) {
        String path = URI.create(uri).getPath();
        int index = path.lastIndexOf("/");
        if (index > 0) {
            path = path.substring(index, path.length());
        }
        return FileNameUtils.toValidFileName(elementIndex + "_" + path);
    }

//    String url = currentItemDownloadInfoJson.optString("url");
//    String tempFileName = currentItemDownloadInfoJson.optString("tempFileName");
//    String saveFileName = currentItemDownloadInfoJson.optString("saveFileName");
//    long current = currentItemDownloadInfoJson.optLong("current");
//    long total = currentItemDownloadInfoJson.optLong("total");
}
