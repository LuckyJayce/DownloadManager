package com.shizhefei.downloadmanager;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.manager.DownloadManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View addButton;
    private EditText editText;
    private DataAdapter dataAdapter;
    private DownloadManager downloadManager;
    private PermissionHelper permissionHelper;
    private View pauseAllButton;
    private View addButton2;
    private EditText editText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        downloadManager = DownloadManager.getLocal();
        downloadManager.registerDownloadListener(downloadListener);

        recyclerView = findViewById(R.id.recyclerView);
        addButton = findViewById(R.id.button);
        editText = findViewById(R.id.editText);
        addButton2 = findViewById(R.id.button2);
        editText2 = findViewById(R.id.editText2);
        pauseAllButton = findViewById(R.id.pause_all_button);

        recyclerView.setLayoutManager(new FixLinearLayoutManager(this));
        recyclerView.setAdapter(dataAdapter = new DataAdapter(downloadManager));

        addButton.setOnClickListener(onClickListener);
        addButton2.setOnClickListener(onClickListener);
        pauseAllButton.setOnClickListener(onClickListener);

        permissionHelper = new PermissionHelper(this);

        findViewById(R.id.play_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), PlayActivity.class));
            }
        });
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
                permissionHelper.checkAndRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionHelper.OnCheckCallback() {
                    @Override
                    public void onFail(List<String> successPermissions, List<String> failPermissions) {
                        Toast.makeText(getApplicationContext(), getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess(List<String> successPermissions) {
                        String url = editText.getText().toString();
                        DownloadParams downloadParams = new DownloadParams.Builder()
                                .setUrl(url)
                                .build();
                        downloadManager.start(downloadParams);
                    }
                });
            } else if (v == addButton2) {
                permissionHelper.checkAndRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionHelper.OnCheckCallback() {
                    @Override
                    public void onFail(List<String> successPermissions, List<String> failPermissions) {
                        Toast.makeText(getApplicationContext(), getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess(List<String> successPermissions) {
                        String url = editText2.getText().toString();
                        DownloadParams downloadParams = new DownloadParams.Builder()
                                .setUrl(url)
                                .build();
                        downloadManager.start(downloadParams);
                    }
                });
            } else if (v == pauseAllButton) {
                downloadManager.pauseAll();
            }
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private DownloadListener downloadListener = new DownloadListener() {

        @Override
        public void onPending(long downloadId) {
            super.onPending(downloadId);
            dataAdapter.notifyDataSetChanged();
        }

        @Override
        public void onStart(long downloadId, long current, long total) {
            super.onStart(downloadId, current, total);
            int position = downloadManager.getDownloadInfoList().getPosition(downloadId);
            dataAdapter.notifyItemChanged(position);
        }

        @Override
        public void onDownloadIng(long downloadId, long current, long total) {
            super.onDownloadIng(downloadId, current, total);
            int position = downloadManager.getDownloadInfoList().getPosition(downloadId);
            dataAdapter.notifyItemChanged(position);
        }

        @Override
        public void onConnected(long downloadId, HttpInfo httpInfo, String saveDir, String saveFileName, String tempFileName, long current, long total) {
            super.onConnected(downloadId, httpInfo, saveDir, saveFileName, tempFileName, current, total);
            int position = downloadManager.getDownloadInfoList().getPosition(downloadId);
            dataAdapter.notifyItemChanged(position);
        }

        @Override
        public void onError(long downloadId, int errorCode, String errorMessage) {
            int position = downloadManager.getDownloadInfoList().getPosition(downloadId);
            dataAdapter.notifyItemChanged(position);
        }

        @Override
        public void onComplete(long downloadId) {
            int position = downloadManager.getDownloadInfoList().getPosition(downloadId);
            dataAdapter.notifyItemChanged(position);
        }

        @Override
        public void onPaused(long downloadId) {
            super.onPaused(downloadId);
            int position = downloadManager.getDownloadInfoList().getPosition(downloadId);
            dataAdapter.notifyItemChanged(position);
        }

        @Override
        public void onDownloadResetBegin(long downloadId, int reason, long current, long total) {
            super.onDownloadResetBegin(downloadId, reason, current, total);
            int position = downloadManager.getDownloadInfoList().getPosition(downloadId);
            dataAdapter.notifyItemChanged(position);
        }

        @Override
        public void onRemove(long downloadId) {
            super.onRemove(downloadId);
            dataAdapter.notifyDataSetChanged();
        }
    };
}
