package com.shizhefei.download.utils;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;

/**
 * Created by luckyjayce on 18-3-12.
 */

public class FileNameUtils {
    private static final String DOWNLOAD = "download";

    public static String getFileName(File dir, String fileName, String urlString, String contentType, String headContentDisposition, long downloadId) {
        String preName = null;
        String endName = null;
        if (!TextUtils.isEmpty(fileName)) {
            if (fileName.contains(".")) {
                return fileName;
            } else {
                preName = fileName;
            }
        }
        fileName = getNameFromDisposition(headContentDisposition);
        if (fileName == null) {
            String urlName = getNameFromUrl(urlString);
            if (urlName != null) {
                int index = urlName.lastIndexOf(".");
                if (index >= 0) {
                    if (preName == null) {
                        preName = urlName.substring(0, index) + String.valueOf(System.currentTimeMillis());
                    }
                    endName = urlName.substring(index, urlName.length());
                } else {
                    if (preName == null) {
                        preName = urlName;
                    }
                }
            } else {
                preName = String.valueOf(downloadId);
            }
            if (!TextUtils.isEmpty(contentType)) {
                String e = getEndName(contentType);
                if (!TextUtils.isEmpty(e)) {
                    endName = "." + e;
                }
            }
            if (endName == null) {
                fileName = preName;
            } else {
                fileName = preName + endName;
            }
        } else {
            if (preName != null) {
                int index = fileName.lastIndexOf(".");
                if (index > 0) {
                    fileName = preName + fileName.substring(index, fileName.length());
                } else {
                    fileName = preName;
                }
            }
        }
        Log.i(DOWNLOAD, "fileName:" + fileName);
        return toValidFileName(dir, fileName);
    }

    /**
     * 转化为有效的文件名
     */
    public static String toValidFileName(String fileName) {
        // \ / | : * ? " < >
        return fileName == null ? fileName : fileName.replaceAll("(\\\\)|(/)|(\\|)|(:)|(\\*)|(\\?)|(\")|(<)|(>)", "_");
    }

    /**
     * 转化为有效的文件名
     */
    public static String toValidFileName(File dir, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return fileName;
        }
        // \ / | : * ? " < >
        fileName = fileName == null ? fileName : fileName.replaceAll("(\\\\)|(/)|(\\|)|(:)|(\\*)|(\\?)|(\")|(<)|(>)", "_");
        String[] names = split(fileName);
        int i = 1;
        while (new File(dir, fileName).exists()) {
            fileName = names[0] + "(" + i + ")" + names[1];
            i++;
        }
        return fileName;
    }

    public static String[] split(String fileName) {
        int i = fileName.lastIndexOf(".");
        String endName = "";
        if (i > 0) {
            endName = fileName.substring(i);
        }
        String preName = fileName.substring(0, fileName.length() - endName.length());
        return new String[]{preName, endName};
    }

    private static String getNameFromUrl(String urlString) {
        return MimeTypeMap.getFileExtensionFromUrl(urlString);
    }

    public static String getEndName(String miniType) {
        if (miniType == null)
            return null;
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(miniType);
    }

    private static String getNameFromDisposition(String disposition) {
        if (disposition != null) {
            try {
                // extracts file name from header field
                final String FILENAME = "filename=";
                final int startIdx = disposition.indexOf(FILENAME);
                final int endIdx = disposition.indexOf(';', startIdx);
                return disposition.substring(startIdx + FILENAME.length(), endIdx > 0 ? endIdx : disposition.length());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
