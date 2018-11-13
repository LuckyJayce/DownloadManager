package com.shizhefei.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DBHelper extends SQLiteOpenHelper {
    static final String TABLE_NAME = "download";

    static final String FIELD_KEY = "id";

    static final String FIELD_DOWNLOAD_PARAMS = "download_params";

    static final String FIELD_STATUS = "status";
    static final String FIELD_DIR = "dir";
    static final String FIELD_FILENAME = "filename";
    static final String FIELD_TEMP_FILENAME = "tempFileName";
    static final String FIELD_HTTP_INFO = "http_info";
    static final String FIELD_ERROR_INFO = "error_info";
    static final String FIELD_EXT_INFO = "ext_info";
    static final String FIELD_URL = "url";

    static final String FIELD_START_TIME = "start_time";
    static final String FIELD_CURRENT = "current";
    static final String FIELD_TOTAL = "total";

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + "(" +
                FIELD_KEY + " BIGINT primary key," +
                FIELD_DIR + " VARCHAR," +
                FIELD_START_TIME + " BIGINT," +
                FIELD_STATUS + " INTEGER," +
                FIELD_URL + " VARCHAR," +
                FIELD_FILENAME + " VARCHAR," +
                FIELD_TEMP_FILENAME + " VARCHAR," +
                FIELD_HTTP_INFO + " VARCHAR," +
                FIELD_ERROR_INFO + " VARCHAR," +
                FIELD_DOWNLOAD_PARAMS + " VARCHAR," +
                FIELD_EXT_INFO + " VARCHAR," +
                FIELD_CURRENT + " BIGINT," +
                FIELD_TOTAL + " BIGINT" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}