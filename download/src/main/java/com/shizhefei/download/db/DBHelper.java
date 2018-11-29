package com.shizhefei.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.shizhefei.download.manager.DownloadManager;

class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                DownloadManager.TABLE_NAME + "(" +
                DownloadManager.FIELD_KEY + " BIGINT primary key," +
                DownloadManager.FIELD_DIR + " VARCHAR," +
                DownloadManager.FIELD_START_TIME + " BIGINT," +
                DownloadManager.FIELD_STATUS + " INTEGER," +
                DownloadManager.FIELD_URL + " VARCHAR," +
                DownloadManager.FIELD_FILENAME + " VARCHAR," +
                DownloadManager.FIELD_TEMP_FILENAME + " VARCHAR," +
                DownloadManager.FIELD_HTTP_INFO + " VARCHAR," +
                DownloadManager.FIELD_ERROR_INFO + " VARCHAR," +
                DownloadManager.FIELD_DOWNLOAD_PARAMS + " VARCHAR," +
                DownloadManager.FIELD_EXT_INFO + " VARCHAR," +
                DownloadManager.FIELD_CURRENT + " BIGINT," +
                DownloadManager.FIELD_ESTIMATE_TOTAL + " BIGINT," +
                DownloadManager.FIELD_DOWNLOAD_TASK_NAME + " VARCHAR," +
                DownloadManager.FIELD_TOTAL + " BIGINT" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}