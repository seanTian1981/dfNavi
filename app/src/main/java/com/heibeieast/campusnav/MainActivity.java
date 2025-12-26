package com.heibeieast.campusnav;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.heibeieast.campusnav.services.DatabaseService;
import com.heibeieast.campusnav.services.VoiceService;
import com.heibeieast.campusnav.utils.PermissionManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Button btnNavigation;
    private Button btnManageLocations;
    private Button btnSettings;
    private TextView tvAppInfo;

    private DatabaseService databaseService;
    private VoiceService voiceService;
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize services
        databaseService = DatabaseService.getInstance(this);
        voiceService = new VoiceService(this);
        permissionManager = new PermissionManager(this);

        // Initialize views
        initializeViews();

        // Request permissions
        requestPermissions();

        // Initialize TTS
        voiceService.initializeTTS();
    }

    private void initializeViews() {
        btnNavigation = findViewById(R.id.btnNavigation);
        btnManageLocations = findViewById(R.id.btnManageLocations);
        btnSettings = findViewById(R.id.btnSettings);
        tvAppInfo = findViewById(R.id.tvAppInfo);

        // Set app info
        String appInfo = getString(R.string.app_name) + " v" + getString(R.string.app_version);
        tvAppInfo.setText(appInfo);
        tvAppInfo.setContentDescription(appInfo);

        // Navigation button
        btnNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NavigationActivity.class));
            }
        });

        // Manage locations button
        btnManageLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LocationManagementActivity.class));
            }
        });

        // Settings button
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
    }

    private void requestPermissions() {
        permissionManager.setCallback(new PermissionManager.PermissionCallback() {
            @Override
            public void onPermissionGranted(String permission) {
                // Permission granted
            }

            @Override
            public void onPermissionDenied(String permission) {
                // Permission denied
            }

            @Override
            public void onAllPermissionsGranted() {
                // All permissions granted
            }
        });

        permissionManager.checkAndRequestAllPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionManager != null) {
            permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh TTS settings in case they were changed
        if (voiceService != null) {
            voiceService.updateSettings();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop any ongoing speech
        if (voiceService != null) {
            voiceService.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceService != null) {
            voiceService.shutdown();
        }
    }
}
