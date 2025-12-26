package com.heibeieast.campusnav.models;

public class NavigationInstruction {
    private int step;
    private String instruction;
    private double distance;
    private int steps;
    private int bearing;

    public NavigationInstruction() {
    }

    public NavigationInstruction(int step, String instruction) {
        this.step = step;
        this.instruction = instruction;
    }

    public NavigationInstruction(int step, String instruction, double distance, int steps, int bearing) {
        this.step = step;
        this.instruction = instruction;
        this.distance = distance;
        this.steps = steps;
        this.bearing = bearing;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getBearing() {
        return bearing;
    }

    public void setBearing(int bearing) {
        this.bearing = bearing;
    }
}
