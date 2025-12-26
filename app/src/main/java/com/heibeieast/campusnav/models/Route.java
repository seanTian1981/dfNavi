package com.heibeieast.campusnav.models;

public class Route {
    private long id;
    private long fromLocationId;
    private long toLocationId;
    private double distanceMeters;
    private String routeDescription;
    private int estimatedSteps;

    public Route() {
    }

    public Route(long fromLocationId, long toLocationId, double distanceMeters, String routeDescription, int estimatedSteps) {
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.distanceMeters = distanceMeters;
        this.routeDescription = routeDescription;
        this.estimatedSteps = estimatedSteps;
    }

    public Route(long id, long fromLocationId, long toLocationId, double distanceMeters, String routeDescription, int estimatedSteps) {
        this.id = id;
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.distanceMeters = distanceMeters;
        this.routeDescription = routeDescription;
        this.estimatedSteps = estimatedSteps;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getFromLocationId() {
        return fromLocationId;
    }

    public void setFromLocationId(long fromLocationId) {
        this.fromLocationId = fromLocationId;
    }

    public long getToLocationId() {
        return toLocationId;
    }

    public void setToLocationId(long toLocationId) {
        this.toLocationId = toLocationId;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public String getRouteDescription() {
        return routeDescription;
    }

    public void setRouteDescription(String routeDescription) {
        this.routeDescription = routeDescription;
    }

    public int getEstimatedSteps() {
        return estimatedSteps;
    }

    public void setEstimatedSteps(int estimatedSteps) {
        this.estimatedSteps = estimatedSteps;
    }
}
