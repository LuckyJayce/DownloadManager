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

    public static String getFileName(File dir, String userSetFileName, String urlString, String contentType, String headContentDisposition, long downloadId) {
        String userSetPreName = null;
        String filename;
        String preName = "";
        String endName = "";
        if (!TextUtils.isEmpty(userSetFileName)) {
            if (userSetFileName.contains(".")) {
                return userSetFileName;
            } else {
                userSetPreName = userSetFileName;
                preName = userSetPreName;
            }
        }
        //后期还可以再优化 endName判断是否是常用格式，来更精准定位哪个才是真正endName进行选取
        String dispositionName = getNameFromDisposition(headContentDisposition);
        if (!TextUtils.isEmpty(dispositionName)) {
            int index = dispositionName.lastIndexOf(".");
            if (index > 0) {
                if (TextUtils.isEmpty(preName)) {
                    preName = dispositionName.substring(0, index);
                }
                if (TextUtils.isEmpty(endName)) {
                    endName = dispositionName.substring(index, dispositionName.length());
                }
            } else {
                if (TextUtils.isEmpty(preName)) {
                    preName = dispositionName;
                }
            }
        }
        String urlName = getFileNameFromUrl(urlString);
        DownloadLogUtils.d("downloadId:%d  getFileName() urlName:%s", downloadId, urlName);
        if (urlName != null) {
            int index = urlName.lastIndexOf(".");
            if (index >= 0) {
                if (TextUtils.isEmpty(preName) || TextUtils.isEmpty(userSetPreName)) {
                    preName = urlName.substring(0, index);
                }
                if (TextUtils.isEmpty(endName)) {
                    endName = urlName.substring(index, urlName.length());
                }
            } else {
                if (TextUtils.isEmpty(preName) || TextUtils.isEmpty(userSetPreName)) {
                    preName = urlName;
                }
            }
        }
        if (TextUtils.isEmpty(endName)) {
            if (!TextUtils.isEmpty(contentType)) {
                String e = getEndName(contentType);
                if (!TextUtils.isEmpty(e)) {
                    endName = "." + e;
                }
            }
        }
        if (TextUtils.isEmpty(preName)) {
            preName = downloadId + "_" + String.valueOf(System.currentTimeMillis());
        } else {
            if (!preName.equals(userSetPreName)) {
                preName = downloadId + "_" + preName;
            }
        }
        filename = preName + endName;
        return toValidFileName(dir, filename);
    }

    private static String getFileNameFromUrl(String url) {
        int fragment = url.lastIndexOf('#');
        if (fragment > 0) {
            url = url.substring(0, fragment);
        }

        int query = url.lastIndexOf('?');
        if (query > 0) {
            url = url.substring(0, query);
        }

        int filenamePos = url.lastIndexOf('/');
        String filename =
                0 <= filenamePos ? url.substring(filenamePos + 1) : url;
        return filename;
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
