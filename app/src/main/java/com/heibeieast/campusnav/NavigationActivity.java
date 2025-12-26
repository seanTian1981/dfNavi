package com.heibeieast.campusnav;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.heibeieast.campusnav.models.CampusLocation;
import com.heibeieast.campusnav.models.NavigationInstruction;
import com.heibeieast.campusnav.services.DatabaseService;
import com.heibeieast.campusnav.services.LocationService;
import com.heibeieast.campusnav.services.PathPlanningService;
import com.heibeieast.campusnav.services.VoiceService;

import java.util.ArrayList;
import java.util.List;

public class NavigationActivity extends AppCompatActivity {
    private static final String TAG = "NavigationActivity";

    private Spinner spinnerFromLocation;
    private Spinner spinnerToLocation;
    private Button btnStartNavigation;
    private Button btnStopNavigation;
    private TextView tvCurrentLocation;
    private TextView tvDistance;
    private TextView tvRemainingSteps;
    private TextView tvInstruction;
    private View navigationInfoSection;

    private DatabaseService databaseService;
    private LocationService locationService;
    private VoiceService voiceService;
    private PathPlanningService pathPlanningService;

    private List<CampusLocation> locations;
    private ArrayAdapter<String> locationAdapter;

    private String selectedFromLocation;
    private String selectedToLocation;
    private boolean isNavigating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Initialize services
        databaseService = DatabaseService.getInstance(this);
        locationService = new LocationService(this);
        voiceService = new VoiceService(this);
        pathPlanningService = new PathPlanningService(databaseService, locationService, voiceService);

        // Initialize views
        initializeViews();

        // Load locations
        loadLocations();

