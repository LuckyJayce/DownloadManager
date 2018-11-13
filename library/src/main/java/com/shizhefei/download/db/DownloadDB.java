package com.shizhefei.download.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.shizhefei.download.base.DownloadParams;
import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.utils.DownloadLogUtils;
import com.shizhefei.mvc.ProgressSender;
import com.shizhefei.task.ITask;
import com.shizhefei.task.TaskHelper;

import java.util.concurrent.Executor;

public class DownloadDB {
    private static final int DB_VERSION = 1;
    private static final String SQL_SELECT = "select * from " + DBHelper.TABLE_NAME + " where " + DBHelper.FIELD_KEY + "=?";
    private final Executor executor;
    private final Context context;
    private final DBHelper dbHelper;
    private TaskHelper taskHelper;
    private static int downloadId;

    public DownloadDB(Context context, Executor executor) {
        this.context = context;
        this.executor = executor;
        dbHelper = new DBHelper(context, "DownloadDB", null, DB_VERSION);
        taskHelper = new TaskHelper();
        taskHelper.setThreadExecutor(executor);
        //TODO 查找出当前最大的id
    }

    @Nullable
    public Pair<DownloadInfo.Agency, DownloadParams> find(long downloadId) {
        Pair<DownloadInfo.Agency, DownloadParams> paramsPair = null;
        try {
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            Cursor cursor = database.rawQuery(SQL_SELECT, new String[]{String.valueOf(downloadId)});
            if (cursor.moveToNext()) {
                String downloadParams = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_DOWNLOAD_PARAMS));
                String dir = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_DIR));
                String fileName = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_FILENAME));
                String tempFileName = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_TEMP_FILENAME));
                String httpInfo = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_HTTP_INFO));
                String errorInfo = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_ERROR_INFO));
                String extInfo = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_EXT_INFO));
                String url = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_URL));
                int status = cursor.getInt(cursor.getColumnIndex(DBHelper.FIELD_STATUS));
                long startTime = cursor.getLong(cursor.getColumnIndex(DBHelper.FIELD_START_TIME));
                long current = cursor.getLong(cursor.getColumnIndex(DBHelper.FIELD_CURRENT));
                long total = cursor.getLong(cursor.getColumnIndex(DBHelper.FIELD_TOTAL));

                DownloadInfo.Agency agency = new DownloadInfo.Agency();
                agency.setCurrent(current);
                agency.setStatus(status);
                agency.setTempFileName(tempFileName);
                agency.setFilename(fileName);
                agency.setTotal(total);
                agency.setDir(dir);
                agency.setUrl(url);
                agency.setId(downloadId);
                agency.setStartTime(startTime);
                agency.getHttpInfo().setByJson(httpInfo);
                agency.getErrorInfo().setByJson(errorInfo);
                agency.setExtInfo(extInfo);
                DownloadParams downloadParams1 = new DownloadParams();
                downloadParams1.setByJson(downloadParams);
                paramsPair = new Pair<>(agency, downloadParams1);
            }
            cursor.close();
        } catch (Exception e) {
            DownloadLogUtils.e(e, "find downloadId={}", downloadId);
        }
        return paramsPair;
    }

    public void update(DownloadInfo downloadInfo) {
        taskHelper.execute(new UpdateTask(dbHelper, downloadInfo), null);
    }

    public void update(long downloadId, long current, long total) {
        taskHelper.execute(new UpdateProgressTask(dbHelper, downloadId, current, total), null);
    }

    public long addAndGetDownloadId() {
        downloadId++;
        return downloadId;
    }

    public void replace(DownloadParams downloadParams, DownloadInfo downloadInfo) {
        taskHelper.execute(new ReplaceTask(dbHelper, downloadInfo, downloadParams), null);
    }

    public void delete(long downloadId) {
        taskHelper.execute(new DeleteTask(dbHelper, downloadId), null);
    }

    private static class UpdateProgressTask implements ITask<Void> {
        private final long current;
        private final long total;
        private final long downloadId;
        private DBHelper dbHelper;

        public UpdateProgressTask(DBHelper dbHelper, long downloadId, long current, long total) {
            this.dbHelper = dbHelper;
            this.downloadId = downloadId;
            this.current = current;
            this.total = total;
        }

        @Override
        public Void execute(ProgressSender progressSender) throws Exception {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.FIELD_CURRENT, current);
            contentValues.put(DBHelper.FIELD_TOTAL, total);
            database.update(DBHelper.TABLE_NAME, contentValues, DBHelper.FIELD_KEY + " = ?", new String[]{String.valueOf(downloadId)});
            return null;
        }

        @Override
        public void cancel() {

        }
    }

    private static class UpdateTask implements ITask<Void> {
        private DBHelper dbHelper;
        private DownloadInfo downloadInfo;

        public UpdateTask(DBHelper dbHelper, DownloadInfo downloadInfo) {
            this.dbHelper = dbHelper;
            this.downloadInfo = downloadInfo;
        }

        @Override
        public Void execute(ProgressSender progressSender) throws Exception {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.FIELD_STATUS, downloadInfo.getStatus());
            contentValues.put(DBHelper.FIELD_DIR, downloadInfo.getCurrent());
            contentValues.put(DBHelper.FIELD_FILENAME, downloadInfo.getFilename());
            contentValues.put(DBHelper.FIELD_TEMP_FILENAME, downloadInfo.getTempFileName());
            contentValues.put(DBHelper.FIELD_HTTP_INFO, downloadInfo.getHttpInfo().toJson());
            contentValues.put(DBHelper.FIELD_ERROR_INFO, downloadInfo.getErrorInfo().toJson());
            contentValues.put(DBHelper.FIELD_EXT_INFO, downloadInfo.getExtInfo());
            contentValues.put(DBHelper.FIELD_URL, downloadInfo.getUrl());
            contentValues.put(DBHelper.FIELD_START_TIME, downloadInfo.getStartTime());
            contentValues.put(DBHelper.FIELD_CURRENT, downloadInfo.getCurrent());
            contentValues.put(DBHelper.FIELD_TOTAL, downloadInfo.getTotal());
            database.update(DBHelper.TABLE_NAME, contentValues, DBHelper.FIELD_KEY + " = ?", new String[]{String.valueOf(downloadInfo.getId())});
            return null;
        }

        @Override
        public void cancel() {

        }
    }

    private static class ReplaceTask implements ITask<Void> {
        private final DownloadParams downloadParams;
        private DBHelper dbHelper;
        private DownloadInfo downloadInfo;

        public ReplaceTask(DBHelper dbHelper, DownloadInfo downloadInfo, DownloadParams downloadParams) {
            this.dbHelper = dbHelper;
            this.downloadParams = downloadParams;
            this.downloadInfo = downloadInfo;
        }

        @Override
        public Void execute(ProgressSender progressSender) throws Exception {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.FIELD_KEY, downloadInfo.getId());
            contentValues.put(DBHelper.FIELD_DOWNLOAD_PARAMS, downloadParams.toJson());
            contentValues.put(DBHelper.FIELD_STATUS, downloadInfo.getStatus());
            contentValues.put(DBHelper.FIELD_DIR, downloadInfo.getCurrent());
            contentValues.put(DBHelper.FIELD_FILENAME, downloadInfo.getFilename());
            contentValues.put(DBHelper.FIELD_TEMP_FILENAME, downloadInfo.getTempFileName());
            contentValues.put(DBHelper.FIELD_HTTP_INFO, downloadInfo.getHttpInfo().toJson());
            contentValues.put(DBHelper.FIELD_ERROR_INFO, downloadInfo.getErrorInfo().toJson());
            contentValues.put(DBHelper.FIELD_EXT_INFO, downloadInfo.getExtInfo());
            contentValues.put(DBHelper.FIELD_URL, downloadInfo.getUrl());
            contentValues.put(DBHelper.FIELD_START_TIME, downloadInfo.getStartTime());
            contentValues.put(DBHelper.FIELD_CURRENT, downloadInfo.getCurrent());
            contentValues.put(DBHelper.FIELD_TOTAL, downloadInfo.getTotal());
            database.replace(DBHelper.TABLE_NAME, null, contentValues);
            return null;
        }

        @Override
        public void cancel() {

        }
    }

    private static class DeleteTask implements ITask<Void> {
        private DBHelper dbHelper;
        private long downloadId;

        public DeleteTask(DBHelper dbHelper, long downloadId) {
            this.dbHelper = dbHelper;
            this.downloadId = downloadId;
        }

        @Override
        public Void execute(ProgressSender progressSender) throws Exception {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            database.delete(DBHelper.TABLE_NAME, DBHelper.FIELD_KEY + " = ?", new String[]{String.valueOf(downloadId)});
            return null;
        }

        @Override
        public void cancel() {

        }
    }
}
