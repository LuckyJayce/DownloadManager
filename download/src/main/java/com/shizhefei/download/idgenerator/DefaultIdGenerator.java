package com.shizhefei.download.idgenerator;

import android.content.Context;
import android.content.SharedPreferences;

import com.shizhefei.download.base.IdGenerator;
import com.shizhefei.download.entity.DownloadParams;

public class DefaultIdGenerator implements IdGenerator {
    private SharedPreferences sharedPreferences;
    private long maxDownloadId = -1;
    private Context context;

    public DefaultIdGenerator(Context context) {
        this.context = context;
    }

    @Override
    public long generateId(DownloadParams downloadParams) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("download_config", 0);
            maxDownloadId = sharedPreferences.getLong("maxDownloadId", 1);
        }
        maxDownloadId++;
        sharedPreferences.edit().putLong("maxDownloadId", maxDownloadId).apply();
        return maxDownloadId;
    }
}
