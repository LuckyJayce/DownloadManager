package com.shizhefei.downloadmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.imp.DownloadCursor;
import com.shizhefei.download.imp.DownloadManager;

import java.io.File;

public class DataAdapter extends RecyclerView.Adapter {

    private final DownloadManager downloadManager;
    private final DownloadCursor downloadCursor;

    public DataAdapter(DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
        downloadCursor = downloadManager.getDownloadCursor();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        return new ItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_download, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        DownloadInfo downloadInfo = downloadCursor.getDownloadInfo(position);
        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        itemViewHolder.setData(downloadInfo);
    }

    @Override
    public int getItemCount() {
        return downloadCursor.getCount();
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private final TextView fileNameTextView;
        private final ProgressBar progressBar;
        private final TextView infoTextView;
        private final View pauseButton;
        private final View removeButton;
        private DownloadInfo downloadInfo;
        private Context context;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.context = itemView.getContext();
            fileNameTextView = itemView.findViewById(R.id.item_download_filename_textView);
            progressBar = itemView.findViewById(R.id.item_download_progressBar);
            infoTextView = itemView.findViewById(R.id.item_download_info_textView);
            pauseButton = itemView.findViewById(R.id.item_download_pasuse_button);
            removeButton = itemView.findViewById(R.id.item_download_remove_button);

            removeButton.setOnClickListener(onClickListener);
            pauseButton.setOnClickListener(onClickListener);
        }

        public void setData(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
            String tempPath = downloadInfo.getDir() + File.separator + downloadInfo.getTempFileName();
            fileNameTextView.setText(tempPath + " / " + downloadInfo.getFilename() + "\n" + downloadInfo.getUrl());
            int p;
            if (downloadInfo.getTotal() <= 0) {
                p = 0;
            } else {
                p = (int) (1.0 * downloadInfo.getCurrent() / downloadInfo.getTotal() * 100);
            }
            progressBar.setProgress(p);
            StringBuilder info = new StringBuilder();
            info.append(Formatter.formatFileSize(context, downloadInfo.getCurrent())).append("/").append(Formatter.formatFileSize(context, downloadInfo.getTotal()));
            info.append("  %").append(p);
            info.append("\nerror:").append(downloadInfo.getErrorInfo().toJson());
            infoTextView.setText(info);
        }

        private View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == removeButton) {
                    downloadManager.remove(downloadInfo.getId());
                } else if (v == pauseButton) {
                    downloadManager.pause(downloadInfo.getId());
                }
            }
        };
    }
}