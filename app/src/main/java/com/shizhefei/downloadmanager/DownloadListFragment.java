package com.shizhefei.downloadmanager;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.shizhefei.download.base.DownloadInfoList;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.manager.DownloadManager;
import com.shizhefei.fragment.LazyFragment;

public class DownloadListFragment extends LazyFragment {
    private DownloadManager downloadManager;
    private DataAdapter dataAdapter;
    public static final String EXTRA_INT_DOWNLOAD_STATUS = "extra_int_download_status";
    private int status;
    private DownloadInfoList downloadInfoList;
    private RecyclerView recyclerView;

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        setContentView(R.layout.fragment_downloadlist);
        downloadManager = DownloadManager.getRemote();

        status = getArguments().getInt(EXTRA_INT_DOWNLOAD_STATUS);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        downloadInfoList = downloadManager.createDownloadInfoList(status);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
       new Handler().postDelayed(new Runnable() {
           @Override
           public void run() {
               recyclerView.setAdapter(dataAdapter = new DataAdapter(downloadManager, downloadInfoList));
           }
       },300);


        downloadManager.registerDownloadListener(downloadListener);
    }

    @Override
    protected void onDestroyViewLazy() {
        super.onDestroyViewLazy();
        downloadManager.unregisterDownloadListener(downloadListener);
    }

    private DownloadListener downloadListener = new DownloadListener() {

        @Override
        public void onPending(long downloadId) {
            super.onPending(downloadId);
            int position = downloadInfoList.getPosition(downloadId);
            if (position >= 0) {
                dataAdapter.notifyItemChanged(position);
            }
        }

        @Override
        public void onStart(long downloadId, long current, long total) {
            super.onStart(downloadId, current, total);
            int position = downloadInfoList.getPosition(downloadId);
            if (position >= 0) {
                dataAdapter.notifyItemChanged(position);
            }
        }

        @Override
        public void onDownloadIng(long downloadId, long current, long total) {
            super.onDownloadIng(downloadId, current, total);
            int position = downloadInfoList.getPosition(downloadId);
            if (position >= 0) {
                dataAdapter.notifyItemChanged(position);
            }
        }

        @Override
        public void onConnected(long downloadId, HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName, long current, long total) {
            super.onConnected(downloadId, httpInfo, saveDir, saveFileName, tempFileName, current, total);
            int position = downloadInfoList.getPosition(downloadId);
            if (position >= 0) {
                dataAdapter.notifyItemChanged(position);
            }
        }

        @Override
        public void onError(long downloadId, int errorCode, String errorMessage) {
            int position = downloadInfoList.getPosition(downloadId);
            if (position >= 0) {
                dataAdapter.notifyItemChanged(position);
            }
        }

        @Override
        public void onComplete(long downloadId) {
            int position = downloadInfoList.getPosition(downloadId);
            if (position >= 0) {
                dataAdapter.notifyItemChanged(position);
            }
        }

        @Override
        public void onPaused(long downloadId) {
            super.onPaused(downloadId);
            int position = downloadInfoList.getPosition(downloadId);
            if (position >= 0) {
                dataAdapter.notifyItemChanged(position);
            }
        }

        @Override
        public void onDownloadResetSchedule(long downloadId, int reason, long current, long total) {
            super.onDownloadResetSchedule(downloadId, reason, current, total);
            int position = downloadInfoList.getPosition(downloadId);
            if (position >= 0) {
                dataAdapter.notifyItemChanged(position);
            }
        }

        @Override
        public void onRemove(long downloadId) {
            super.onRemove(downloadId);
            dataAdapter.notifyDataSetChanged();
        }
    };
}
