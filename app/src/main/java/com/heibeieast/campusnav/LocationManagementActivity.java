package com.heibeieast.campusnav;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.heibeieast.campusnav.models.CampusLocation;
import com.heibeieast.campusnav.services.DatabaseService;
import com.heibeieast.campusnav.services.LocationService;
import com.heibeieast.campusnav.services.VoiceService;

import java.util.ArrayList;
import java.util.List;

public class LocationManagementActivity extends AppCompatActivity {
    private static final String TAG = "LocationManagementActivity";

    private ListView lvLocations;
    private Button btnAddLocation;
    private Button btnEditLocation;
    private Button btnDeleteLocation;

    private DatabaseService databaseService;
    private LocationService locationService;
    private VoiceService voiceService;

    private List<CampusLocation> locations;
    private ArrayAdapter<String> locationAdapter;
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_management);

        // Initialize services
        databaseService = DatabaseService.getInstance(this);
        locationService = new LocationService(this);
        voiceService = new VoiceService(this);

        // Initialize views
        initializeViews();

        // Load locations
        loadLocations();

        // Initialize TTS
        voiceService.initializeTTS();
    }

    private void initializeViews() {
        lvLocations = findViewById(R.id.lvLocations);
        btnAddLocation = findViewById(R.id.btnAddLocation);
        btnEditLocation = findViewById(R.id.btnEditLocation);
        btnDeleteLocation = findViewById(R.id.btnDeleteLocation);

        // Location adapter
        locationAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_single_choice,
                new ArrayList<String>()
        );

        lvLocations.setAdapter(locationAdapter);
        lvLocations.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Location selection listener
        lvLocations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position;
                updateButtonStates();
            }
        });

        // Add location button
        btnAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddLocationDialog();
            }
        });

        // Edit location button
        btnEditLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPosition >= 0 && selectedPosition < locations.size()) {
                    showEditLocationDialog(locations.get(selectedPosition));
                }
            }
        });

        // Delete location button
        btnDeleteLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPosition >= 0 && selectedPosition < locations.size()) {
                    confirmDeleteLocation(locations.get(selectedPosition));
                }
            }
        });

        // Initial button states
        updateButtonStates();
    }

    private void loadLocations() {
        locations = databaseService.getAllLocations();
        List<String> locationNames = new ArrayList<>();
        for (CampusLocation location : locations) {
            String item = location.getName() + "\n(" + location.getCategory() + ")";
            locationNames.add(item);
        }

        locationAdapter.clear();
        locationAdapter.addAll(locationNames);
        locationAdapter.notifyDataSetChanged();

        selectedPosition = -1;
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedPosition >= 0 && selectedPosition < locations.size();
        btnEditLocation.setEnabled(hasSelection);
        btnDeleteLocation.setEnabled(hasSelection);
    }

    @SuppressLint("MissingPermission")
    private void showAddLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加新位置");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_location, null);

        final EditText etName = dialogView.findViewById(R.id.etLocationName);
        final Spinner spCategory = dialogView.findViewById(R.id.spinnerCategory);
        final EditText etDescription = dialogView.findViewById(R.id.etDescription);
        final Button btnUseCurrentLocation = dialogView.findViewById(R.id.btnUseCurrentLocation);

        final TextView tvLatitude = dialogView.findViewById(R.id.tvLatitude);
        final TextView tvLongitude = dialogView.findViewById(R.id.tvLongitude);

        // Setup category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"building", "library", "canteen", "gym", "gate", "other"}
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        // Use current location button
        btnUseCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!locationService.checkLocationPermission()) {
                    Toast.makeText(LocationManagementActivity.this, "需要位置权限", Toast.LENGTH_SHORT).show();
                    voiceService.speak("需要位置权限");
                    return;
                }

                Location currentLocation = locationService.getCurrentLocation();
                if (currentLocation != null) {
                    tvLatitude.setText(String.format("%.6f", currentLocation.getLatitude()));
                    tvLongitude.setText(String.format("%.6f", currentLocation.getLongitude()));
                    Toast.makeText(LocationManagementActivity.this, "已获取当前位置", Toast.LENGTH_SHORT).show();
                    voiceService.speak("已获取当前位置");
                } else {
                    Toast.makeText(LocationManagementActivity.this, "无法获取当前位置", Toast.LENGTH_SHORT).show();
                    voiceService.speak("无法获取当前位置");
                }
            }
        });

        builder.setView(dialogView);
        builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = etName.getText().toString().trim();
                String category = (String) spCategory.getSelectedItem();
                String description = etDescription.getText().toString().trim();
                String latStr = tvLatitude.getText().toString().trim();
                String lonStr = tvLongitude.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(LocationManagementActivity.this, "请输入位置名称", Toast.LENGTH_SHORT).show();
                    voiceService.speak("请输入位置名称");
                    return;
                }

                if (latStr.isEmpty() || lonStr.isEmpty()) {
                    Toast.makeText(LocationManagementActivity.this, "请设置位置坐标", Toast.LENGTH_SHORT).show();
                    voiceService.speak("请设置位置坐标");
                    return;
                }

                try {
                    double latitude = Double.parseDouble(latStr);
                    double longitude = Double.parseDouble(lonStr);

                    CampusLocation location = new CampusLocation(name, latitude, longitude, category, description);
                    databaseService.addLocation(location);

                    loadLocations();
                    Toast.makeText(LocationManagementActivity.this, "位置已添加", Toast.LENGTH_SHORT).show();
                    voiceService.announceLocationAdded(name);

                } catch (NumberFormatException e) {
                    Toast.makeText(LocationManagementActivity.this, "坐标格式错误", Toast.LENGTH_SHORT).show();
                    voiceService.speak("坐标格式错误");
                }
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showEditLocationDialog(final CampusLocation location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("编辑位置");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_location, null);

        final EditText etName = dialogView.findViewById(R.id.etLocationName);
        final Spinner spCategory = dialogView.findViewById(R.id.spinnerCategory);
        final EditText etDescription = dialogView.findViewById(R.id.etDescription);
        final TextView tvLatitude = dialogView.findViewById(R.id.tvLatitude);
        final TextView tvLongitude = dialogView.findViewById(R.id.tvLongitude);
        final Button btnUseCurrentLocation = dialogView.findViewById(R.id.btnUseCurrentLocation);

        // Setup category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"building", "library", "canteen", "gym", "gate", "other"}
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        // Populate with existing data
        etName.setText(location.getName());
        etDescription.setText(location.getDescription());
        tvLatitude.setText(String.valueOf(location.getLatitude()));
        tvLongitude.setText(String.valueOf(location.getLongitude()));

        // Select category
        int categoryPosition = categoryAdapter.getPosition(location.getCategory());
        if (categoryPosition >= 0) {
            spCategory.setSelection(categoryPosition);
        }

        // Use current location button
        btnUseCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (!locationService.checkLocationPermission()) {
                    Toast.makeText(LocationManagementActivity.this, "需要位置权限", Toast.LENGTH_SHORT).show();
                    voiceService.speak("需要位置权限");
                    return;
                }

                Location currentLocation = locationService.getCurrentLocation();
                if (currentLocation != null) {
                    tvLatitude.setText(String.format("%.6f", currentLocation.getLatitude()));
                    tvLongitude.setText(String.format("%.6f", currentLocation.getLongitude()));
                    Toast.makeText(LocationManagementActivity.this, "已获取当前位置", Toast.LENGTH_SHORT).show();
                    voiceService.speak("已获取当前位置");
                } else {
                    Toast.makeText(LocationManagementActivity.this, "无法获取当前位置", Toast.LENGTH_SHORT).show();
                    voiceService.speak("无法获取当前位置");
                }
            }
        });

        builder.setView(dialogView);
        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = etName.getText().toString().trim();
                String category = (String) spCategory.getSelectedItem();
                String description = etDescription.getText().toString().trim();
                String latStr = tvLatitude.getText().toString().trim();
                String lonStr = tvLongitude.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(LocationManagementActivity.this, "请输入位置名称", Toast.LENGTH_SHORT).show();
                    voiceService.speak("请输入位置名称");
                    return;
                }

                try {
                    double latitude = Double.parseDouble(latStr);
                    double longitude = Double.parseDouble(lonStr);

                    location.setName(name);
                    location.setCategory(category);
                    location.setDescription(description);
                    location.setLatitude(latitude);
                    location.setLongitude(longitude);

                    databaseService.updateLocation(location);

                    loadLocations();
                    Toast.makeText(LocationManagementActivity.this, "位置已更新", Toast.LENGTH_SHORT).show();
                    voiceService.announceLocationEdited(name);

                } catch (NumberFormatException e) {
                    Toast.makeText(LocationManagementActivity.this, "坐标格式错误", Toast.LENGTH_SHORT).show();
                    voiceService.speak("坐标格式错误");
                }
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void confirmDeleteLocation(final CampusLocation location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除位置");
        builder.setMessage("确定要删除位置 \"" + location.getName() + "\" 吗？");

        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                databaseService.deleteLocation(location.getId());

                loadLocations();
                Toast.makeText(LocationManagementActivity.this, "位置已删除", Toast.LENGTH_SHORT).show();
                voiceService.announceLocationDeleted(location.getName());
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
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
