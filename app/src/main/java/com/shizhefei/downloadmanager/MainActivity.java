package com.shizhefei.downloadmanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.base.DownloadParams;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.imp.DownloadManager;
import com.shizhefei.download.imp.LocalDownloadManager;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View addButton;
    private EditText editText;
    private DataAdapter dataAdapter;
    private LocalDownloadManager downloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        downloadManager = DownloadManager.getLocal();
        downloadManager.registerDownloadListener(downloadListener);

        recyclerView = findViewById(R.id.recyclerView);
        addButton = findViewById(R.id.button);
        editText = findViewById(R.id.editText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(dataAdapter = new DataAdapter(downloadManager));

        addButton.setOnClickListener(onClickListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downloadManager.unregisterDownloadListener(downloadListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == addButton) {
                String url = editText.getText().toString();
                DownloadParams downloadParams = new DownloadParams.Builder()
                        .setUrl(url)
                        .build();
                downloadManager.start(downloadParams);
            }
        }
    };

    private DownloadListener downloadListener = new DownloadListener() {

        @Override
        public void onPending(long downloadId) {
            super.onPending(downloadId);
            dataAdapter.notifyDataSetChanged();
        }

        @Override
        public void onStart(long downloadId) {
            super.onStart(downloadId);
            int position = downloadManager.getDownloadCursor().getPosition(downloadId);
            dataAdapter.notifyItemChanged(position);
        }

        @Override
        public void onDownloadIng(long downloadId, long current, long total) {
            super.onDownloadIng(downloadId, current, total);
            int position = downloadManager.getDownloadCursor().getPosition(downloadId);
            dataAdapter.notifyItemChanged(position);
        }

        @Override
        public void onConnected(long downloadId, HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName) {
            super.onConnected(downloadId, httpInfo, saveDir, saveFileName, tempFileName);
            int position = downloadManager.getDownloadCursor().getPosition(downloadId);
            dataAdapter.notifyItemChanged(position);
        }

        @Override
        public void onError(long downloadId, int errorCode, String errorMessage) {
            int position = downloadManager.getDownloadCursor().getPosition(downloadId);
            dataAdapter.notifyItemChanged(position);
        }

        @Override
        public void onComplete(long downloadId) {
            int position = downloadManager.getDownloadCursor().getPosition(downloadId);
            dataAdapter.notifyItemChanged(position);
        }

        @Override
        public void onPaused(long downloadId) {
            super.onPaused(downloadId);
            int position = downloadManager.getDownloadCursor().getPosition(downloadId);
            dataAdapter.notifyItemChanged(position);
        }

        @Override
        public void onRemove(long downloadId) {
            super.onRemove(downloadId);
            dataAdapter.notifyDataSetChanged();
        }
    };
}