        // Initialize TTS
        voiceService.initializeTTS();
    }

    private void initializeViews() {
        spinnerFromLocation = findViewById(R.id.spinnerFromLocation);
        spinnerToLocation = findViewById(R.id.spinnerToLocation);
        btnStartNavigation = findViewById(R.id.btnStartNavigation);
        btnStopNavigation = findViewById(R.id.btnStopNavigation);
        tvCurrentLocation = findViewById(R.id.tvCurrentLocation);
        tvDistance = findViewById(R.id.tvDistance);
        tvRemainingSteps = findViewById(R.id.tvRemainingSteps);
        tvInstruction = findViewById(R.id.tvInstruction);
        navigationInfoSection = findViewById(R.id.navigationInfoSection);

        // Set initial button state
        btnStopNavigation.setEnabled(false);
        btnStopNavigation.setVisibility(View.GONE);

        // From location spinner
        spinnerFromLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFromLocation = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedFromLocation = null;
            }
        });

        // To location spinner
        spinnerToLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedToLocation = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedToLocation = null;
            }
        });

        // Start navigation button
        btnStartNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedFromLocation == null || selectedToLocation == null) {
                    Toast.makeText(NavigationActivity.this, "请选择起点和终点", Toast.LENGTH_SHORT).show();
                    voiceService.speak("请选择起点和终点");
                    return;
                }

                if (selectedFromLocation.equals(selectedToLocation)) {
                    Toast.makeText(NavigationActivity.this, "起点和终点不能相同", Toast.LENGTH_SHORT).show();
                    voiceService.speak("起点和终点不能相同");
                    return;
                }

                startNavigation();
            }
        });

        // Stop navigation button
        btnStopNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopNavigation();
            }
        });
    }

    private void loadLocations() {
        locations = databaseService.getAllLocations();
        List<String> locationNames = new ArrayList<>();
        for (CampusLocation location : locations) {
            locationNames.add(location.getName());
        }

        locationAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                locationNames
        );
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFromLocation.setAdapter(locationAdapter);
        spinnerToLocation.setAdapter(locationAdapter);

        // Set default selections
        if (!locationNames.isEmpty()) {
            spinnerFromLocation.setSelection(0);
            if (locationNames.size() > 1) {
                spinnerToLocation.setSelection(1);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void startNavigation() {
        // Plan path
        boolean pathPlanned = pathPlanningService.planPath(selectedFromLocation, selectedToLocation);

        if (!pathPlanned) {
            Toast.makeText(this, "无法规划路径", Toast.LENGTH_SHORT).show();
            voiceService.speak("无法规划路径");
            return;
        }

        // Start location tracking
        boolean trackingStarted = locationService.startLocationTracking(new LocationService.LocationUpdateListener() {
            @Override
            public void onLocationUpdated(Location location) {
                updateNavigationUI(location);
            }
        });

        if (!trackingStarted) {
            Toast.makeText(this, "无法启动位置跟踪", Toast.LENGTH_SHORT).show();
            voiceService.speak("无法启动位置跟踪");
            return;
        }

        // Announce navigation start
        voiceService.announceNavigation(selectedFromLocation, selectedToLocation);

        // Update UI state
        isNavigating = true;
        btnStartNavigation.setEnabled(false);
        btnStartNavigation.setVisibility(View.GONE);
        btnStopNavigation.setEnabled(true);
        btnStopNavigation.setVisibility(View.VISIBLE);
        navigationInfoSection.setVisibility(View.VISIBLE);

        // Initial update
        Location currentLocation = locationService.getCurrentLocation();
        if (currentLocation != null) {
            updateNavigationUI(currentLocation);
        }
    }

    private void updateNavigationUI(Location location) {
        if (!isNavigating) {
            return;
        }

        // Update current location display
        String locationText = String.format("当前位置: %.6f, %.6f",
                location.getLatitude(), location.getLongitude());
        tvCurrentLocation.setText(locationText);
        tvCurrentLocation.setContentDescription("当前位置：" +
                String.format("北纬%.4f，东经%.4f", location.getLatitude(), location.getLongitude()));

        // Get destination location
        long destId = databaseService.getLocationIdByName(selectedToLocation);
        CampusLocation destination = databaseService.getLocationById(destId);

        if (destination == null) {
            return;
        }

        // Update navigation progress
        NavigationInstruction instruction = pathPlanningService.updateNavigationProgress(
                location.getLatitude(),
                location.getLongitude(),
                selectedToLocation
        );

        if (instruction == null) {
            // Arrived at destination
            updateUIOnArrival();
            return;
        }

        // Update distance display
        tvDistance.setText(String.format("距离: %.0f米", instruction.getDistance()));
        tvDistance.setContentDescription(String.format("距离目标%.0f米", instruction.getDistance()));

        // Update steps display
        tvRemainingSteps.setText(String.format("剩余步数: %d步", instruction.getSteps()));
        tvRemainingSteps.setContentDescription(String.format("还需走%d步", instruction.getSteps()));

        // Update instruction display
        tvInstruction.setText(instruction.getInstruction());
        tvInstruction.setContentDescription(instruction.getInstruction());

        // Voice announcement (only when significant change)
        announceNavigationUpdate(instruction);
    }

    private void announceNavigationUpdate(NavigationInstruction instruction) {
        // Only announce direction changes or major distance updates
        String direction = LocationService.getDirectionName(instruction.getBearing());
        String message = String.format("请向%s方向走%.0f米", direction, instruction.getDistance());
        voiceService.announceInstruction(message);
    }

    private void updateUIOnArrival() {
        isNavigating = false;

        // Stop location tracking
        locationService.stopLocationTracking();

        // Stop path planning
        pathPlanningService.stopNavigation();

        // Announce cancellation
        voiceService.announceArrival(selectedToLocation);

        // Update UI to show arrival state
        navigationInfoSection.setVisibility(View.VISIBLE);
        btnStartNavigation.setEnabled(true);
        btnStartNavigation.setVisibility(View.VISIBLE);
        btnStopNavigation.setEnabled(false);
        btnStopNavigation.setVisibility(View.GONE);

        tvDistance.setText("已到达目标");
        tvDistance.setContentDescription("已到达" + selectedToLocation);
        tvRemainingSteps.setText("剩余步数: 0步");
        tvInstruction.setText("导航结束");

        Toast.makeText(this, "已到达" + selectedToLocation, Toast.LENGTH_SHORT).show();
    }

    private void stopNavigation() {
        isNavigating = false;

        // Stop location tracking
        locationService.stopLocationTracking();

        // Stop path planning
        pathPlanningService.stopNavigation();

        // Announce cancellation
        voiceService.announceNavigationCancelled();

        // Update UI state
        btnStartNavigation.setEnabled(true);
        btnStartNavigation.setVisibility(View.VISIBLE);
        btnStopNavigation.setEnabled(false);
        btnStopNavigation.setVisibility(View.GONE);
        navigationInfoSection.setVisibility(View.GONE);

        // Clear navigation data
        tvCurrentLocation.setText("当前位置: --");
        tvCurrentLocation.setContentDescription("当前位置未知");
        tvDistance.setText("距离: --");
        tvRemainingSteps.setText("剩余步数: --");
        tvInstruction.setText("请选择起点和终点开始导航");
        tvInstruction.setContentDescription("请选择起点和终点开始导航");
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
        if (locationService != null && isNavigating) {
            locationService.stopLocationTracking();
        }
        if (pathPlanningService != null) {
            pathPlanningService.stopNavigation();
        }
        if (voiceService != null) {
            voiceService.shutdown();
        }
    }
}
