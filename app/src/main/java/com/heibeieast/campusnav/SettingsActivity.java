package com.heibeieast.campusnav;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.heibeieast.campusnav.services.DatabaseService;
import com.heibeieast.campusnav.services.VoiceService;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    private SeekBar seekBarStepLength;
    private SeekBar seekBarVoiceSpeed;
    private SeekBar seekBarVoiceVolume;
    private Switch switchAccessibilityMode;
    private Button btnSaveSettings;
    private Button btnResetDefaults;

    private TextView tvStepLengthValue;
    private TextView tvVoiceSpeedValue;
    private TextView tvVoiceVolumeValue;

    private DatabaseService databaseService;
    private VoiceService voiceService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize services
        databaseService = DatabaseService.getInstance(this);
        voiceService = new VoiceService(this);

        // Initialize views
        initializeViews();

        // Load current settings
        loadSettings();

        // Initialize TTS
        voiceService.initializeTTS();
    }

    private void initializeViews() {
        seekBarStepLength = findViewById(R.id.seekBarStepLength);
        seekBarVoiceSpeed = findViewById(R.id.seekBarVoiceSpeed);
        seekBarVoiceVolume = findViewById(R.id.seekBarVoiceVolume);
        switchAccessibilityMode = findViewById(R.id.switchAccessibilityMode);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        btnResetDefaults = findViewById(R.id.btnResetDefaults);

        tvStepLengthValue = findViewById(R.id.tvStepLengthValue);
        tvVoiceSpeedValue = findViewById(R.id.tvVoiceSpeedValue);
        tvVoiceVolumeValue = findViewById(R.id.tvVoiceVolumeValue);

        // Step length slider (0.5 - 1.0 meters)
        seekBarStepLength.setMax(50); // 50 steps * 0.01 = 0.5 meters range
        seekBarStepLength.setProgress(20); // Default 0.7 = (0.5 + 0.2*50/100)

        seekBarStepLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double stepLength = 0.5 + (progress * 0.01);
                tvStepLengthValue.setText(String.format("%.2f 米", stepLength));
                tvStepLengthValue.setContentDescription(String.format("平均步长 %.2f 米", stepLength));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Voice speed slider (0.5 - 2.0)
        seekBarVoiceSpeed.setMax(150); // 150 steps * 0.01 = 1.5 range
        seekBarVoiceSpeed.setProgress(30); // Default 0.8 = (0.5 + 0.3)

        seekBarVoiceSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double voiceSpeed = 0.5 + (progress * 0.01);
                tvVoiceSpeedValue.setText(String.format("%.1f 倍", voiceSpeed));
                tvVoiceSpeedValue.setContentDescription(String.format("语音速度 %.1f 倍", voiceSpeed));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Test voice speed when slider stops
                double voiceSpeed = 0.5 + (progress * 0.01);
                voiceService.setSpeechRate((float) voiceSpeed);
                voiceService.speak("语音速度测试");
            }
        });

        // Voice volume slider (0 - 100)
        seekBarVoiceVolume.setMax(100);
        seekBarVoiceVolume.setProgress(100); // Default 100

        seekBarVoiceVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvVoiceVolumeValue.setText(String.format("%d", progress));
                tvVoiceVolumeValue.setContentDescription(String.format("语音音量 %d", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Test volume when slider stops
                voiceService.speak("音量测试");
            }
        });

        // Accessibility mode switch
        switchAccessibilityMode.setOnCheckedChangeListener(null);

        // Save settings button
        btnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        // Reset defaults button
        btnResetDefaults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetDefaults();
            }
        });
    }

    private void loadSettings() {
        // Load step length
        double stepLength = databaseService.getAverageStepLength();
        int stepLengthProgress = (int) ((stepLength - 0.5) * 100);
        seekBarStepLength.setProgress(stepLengthProgress);
        tvStepLengthValue.setText(String.format("%.2f 米", stepLength));

        // Load voice speed
        double voiceSpeed = databaseService.getVoiceSpeed();
        int voiceSpeedProgress = (int) ((voiceSpeed - 0.5) * 100);
        seekBarVoiceSpeed.setProgress(voiceSpeedProgress);
        tvVoiceSpeedValue.setText(String.format("%.1f 倍", voiceSpeed));

        // Load voice volume
        int voiceVolume = databaseService.getVoiceVolume();
        seekBarVoiceVolume.setProgress(voiceVolume);
        tvVoiceVolumeValue.setText(String.format("%d", voiceVolume));

        // Load accessibility mode
        boolean accessibilityMode = databaseService.getAccessibilityMode();
        switchAccessibilityMode.setChecked(accessibilityMode);
        switchAccessibilityMode.setContentDescription(
                accessibilityMode ? "无障碍模式已开启" : "无障碍模式已关闭"
        );
    }

    private void saveSettings() {
        // Get values from UI
        double stepLength = 0.5 + (seekBarStepLength.getProgress() * 0.01);
        double voiceSpeed = 0.5 + (seekBarVoiceSpeed.getProgress() * 0.01);
        int voiceVolume = seekBarVoiceVolume.getProgress();
        boolean accessibilityMode = switchAccessibilityMode.isChecked();

        // Save to database
        databaseService.updateAverageStepLength(stepLength);
        databaseService.updateVoiceSpeed(voiceSpeed);
        databaseService.updateVoiceVolume(voiceVolume);
        databaseService.updateAccessibilityMode(accessibilityMode);

        // Update TTS settings
        voiceService.updateSettings();

        // Show feedback
        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
        voiceService.announceSettingsSaved();
    }

    private void resetDefaults() {
        // Reset to default values
        seekBarStepLength.setProgress(20); // 0.7 meters
        seekBarVoiceSpeed.setProgress(30); // 0.8x speed
        seekBarVoiceVolume.setProgress(100); // 100 volume
        switchAccessibilityMode.setChecked(true); // Accessibility mode on

        // Save defaults
        saveSettings();

        Toast.makeText(this, "已恢复默认设置", Toast.LENGTH_SHORT).show();
        voiceService.speak("已恢复默认设置");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (voiceService != null) {
            voiceService.updateSettings();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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
