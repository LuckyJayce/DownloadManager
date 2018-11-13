package com.shizhefei.downloadmanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

        recyclerView.setLayoutManager(new FixLinearLayoutManager(this));
        recyclerView.setAdapter(dataAdapter = new DataAdapter(downloadManager));

        addButton.setOnClickListener(onClickListener);
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
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                        Toast.makeText(getApplicationContext(), "你设置不再提醒，强制拒绝了", Toast.LENGTH_SHORT).show();
                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }

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
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "权限请求到了，万岁", Toast.LENGTH_SHORT).show();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(getApplicationContext(), "为什么要拒绝", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
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
