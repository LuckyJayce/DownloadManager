package com.shizhefei.download.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.shizhefei.download.entity.DownloadInfo;
import com.shizhefei.download.entity.DownloadParams;
import com.shizhefei.download.entity.ErrorInfo;
import com.shizhefei.download.manager.DownloadManager;
import com.shizhefei.download.utils.DownloadUtils;
import com.shizhefei.mvc.ProgressSender;
import com.shizhefei.task.ITask;
import com.shizhefei.task.TaskHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class DownloadDB {
    private static final int DB_VERSION = 1;
    private static final int DOWNLOAD_ID_INVALID = -1;//非法的id
    private static final int DOWNLOAD_ID_MIN = 1;//最小的id
    private static final String SQL_SELECT = "select * from " + DownloadManager.TABLE_NAME + " where " + DownloadManager.FIELD_KEY + "=?";
    private static final String SQL_SELECT_DOWNLOAD_TASK_NAME = "select " + DownloadManager.FIELD_DOWNLOAD_TASK_NAME + " from " + DownloadManager.TABLE_NAME + " where " + DownloadManager.FIELD_KEY + "=?";
    private static final String SQL_SELECT_ALL = "select * from " + DownloadManager.TABLE_NAME;
    private final DBHelper dbHelper;
    private TaskHelper taskHelper;

    public DownloadDB(Context context, Executor executor) {
        dbHelper = new DBHelper(context, "DownloadDB.db", null, DB_VERSION);
        taskHelper = new TaskHelper();
        taskHelper.setThreadExecutor(executor);
    }

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
            DownloadUtils.logE(e, "find error downloadId=%d", downloadId);
        }
        return downloadInfoAgency;
    }

    public String findDownloadTaskName(long downloadId) {
        String downloadTaskName = null;
        try {
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            Cursor cursor = database.rawQuery(SQL_SELECT_DOWNLOAD_TASK_NAME, new String[]{String.valueOf(downloadId)});
            if (cursor.moveToNext()) {
                downloadTaskName = cursor.getString(cursor.getColumnIndex(DownloadManager.FIELD_DOWNLOAD_TASK_NAME));
            }
            cursor.close();
        } catch (Exception e) {
            DownloadUtils.logE(e, "find error downloadId=%d", downloadId);
        }
        return downloadTaskName;
    }

    @NonNull
    public List<DownloadInfo.Agency> findAll() {
        List<DownloadInfo.Agency> list = new ArrayList<>();
        try {
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            Cursor cursor = database.rawQuery(SQL_SELECT_ALL, new String[]{});
            while (cursor.moveToNext()) {
                list.add(get(cursor));
            }
            cursor.close();
        } catch (Exception e) {
            DownloadUtils.logE(e, "findAll");
        }
        return list;
    }

    private DownloadInfo.Agency get(Cursor cursor) {
        String downloadParams = cursor.getString(cursor.getColumnIndex(DownloadManager.FIELD_DOWNLOAD_PARAMS));
        String dir = cursor.getString(cursor.getColumnIndex(DownloadManager.FIELD_DIR));
        String fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.FIELD_FILENAME));
        String tempFileName = cursor.getString(cursor.getColumnIndex(DownloadManager.FIELD_TEMP_FILENAME));
        String httpInfo = cursor.getString(cursor.getColumnIndex(DownloadManager.FIELD_HTTP_INFO));
        String errorInfo = cursor.getString(cursor.getColumnIndex(DownloadManager.FIELD_ERROR_INFO));
        String extInfo = cursor.getString(cursor.getColumnIndex(DownloadManager.FIELD_EXT_INFO));
        String url = cursor.getString(cursor.getColumnIndex(DownloadManager.FIELD_URL));
        String downloadTaskName = cursor.getString(cursor.getColumnIndex(DownloadManager.FIELD_DOWNLOAD_TASK_NAME));
        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.FIELD_STATUS));
        long startTime = cursor.getLong(cursor.getColumnIndex(DownloadManager.FIELD_START_TIME));
        long current = cursor.getLong(cursor.getColumnIndex(DownloadManager.FIELD_CURRENT));
        long total = cursor.getLong(cursor.getColumnIndex(DownloadManager.FIELD_TOTAL));
        long downloadId = cursor.getLong(cursor.getColumnIndex(DownloadManager.FIELD_KEY));
        long estimateTotal = cursor.getLong(cursor.getColumnIndex(DownloadManager.FIELD_ESTIMATE_TOTAL));

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
        agency.setDownloadTaskName(downloadTaskName);
        agency.setEstimateTotal(estimateTotal);
        return agency;
    }

    public void updateError(long downloadId, ErrorInfo downloadInfo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DownloadManager.FIELD_ERROR_INFO, downloadInfo.toJson());
        contentValues.put(DownloadManager.FIELD_STATUS, DownloadManager.STATUS_ERROR);
        updateContent(downloadId, contentValues);
    }

    public void updateProgress(long downloadId, long current, long total) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DownloadManager.FIELD_STATUS, DownloadManager.STATUS_PROGRESS);
        contentValues.put(DownloadManager.FIELD_CURRENT, current);
        contentValues.put(DownloadManager.FIELD_TOTAL, total);
        updateContent(downloadId, contentValues);
    }

    public void updateProgress(long downloadId, long current, long total, String extInfo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DownloadManager.FIELD_STATUS, DownloadManager.STATUS_PROGRESS);
        contentValues.put(DownloadManager.FIELD_CURRENT, current);
        contentValues.put(DownloadManager.FIELD_TOTAL, total);
        contentValues.put(DownloadManager.FIELD_ERROR_INFO, extInfo);
        updateContent(downloadId, contentValues);
    }

    public void updateDownloadResetSchedule(long downloadId, long current, long total, String extInfo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DownloadManager.FIELD_STATUS, DownloadManager.STATUS_DOWNLOAD_RESET_SCHEDULE);
        contentValues.put(DownloadManager.FIELD_CURRENT, current);
        contentValues.put(DownloadManager.FIELD_TOTAL, total);
        contentValues.put(DownloadManager.FIELD_ERROR_INFO, extInfo);
        updateContent(downloadId, contentValues);
    }

    public void updateExtInfo(long downloadId, String extInfo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DownloadManager.FIELD_ERROR_INFO, extInfo);
        updateContent(downloadId, contentValues);
    }

    public void updateStatus(long downloadId, int status) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DownloadManager.FIELD_STATUS, status);
        taskHelper.execute(new UpdateContentTask(dbHelper, downloadId, contentValues), null);
    }

    public void updateContent(long downloadId, ContentValues contentValues) {
        taskHelper.execute(new UpdateContentTask(dbHelper, downloadId, contentValues), null);
    }

    public void updateDownloadParams(long downloadId, DownloadParams downloadParams) {
        taskHelper.execute(new UpdateDownloadParamsTask(dbHelper, downloadId, downloadParams), null);
    }

    public void update(DownloadInfo downloadInfo) {
        taskHelper.execute(new UpdateTask(dbHelper, downloadInfo), null);
    }

    public void replace(DownloadParams downloadParams, DownloadInfo downloadInfo) {
        taskHelper.execute(new ReplaceTask(dbHelper, downloadInfo, downloadParams), null);
    }

    public void delete(long downloadId) {
        taskHelper.execute(new DeleteTask(dbHelper, downloadId), null);
    }

    private static class UpdateContentTask implements ITask<Void> {
        private final long downloadId;
        private final ContentValues contentValues;
        private DBHelper dbHelper;

        public UpdateContentTask(DBHelper dbHelper, long downloadId, ContentValues contentValues) {
            this.dbHelper = dbHelper;
            this.downloadId = downloadId;
            this.contentValues = contentValues;
        }

        @Override
        public Void execute(ProgressSender progressSender) throws Exception {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            long result = database.update(DownloadManager.TABLE_NAME, contentValues, DownloadManager.FIELD_KEY + " = ?", new String[]{String.valueOf(downloadId)});
            if (result <= 0) {
                DownloadUtils.logE("db update downloadId=%d result=%d", downloadId, result);
            }
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
            contentValues.put(DownloadManager.FIELD_STATUS, downloadInfo.getStatus());
            contentValues.put(DownloadManager.FIELD_DIR, downloadInfo.getDir());
            contentValues.put(DownloadManager.FIELD_FILENAME, downloadInfo.getFileName());
            contentValues.put(DownloadManager.FIELD_TEMP_FILENAME, downloadInfo.getTempFileName());
            contentValues.put(DownloadManager.FIELD_HTTP_INFO, downloadInfo.getHttpInfo().toJson());
            contentValues.put(DownloadManager.FIELD_ERROR_INFO, downloadInfo.getErrorInfo().toJson());
            contentValues.put(DownloadManager.FIELD_EXT_INFO, downloadInfo.getExtInfo());
            contentValues.put(DownloadManager.FIELD_URL, downloadInfo.getUrl());
            contentValues.put(DownloadManager.FIELD_START_TIME, downloadInfo.getStartTime());
            contentValues.put(DownloadManager.FIELD_CURRENT, downloadInfo.getCurrent());
            contentValues.put(DownloadManager.FIELD_TOTAL, downloadInfo.getTotal());
            contentValues.put(DownloadManager.FIELD_DOWNLOAD_TASK_NAME, downloadInfo.getDownloadTaskName());
            contentValues.put(DownloadManager.FIELD_ESTIMATE_TOTAL, downloadInfo.getEstimateTotal());
            int result = database.update(DownloadManager.TABLE_NAME, contentValues, DownloadManager.FIELD_KEY + " = ?", new String[]{String.valueOf(downloadInfo.getId())});
            if (result <= 0) {
                DownloadUtils.logE("db update downloadId=%d result=%d", downloadInfo.getId(), result);
            }
            return null;
        }

        @Override
        public void cancel() {

        }
    }

    private static class UpdateDownloadParamsTask implements ITask<Void> {
        private final DownloadParams downloadParams;
        private final long downloadId;
        private DBHelper dbHelper;

        public UpdateDownloadParamsTask(DBHelper dbHelper, long downloadId, DownloadParams downloadParams) {
            this.dbHelper = dbHelper;
            this.downloadParams = downloadParams;
            this.downloadId = downloadId;
        }

        @Override
        public Void execute(ProgressSender progressSender) throws Exception {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DownloadManager.FIELD_KEY, downloadId);
            contentValues.put(DownloadManager.FIELD_DOWNLOAD_PARAMS, downloadParams.toJson());
            long result = database.replace(DownloadManager.TABLE_NAME, null, contentValues);
            if (result <= 0) {
                DownloadUtils.logE("db UpdateDownloadParams downloadId=%d result=%d", downloadId, result);
            }
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
            contentValues.put(DownloadManager.FIELD_KEY, downloadInfo.getId());
            contentValues.put(DownloadManager.FIELD_DOWNLOAD_PARAMS, downloadParams.toJson());
            contentValues.put(DownloadManager.FIELD_STATUS, downloadInfo.getStatus());
            contentValues.put(DownloadManager.FIELD_DIR, downloadInfo.getDir());
            contentValues.put(DownloadManager.FIELD_FILENAME, downloadInfo.getFileName());
            contentValues.put(DownloadManager.FIELD_TEMP_FILENAME, downloadInfo.getTempFileName());
            contentValues.put(DownloadManager.FIELD_HTTP_INFO, downloadInfo.getHttpInfo().toJson());
            contentValues.put(DownloadManager.FIELD_ERROR_INFO, downloadInfo.getErrorInfo().toJson());
            contentValues.put(DownloadManager.FIELD_EXT_INFO, downloadInfo.getExtInfo());
            contentValues.put(DownloadManager.FIELD_URL, downloadInfo.getUrl());
            contentValues.put(DownloadManager.FIELD_START_TIME, downloadInfo.getStartTime());
            contentValues.put(DownloadManager.FIELD_CURRENT, downloadInfo.getCurrent());
            contentValues.put(DownloadManager.FIELD_TOTAL, downloadInfo.getTotal());
            contentValues.put(DownloadManager.FIELD_DOWNLOAD_TASK_NAME, downloadInfo.getDownloadTaskName());
            contentValues.put(DownloadManager.FIELD_ESTIMATE_TOTAL, downloadInfo.getEstimateTotal());
            long result = database.replace(DownloadManager.TABLE_NAME, null, contentValues);
            if (result <= 0) {
                DownloadUtils.logE("db replace downloadId=%d result=%d", downloadInfo.getId(), result);
            }
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
            long result = database.delete(DownloadManager.TABLE_NAME, DownloadManager.FIELD_KEY + " = ?", new String[]{String.valueOf(downloadId)});
            if (result <= 0) {
                DownloadUtils.logE("db delete downloadId=%d result=%d", downloadId, result);
            }
            return null;
        }

        @Override
        public void cancel() {

        }
    }
}
