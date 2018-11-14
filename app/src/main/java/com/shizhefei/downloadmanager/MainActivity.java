package com.shizhefei.downloadmanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.manager.DownloadManager;
import com.shizhefei.download.manager.LocalDownloadManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View addButton;
    private EditText editText;
    private DataAdapter dataAdapter;
    private LocalDownloadManager downloadManager;
    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        downloadManager = DownloadManager.getLocal();
        downloadManager.registerDownloadListener(downloadListener);

        recyclerView = findViewById(R.id.recyclerView);
        addButton = findViewById(R.id.button);
        editText = findViewById(R.id.editText);

        recyclerView.setLayoutManager(new FixLinearLayoutManager(this));
        recyclerView.setAdapter(dataAdapter = new DataAdapter(downloadManager));

        addButton.setOnClickListener(onClickListener);
        permissionHelper = new PermissionHelper(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downloadManager.unregisterDownloadListener(downloadListener);
    }

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 324;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == addButton) {
//                String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
//                if (!permissionHelper.checkSelfPermission(permissions)) {
//                    permissionHelper.requestPermissions(permissions, new PermissionHelper.OnCheckCallback() {
//                        @Override
//                        public void onFail(List<String> successPermissions, List<String> failPermissions) {
//                            Log.d("tttt", "onFail successPermissions:" + new Gson().toJson(successPermissions) + " failPermissions:" + failPermissions);
//                        }
//
//                        @Override
//                        public void onSuccess(List<String> successPermissions) {
//                            Log.d("tttt", "onSuccess successPermissions:" + new Gson().toJson(successPermissions));
//                        }
//                    });
//                }
////
                String url = editText.getText().toString();
                DownloadParams downloadParams = new DownloadParams.Builder()
                        .setUrl(url)
                        .build();
                downloadManager.start(downloadParams);
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
        public void onDownloadResetBegin(long downloadId, int reason) {
            super.onDownloadResetBegin(downloadId, reason);
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
