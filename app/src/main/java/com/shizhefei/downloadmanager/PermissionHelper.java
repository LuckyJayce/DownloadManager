package com.shizhefei.downloadmanager;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionHelper {

    private int code = 160;
    private SparseArray<OnCheckCallback> onCheckCallbacks = new SparseArray<>();
    private Activity activity;

    public PermissionHelper(Activity activity) {
        this.activity = activity;
    }

    public boolean checkSelfPermission(String permission) {
        return checkSelfPermission(new String[]{permission});
    }

    public boolean checkSelfPermission(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void checkAndRequestPermission(String[] permissions, OnCheckCallback onCheckCallback) {
        if (!checkSelfPermission(permissions)) {
            requestPermissions(permissions, onCheckCallback);
        } else {
            onCheckCallback.onSuccess(Arrays.asList(permissions));
        }
    }

    public void checkAndRequestPermission(String permission, OnCheckCallback onCheckCallback) {
        checkAndRequestPermission(new String[]{permission}, onCheckCallback);
    }

    public void requestPermissions(String[] permissions, OnCheckCallback onCheckCallback) {
        onCheckCallbacks.put(code, onCheckCallback);
        ActivityCompat.requestPermissions(activity, permissions, code);
        code++;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        OnCheckCallback onCheckCallback = onCheckCallbacks.get(requestCode);
        if (onCheckCallback != null) {
            onCheckCallbacks.remove(requestCode);
            List<String> successPermissions = new ArrayList<>();
            List<String> failPermissions = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    successPermissions.add(permissions[i]);
                } else {
                    failPermissions.add(permissions[i]);
                }
            }
            if (failPermissions.isEmpty()) {
                onCheckCallback.onSuccess(successPermissions);
            } else {
                onCheckCallback.onFail(successPermissions, failPermissions);
            }
        }
    }

    public interface OnCheckCallback {
        void onFail(List<String> successPermissions, List<String> failPermissions);

        void onSuccess(List<String> successPermissions);
    }
}
