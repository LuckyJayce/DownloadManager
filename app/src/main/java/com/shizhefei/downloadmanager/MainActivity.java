package com.shizhefei.downloadmanager;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.shizhefei.download.base.DownloadListener;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.entity.HttpInfo;
import com.shizhefei.download.manager.DownloadManager;
import com.shizhefei.view.indicator.FixedIndicatorView;
import com.shizhefei.view.indicator.Indicator;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.slidebar.TextWidthColorBar;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private View addButton;
    private EditText editText;
    private DownloadManager downloadManager;
    private PermissionHelper permissionHelper;
    private View pauseAllButton;
    private View addButton2;
    private EditText editText2;
    private ViewPager viewPager;
    private Indicator indicatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        downloadManager = DownloadManager.getLocal();

        addButton = findViewById(R.id.button);
        editText = findViewById(R.id.editText);
        addButton2 = findViewById(R.id.button2);
        editText2 = findViewById(R.id.editText2);
        pauseAllButton = findViewById(R.id.pause_all_button);

        addButton.setOnClickListener(onClickListener);
        addButton2.setOnClickListener(onClickListener);
        pauseAllButton.setOnClickListener(onClickListener);

        permissionHelper = new PermissionHelper(this);

        findViewById(R.id.play_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.editText3);
                long text = Long.parseLong(editText.getText().toString());
                Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
                intent.putExtra(PlayActivity.DOWNLOAD_ID, text);
                startActivity(intent);
            }
        });

        viewPager = findViewById(R.id.viewPager);
        indicatorView = findViewById(R.id.indicator);
        indicatorView.setScrollBar(new TextWidthColorBar(this, indicatorView, Color.RED, 5));
        IndicatorViewPager indicatorViewPager = new IndicatorViewPager(indicatorView, viewPager);
        indicatorViewPager.setAdapter(new PAdapter(getSupportFragmentManager()));
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

    private class PAdapter extends IndicatorViewPager.IndicatorFragmentPagerAdapter {
        private String[] tabNames = {"下载中", "已完成"};

        public PAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return tabNames.length;
        }

        @Override
        public View getViewForTab(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = new TextView(MainActivity.this);
            }
            TextView textView = (TextView) convertView;
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            textView.setGravity(Gravity.CENTER);
            textView.setText(tabNames[position]);
            return convertView;
        }

        @Override
        public Fragment getFragmentForPage(int position) {
            Bundle bundle = new Bundle();
            switch (position) {
                case 0:
                    bundle.putInt(DownloadListFragment.EXTRA_INT_DOWNLOAD_STATUS, ~DownloadManager.STATUS_FINISHED);
                    break;
                case 1:
                    bundle.putInt(DownloadListFragment.EXTRA_INT_DOWNLOAD_STATUS, DownloadManager.STATUS_FINISHED);
                    break;
            }
            DownloadListFragment fragment = new DownloadListFragment();
            fragment.setArguments(bundle);
            return fragment;
        }
    }
}
