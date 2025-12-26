package com.heibeieast.campusnav.services;

import android.util.Log;

import com.heibeieast.campusnav.models.CampusLocation;
import com.heibeieast.campusnav.models.NavigationInstruction;

import java.util.ArrayList;
import java.util.List;

public class PathPlanningService {
    private static final String TAG = "PathPlanningService";
    private static final double ARRIVAL_THRESHOLD = 5.0; // 5 meters
    private static final double INSTRUCTION_UPDATE_THRESHOLD = 10.0; // 10 meters

    private DatabaseService databaseService;
    private LocationService locationService;
    private VoiceService voiceService;

    private CampusLocation startLocation;
    private CampusLocation destinationLocation;
    private List<NavigationInstruction> instructions;
    private int currentInstructionIndex = 0;
    private boolean isNavigating = false;

    public PathPlanningService(DatabaseService databaseService, LocationService locationService, VoiceService voiceService) {
        this.databaseService = databaseService;
        this.locationService = locationService;
        this.voiceService = voiceService;
        this.instructions = new ArrayList<>();
    }

    /**
     * Plan navigation path from start location to end location
     * @param fromLocationName Start location name
     * @param toLocationName Destination location name
     * @return true if path planned successfully, false otherwise
     */
    public boolean planPath(String fromLocationName, String toLocationName) {
        // Get locations from database
        startLocation = getLocationByName(fromLocationName);
        destinationLocation = getLocationByName(toLocationName);

        if (startLocation == null) {
            Log.e(TAG, "Start location not found: " + fromLocationName);
            return false;
        }

        if (destinationLocation == null) {
            Log.e(TAG, "Destination location not found: " + toLocationName);
            return false;
        }

        // Generate navigation instructions
        instructions = generateNavigationInstructions(startLocation, destinationLocation);
        currentInstructionIndex = 0;
        isNavigating = true;

        Log.d(TAG, "Path planned from " + fromLocationName + " to " + toLocationName +
                " with " + instructions.size() + " instructions");

        return true;
    }

    /**
     * Get navigation instructions as a list
     * @param fromLocationName Start location name
     * @param toLocationName Destination location name
     * @return List of navigation instructions
     */
    public List<NavigationInstruction> getNavigationInstructions(String fromLocationName, String toLocationName) {
        CampusLocation fromLocation = getLocationByName(fromLocationName);
        CampusLocation toLocation = getLocationByName(toLocationName);

        if (fromLocation == null || toLocation == null) {
            Log.e(TAG, "One or both locations not found");
            return new ArrayList<>();
        }

        return generateNavigationInstructions(fromLocation, toLocation);
    }

    /**
     * Generate step-by-step navigation instructions
     */
    private List<NavigationInstruction> generateNavigationInstructions(CampusLocation from, CampusLocation to) {
        List<NavigationInstruction> instructions = new ArrayList<>();

        // Calculate total distance and bearing
        double distance = LocationService.calculateDistance(
                from.getLatitude(), from.getLongitude(),
                to.getLatitude(), to.getLongitude()
        );

        double bearing = LocationService.calculateBearing(
                from.getLatitude(), from.getLongitude(),
                to.getLatitude(), to.getLongitude()
        );

        int steps = locationService.calculateSteps(distance);
        String direction = LocationService.getDirectionName(bearing);

        // For simplicity, we create a single instruction for direct navigation
        // In a more complex system, this would be broken down into multiple waypoints
        String instructionText = String.format("向%s方向走%.0f米，约%d步", direction, distance, steps);

        NavigationInstruction instruction = new NavigationInstruction(
                1,
                instructionText,
                distance,
                steps,
                (int) bearing
        );

        instructions.add(instruction);

        return instructions;
    }

