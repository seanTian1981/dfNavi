package com.heibeieast.campusnav.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class LocationService {
    private static final String TAG = "LocationService";
    private static final long MIN_TIME_BETWEEN_UPDATES = 1000; // 1 second
    private static final float MIN_DISTANCE_FOR_UPDATE = 0; // 0 meters

    private Context context;
    private LocationManager locationManager;
    private DatabaseService databaseService;
    private Location currentLocation;
    private boolean isTracking = false;
    private LocationUpdateListener locationUpdateListener;

    public interface LocationUpdateListener {
        void onLocationUpdated(Location location);
    }

    public LocationService(Context context) {
        this.context = context.getApplicationContext();
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.databaseService = DatabaseService.getInstance(context);
    }

    public boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkAndRequestLocationPermission() {
        if (!checkLocationPermission()) {
            return false;
        }
        return true;
    }

    public Location getCurrentLocation() {
        if (!checkLocationPermission()) {
            return null;
        }

        try {
            Location gpsLocation = null;
            Location networkLocation = null;

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            // Use the most recent and accurate location
            if (gpsLocation != null && networkLocation != null) {
                if (gpsLocation.getTime() > networkLocation.getTime()) {
                    currentLocation = gpsLocation;
                } else {
                    currentLocation = networkLocation;
                }
            } else if (gpsLocation != null) {
                currentLocation = gpsLocation;
            } else if (networkLocation != null) {
                currentLocation = networkLocation;
            }

            return currentLocation;
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception while getting location", e);
            return null;
        }
    }

    @SuppressLint("MissingPermission")
    public boolean startLocationTracking(LocationUpdateListener listener) {
        if (!checkLocationPermission()) {
            return false;
        }

        this.locationUpdateListener = listener;

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return false;
        }

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                if (locationUpdateListener != null) {
                    locationUpdateListener.onLocationUpdated(location);
                }
                Log.d(TAG, "Location updated: " + location.getLatitude() + ", " + location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "Provider status changed: " + provider + ", status: " + status);
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d(TAG, "Provider enabled: " + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(TAG, "Provider disabled: " + provider);
            }
        };

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BETWEEN_UPDATES,
                        MIN_DISTANCE_FOR_UPDATE,
                        locationListener);
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BETWEEN_UPDATES,
                        MIN_DISTANCE_FOR_UPDATE,
                        locationListener);
            }

            isTracking = true;
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception while starting location tracking", e);
            return false;
        }
    }

    public void stopLocationTracking() {
        if (locationManager != null && isTracking) {
            try {
                locationManager.removeUpdates((LocationListener) locationUpdateListener);
            } catch (Exception e) {
                Log.e(TAG, "Error stopping location tracking", e);
            }
            isTracking = false;
            locationUpdateListener = null;
        }
    }

    public boolean isTracking() {
        return isTracking;
    }

    public Location getCurrentPosition() {
        return currentLocation;
    }

    /**
     * Calculate distance between two points using Haversine formula
     * @param lat1 First point latitude
     * @param lon1 First point longitude
     * @param lat2 Second point latitude
     * @param lon2 Second point longitude
     * @return Distance in meters
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth's radius in meters

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Calculate bearing (direction) between two points
     * @param lat1 First point latitude
     * @param lon1 First point longitude
     * @param lat2 Second point latitude
     * @param lon2 Second point longitude
     * @return Bearing in degrees (0-360)
     */
    public static double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double y = Math.sin(deltaLon) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad)
                - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLon);

        double bearing = Math.toDegrees(Math.atan2(y, x));

        // Normalize to 0-360
        return (bearing + 360) % 360;
    }

    /**
     * Convert bearing to Chinese direction name
     * @param bearing Bearing in degrees (0-360)
     * @return Chinese direction name
     */
    public static String getDirectionName(double bearing) {
        if (bearing >= 337.5 || bearing < 22.5) {
            return "北";
        } else if (bearing >= 22.5 && bearing < 67.5) {
            return "东北";
        } else if (bearing >= 67.5 && bearing < 112.5) {
            return "东";
        } else if (bearing >= 112.5 && bearing < 157.5) {
            return "东南";
        } else if (bearing >= 157.5 && bearing < 202.5) {
            return "南";
        } else if (bearing >= 202.5 && bearing < 247.5) {
            return "西南";
        } else if (bearing >= 247.5 && bearing < 292.5) {
            return "西";
        } else if (bearing >= 292.5 && bearing < 337.5) {
            return "西北";
        }
        return "未知";
    }

    /**
     * Calculate number of steps based on distance and user's average step length
     * @param distance Distance in meters
     * @return Number of steps
     */
    public int calculateSteps(double distance) {
        double stepLength = databaseService.getAverageStepLength();
        return (int) Math.ceil(distance / stepLength);
    }

    /**
     * Calculate distance to a specific location from current location
     * @param targetLat Target latitude
     * @param targetLon Target longitude
     * @return Distance in meters, or -1 if current location is not available
     */
    public double calculateDistanceToTarget(double targetLat, double targetLon) {
        if (currentLocation == null) {
            return -1;
        }
        return calculateDistance(
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                targetLat,
                targetLon
        );
    }

    /**
     * Calculate bearing to a specific location from current location
     * @param targetLat Target latitude
     * @param targetLon Target longitude
     * @return Bearing in degrees, or -1 if current location is not available
     */
    public double calculateBearingToTarget(double targetLat, double targetLon) {
        if (currentLocation == null) {
            return -1;
        }
        return calculateBearing(
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                targetLat,
                targetLon
        );
    }
}
