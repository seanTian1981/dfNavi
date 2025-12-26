package com.heibeieast.campusnav.models;

public class CampusLocation {
    private long id;
    private String name;
    private double latitude;
    private double longitude;
    private String category;
    private String description;
    private String createdAt;

    public CampusLocation() {
    }

    public CampusLocation(String name, double latitude, double longitude, String category, String description) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.description = description;
        this.createdAt = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
    }

    public CampusLocation(long id, String name, double latitude, double longitude, String category, String description, String createdAt) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.description = description;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
