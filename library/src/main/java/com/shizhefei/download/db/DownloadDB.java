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
import java.util.concurrent.atomic.AtomicLong;

public class DownloadDB {
    private static final int DB_VERSION = 1;
    private static final int DOWNLOAD_ID_INVALID = -1;//非法的id
    private static final int DOWNLOAD_ID_MIN = 1;//最小的id
    private static final String SQL_SELECT = "select * from " + DBHelper.TABLE_NAME + " where " + DBHelper.FIELD_KEY + "=?";
    private static final String SQL_SELECT_ALL = "select * from " + DBHelper.TABLE_NAME;
    private final DBHelper dbHelper;
    private TaskHelper taskHelper;
    private AtomicLong downloadMaxId;

    public DownloadDB(Context context, Executor executor) {
        dbHelper = new DBHelper(context, "DownloadDB", null, DB_VERSION);
        taskHelper = new TaskHelper();
        taskHelper.setThreadExecutor(executor);
        long downloadMaxId = DOWNLOAD_ID_INVALID;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        try {
            Cursor cursor = database.rawQuery("select MAX(" + DBHelper.FIELD_KEY + ") as max_id from " + DBHelper.TABLE_NAME, new String[]{String.valueOf(downloadMaxId)});
            if (cursor.moveToNext()) {
                downloadMaxId = cursor.getLong(cursor.getColumnIndex("max_id"));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (downloadMaxId == DOWNLOAD_ID_INVALID) {
            downloadMaxId = DOWNLOAD_ID_MIN;
        }
        this.downloadMaxId = new AtomicLong(downloadMaxId);
        DownloadLogUtils.d("find downloadMaxId={}", downloadMaxId);
    }

//    @Nullable
//    public DownloadInfo.Agency findAll() {
//        DownloadInfo.Agency downloadInfoAgency = null;
//        try {
//            SQLiteDatabase database = dbHelper.getReadableDatabase();
//            Cursor cursor = database.rawQuery(SQL_SELECT_ALL, new String[]{});
//            while (cursor.moveToNext()) {
//                String downloadParams = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_DOWNLOAD_PARAMS));
//                String dir = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_DIR));
//                String fileName = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_FILENAME));
//                String tempFileName = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_TEMP_FILENAME));
//                String httpInfo = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_HTTP_INFO));
//                String errorInfo = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_ERROR_INFO));
//                String extInfo = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_EXT_INFO));
//                String url = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_URL));
//                int status = cursor.getInt(cursor.getColumnIndex(DBHelper.FIELD_STATUS));
//                long startTime = cursor.getLong(cursor.getColumnIndex(DBHelper.FIELD_START_TIME));
//                long current = cursor.getLong(cursor.getColumnIndex(DBHelper.FIELD_CURRENT));
//                long total = cursor.getLong(cursor.getColumnIndex(DBHelper.FIELD_TOTAL));
//                long downloadId = cursor.getLong(cursor.getColumnIndex(DBHelper.FIELD_DIR));
//
//                DownloadInfo.Agency agency = new DownloadInfo.Agency(new DownloadParams.Builder(downloadParams).build());
//                agency.setCurrent(current);
//                agency.setStatus(status);
//                agency.setTempFileName(tempFileName);
//                agency.setFilename(fileName);
//                agency.setTotal(total);
//                agency.setDir(dir);
//                agency.setUrl(url);
//                agency.setId(downloadId);
//                agency.setStartTime(startTime);
//                agency.getHttpInfoAgency().setByJson(httpInfo);
//                agency.getErrorInfoAgency().setByJson(errorInfo);
//                agency.setExtInfo(extInfo);
//                downloadInfoAgency = agency;
//            }
//            cursor.close();
//        } catch (Exception e) {
//            DownloadLogUtils.e(e, "find error downloadId={}", downloadId);
//        }
//        return paramsPair;
//    }

    @Nullable
    public DownloadInfo.Agency find(long downloadId) {
        DownloadInfo.Agency downloadInfoAgency = null;
        try {
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            Cursor cursor = database.rawQuery(SQL_SELECT, new String[]{String.valueOf(downloadId)});
            if (cursor.moveToNext()) {
                downloadInfoAgency = get(cursor);
            }
            cursor.close();
        } catch (Exception e) {
            DownloadLogUtils.e(e, "find error downloadId={}", downloadId);
        }
        return downloadInfoAgency;
    }

    private DownloadInfo.Agency get(Cursor cursor) {
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
        long downloadId = cursor.getLong(cursor.getColumnIndex(DBHelper.FIELD_KEY));

        DownloadInfo.Agency agency = new DownloadInfo.Agency(new DownloadParams.Builder(downloadParams).build());
        agency.setId(downloadId);
        agency.setCurrent(current);
        agency.setStatus(status);
        agency.setTempFileName(tempFileName);
        agency.setFilename(fileName);
        agency.setTotal(total);
        agency.setDir(dir);
        agency.setUrl(url);
        agency.setStartTime(startTime);
        agency.getHttpInfoAgency().setByJson(httpInfo);
        agency.getErrorInfoAgency().setByJson(errorInfo);
        agency.setExtInfo(extInfo);
        return agency;
    }

    public void update(DownloadInfo downloadInfo) {
        taskHelper.execute(new UpdateTask(dbHelper, downloadInfo), null);
    }

    public void update(long downloadId, long current, long total) {
        taskHelper.execute(new UpdateProgressTask(dbHelper, downloadId, current, total), null);
    }

    public long addAndGetDownloadId() {
        return downloadMaxId.addAndGet(1);
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
