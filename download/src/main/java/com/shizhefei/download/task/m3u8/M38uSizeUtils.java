package com.shizhefei.download.task.m3u8;

import com.iheartradio.m3u8.data.TrackData;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.utils.DownloadUtils;
import com.shizhefei.download.utils.UrlBuilder;
import com.shizhefei.mvc.ProgressSender;
import com.shizhefei.task.ITask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class M38uSizeUtils {

    public static ITask<Long> totalSize(final long downloadId, final String m3u8Url, final List<TrackData> tracks, final DownloadParams downloadParams) {
        return new ITask<Long>() {
            private boolean cancel;

            @Override
            public Long execute(ProgressSender progressSender) throws Exception {
                long startTime = System.currentTimeMillis();
                TrackData track = null;
                int index = 0;
                try {
                    long totalSize = 0;
                    for (index = 0; index < tracks.size() && !cancel; index++) {
                        track = tracks.get(index);
                        long itemSize = getTsTotalSize(m3u8Url, track, downloadParams);
                        DownloadUtils.logD("M38uSizeUtils downloadId=%d index=%d uri=%s itemSize=%d", downloadId, index, track.getUri(), itemSize);
                        totalSize += itemSize;
                    }
                    DownloadUtils.logD("M38uSizeUtils downloadId=%d totalSize=%d totalTime=%d", downloadId, totalSize, (System.currentTimeMillis() - startTime));
                    if (cancel) {
                        return -1L;
                    }
                    return totalSize;
                } catch (Exception e) {
                    if (track != null) {
                        DownloadUtils.logE(e,"M38uSizeUtils downloadId=%d  index=%d uri=%s ", downloadId, index, track.getUri());
                    }else{
                        DownloadUtils.logE(e,"M38uSizeUtils downloadId=%d  index=%d ", downloadId, index);
                    }
                }
                return -1L;
            }

            @Override
            public void cancel() {
                cancel = true;
            }
        };
    }

    private static long getTsTotalSize(String m3u8Url, TrackData track, DownloadParams downloadParams) throws IOException {
        String tsUrl = M3u8DownloadTaskImp.getTsUrl(m3u8Url, track.getUri());
        Map<String, List<String>> params = downloadParams.getParams();
        String finalUrl;
        if (params != null) {
            UrlBuilder urlBuilder = new UrlBuilder(tsUrl);
            for (Map.Entry<String, List<String>> stringListEntry : params.entrySet()) {
                urlBuilder.param(stringListEntry.getKey(), stringListEntry.getValue());
            }
            finalUrl = urlBuilder.build();
        } else {
            finalUrl = tsUrl;
        }
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(finalUrl).openConnection();
        Map<String, List<String>> headers = downloadParams.getHeaders();
        if (headers != null) {
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                for (String value : header.getValue()) {
                    httpURLConnection.addRequestProperty(header.getKey(), value);
                }
            }
        }
        httpURLConnection.setRequestMethod("HEAD");
        httpURLConnection.connect();
        if (httpURLConnection.getResponseCode() == 200) {
            return httpURLConnection.getContentLength();
        }
        throw new RuntimeException("request header fail");
    }
}
