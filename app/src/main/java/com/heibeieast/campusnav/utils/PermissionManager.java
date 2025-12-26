package com.heibeieast.campusnav.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int AUDIO_PERMISSION_REQUEST_CODE = 1002;
    private static final int ALL_PERMISSIONS_REQUEST_CODE = 1003;

    private Activity activity;
    private PermissionCallback callback;

    public interface PermissionCallback {
        void onPermissionGranted(String permission);
        void onPermissionDenied(String permission);
        void onAllPermissionsGranted();
    }

    public PermissionManager(Activity activity) {
        this.activity = activity;
    }

    public void setCallback(PermissionCallback callback) {
        this.callback = callback;
    }

    /**
     * Check if location permission is granted
     */
    public boolean checkLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if audio permission is granted
     */
    public boolean checkAudioPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check specific permission status
     */
    public int checkPermissionStatus(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission);
    }

    /**
     * Check and request location permission
     * @return true if permission is already granted, false otherwise
     */
    public boolean checkAndRequestLocationPermission() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (!checkLocationPermission(activity)) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
        }

        if (permissionsNeeded.isEmpty()) {
            if (callback != null) {
                callback.onPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            return true;
        }

        ActivityCompat.requestPermissions(
                activity,
                permissionsNeeded.toArray(new String[0]),
                LOCATION_PERMISSION_REQUEST_CODE
        );

        return false;
    }

    /**
     * Check and request audio permission
     * @return true if permission is already granted, false otherwise
     */
    public boolean checkAndRequestAudioPermission() {
        if (checkAudioPermission(activity)) {
            if (callback != null) {
                callback.onPermissionGranted(Manifest.permission.RECORD_AUDIO);
            }
            return true;
        }

        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.RECORD_AUDIO},
                AUDIO_PERMISSION_REQUEST_CODE
        );

        return false;
    }

    /**
     * Check and request all required permissions
     * @return true if all permissions are already granted, false otherwise
     */
    public boolean checkAndRequestAllPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // Location permissions
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        // Audio permission
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }

        if (permissionsNeeded.isEmpty()) {
            if (callback != null) {
                callback.onAllPermissionsGranted();
            }
            return true;
        }

        ActivityCompat.requestPermissions(
                activity,
                permissionsNeeded.toArray(new String[0]),
                ALL_PERMISSIONS_REQUEST_CODE
        );

        return false;
    }

    /**
     * Handle permission request result
     * Call this in Activity's onRequestPermissionsResult
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            handleLocationPermissionsResult(permissions, grantResults);
        } else if (requestCode == AUDIO_PERMISSION_REQUEST_CODE) {
            handleAudioPermissionsResult(permissions, grantResults);
        } else if (requestCode == ALL_PERMISSIONS_REQUEST_CODE) {
            handleAllPermissionsResult(permissions, grantResults);
        }
    }

    private void handleLocationPermissionsResult(String[] permissions, int[] grantResults) {
        boolean allGranted = true;

        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                if (callback != null) {
                    callback.onPermissionDenied(permissions[i]);
                }
            } else if (callback != null) {
                callback.onPermissionGranted(permissions[i]);
            }
        }

        if (allGranted && callback != null) {
            callback.onAllPermissionsGranted();
        }
    }

    private void handleAudioPermissionsResult(String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (callback != null) {
                callback.onPermissionGranted(Manifest.permission.RECORD_AUDIO);
            }
        } else if (callback != null) {
            callback.onPermissionDenied(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void handleAllPermissionsResult(String[] permissions, int[] grantResults) {
        boolean allGranted = true;

        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                if (callback != null) {
                    callback.onPermissionDenied(permissions[i]);
                }
            } else if (callback != null) {
                callback.onPermissionGranted(permissions[i]);
            }
        }

        if (allGranted && callback != null) {
            callback.onAllPermissionsGranted();
        }
    }

    /**
     * Check if user should show permission rationale
     */
    public boolean shouldShowRationale(String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    /**
     * Check if all required permissions are granted
     */
    public boolean areAllPermissionsGranted() {
        return checkLocationPermission(activity) && checkAudioPermission(activity);
    }

    /**
     * Get list of denied permissions
     */
    public List<String> getDeniedPermissions() {
        List<String> deniedPermissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            deniedPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            deniedPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            deniedPermissions.add(Manifest.permission.RECORD_AUDIO);
        }

        return deniedPermissions;
    }

    /**
     * Check if background location permission is needed (Android 10+)
     */
    public boolean needsBackgroundLocationPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    /**
     * Request background location permission
     */
    public void requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    1004
            );
        }
    }
}
