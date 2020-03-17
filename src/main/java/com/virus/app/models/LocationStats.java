package com.virus.app.models;


public class LocationStats {

    private String state;
    private String country;
    private int latestTotalCases;
    private int diffFromPrevDay;

    public String getState() {
        return state;
    }

    public LocationStats setState(String state) {
        this.state = state;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public LocationStats setCountry(String country) {
        this.country = country;
        return this;
    }

    public int getLatestTotalCases() {
        return latestTotalCases;
    }

    public LocationStats setLatestTotalCases(int latestTotalCases) {
        this.latestTotalCases = latestTotalCases;
        return this;
    }

    public int getDiffFromPrevDay() {
        return diffFromPrevDay;
    }

    public LocationStats setDiffFromPrevDay(int diffFromPrevDay) {
        this.diffFromPrevDay = diffFromPrevDay;
        return this;
    }

    @Override
    public String toString() {
        return "LocationStats{" +
                "state='" + state + '\'' +
                ", country='" + country + '\'' +
                ", latestTotalCases=" + latestTotalCases +
                ", diffFromPrevDay=" + diffFromPrevDay +
                '}';
    }
}