    /**
     * Update navigation progress based on current location
     * @param currentLat Current latitude
     * @param currentLon Current longitude
     * @param destinationName Destination name
     * @return Current navigation instruction, or null if navigation is complete
     */
    public NavigationInstruction updateNavigationProgress(double currentLat, double currentLon, String destinationName) {
        if (!isNavigating || destinationLocation == null) {
            return null;
        }

        // Calculate distance to destination
        double remainingDistance = LocationService.calculateDistance(
                currentLat, currentLon,
                destinationLocation.getLatitude(),
                destinationLocation.getLongitude()
        );

        // Check if arrived
        if (remainingDistance <= ARRIVAL_THRESHOLD) {
            isNavigating = false;
            voiceService.announceArrival(destinationName);
            return null;
        }

        // Calculate bearing to destination
        double bearing = LocationService.calculateBearing(
                currentLat, currentLon,
                destinationLocation.getLatitude(),
                destinationLocation.getLongitude()
        );

        // Update current instruction with new distance and bearing
        if (currentInstructionIndex < instructions.size()) {
            NavigationInstruction instruction = instructions.get(currentInstructionIndex);
            instruction.setDistance(remainingDistance);
            instruction.setSteps(locationService.calculateSteps(remainingDistance));
            instruction.setBearing((int) bearing);

            // Update instruction text
            String direction = LocationService.getDirectionName(bearing);
            String instructionText = String.format("向%s方向走%.0f米，约%d步",
                    direction, remainingDistance, instruction.getSteps());
            instruction.setInstruction(instructionText);

            return instruction;
        }

        return null;
    }

    /**
     * Get current instruction
     */
    public NavigationInstruction getCurrentInstruction() {
        if (currentInstructionIndex < instructions.size()) {
            return instructions.get(currentInstructionIndex);
        }
        return null;
    }

    /**
     * Advance to next instruction
     */
    public boolean nextInstruction() {
        if (currentInstructionIndex < instructions.size() - 1) {
            currentInstructionIndex++;
            return true;
        }
        return false;
    }

    /**
     * Get total number of instructions
     */
    public int getTotalInstructions() {
        return instructions.size();
    }

    /**
     * Get current instruction index
     */
    public int getCurrentInstructionIndex() {
        return currentInstructionIndex;
    }

    /**
     * Check if navigation is in progress
     */
    public boolean isNavigating() {
        return isNavigating;
    }

    /**
     * Stop navigation
     */
    public void stopNavigation() {
        isNavigating = false;
        instructions.clear();
        currentInstructionIndex = 0;
    }

    /**
     * Calculate estimated travel time
     * @param distance Distance in meters
     * @return Estimated time in seconds
     */
    public int calculateEstimatedTime(double distance) {
        // Assume average walking speed of 1.4 m/s
        return (int) (distance / 1.4);
    }

    /**
     * Get distance between two location names
     */
    public double getDistanceBetweenLocations(String fromName, String toName) {
        CampusLocation fromLocation = getLocationByName(fromName);
        CampusLocation toLocation = getLocationByName(toName);

        if (fromLocation == null || toLocation == null) {
            return -1;
        }

        return LocationService.calculateDistance(
                fromLocation.getLatitude(), fromLocation.getLongitude(),
                toLocation.getLatitude(), toLocation.getLongitude()
        );
    }

    /**
     * Find location by name
     */
    private CampusLocation getLocationByName(String name) {
        long locationId = databaseService.getLocationIdByName(name);
        if (locationId == -1) {
            return null;
        }
        return databaseService.getLocationById(locationId);
    }

    /**
     * Get nearest location to current position
     */
    public CampusLocation findNearestLocation(double currentLat, double currentLon) {
        List<CampusLocation> allLocations = databaseService.getAllLocations();

        if (allLocations.isEmpty()) {
            return null;
        }

        CampusLocation nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (CampusLocation location : allLocations) {
            double distance = LocationService.calculateDistance(
                    currentLat, currentLon,
                    location.getLatitude(),
                    location.getLongitude()
            );

            if (distance < minDistance) {
                minDistance = distance;
                nearest = location;
            }
        }

        return nearest;
    }

    /**
     * Get remaining distance to destination
     */
    public double getRemainingDistance(double currentLat, double currentLon) {
        if (destinationLocation == null) {
            return -1;
        }

        return LocationService.calculateDistance(
                currentLat, currentLon,
                destinationLocation.getLatitude(),
                destinationLocation.getLongitude()
        );
    }

    /**
     * Get bearing to destination from current position
     */
    public double getBearingToDestination(double currentLat, double currentLon) {
        if (destinationLocation == null) {
            return -1;
        }

        return LocationService.calculateBearing(
                currentLat, currentLon,
                destinationLocation.getLatitude(),
                destinationLocation.getLongitude()
        );
    }
}
