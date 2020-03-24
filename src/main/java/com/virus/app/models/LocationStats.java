package com.virus.app.models;


public class LocationStats {

    private String state;
    private String country;
    private int confirmedCases;
    private int newConfirmedCases;
    private int deathCases;
    private int newDeathCases;
    private int recoveredCases;
    private int newRecoveredCases;

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

    public int getConfirmedCases() {
        return confirmedCases;
    }

    public LocationStats setConfirmedCases(int confirmedCases) {
        this.confirmedCases = confirmedCases;
        return this;
    }

    public int getNewConfirmedCases() {
        return newConfirmedCases;
    }

    public LocationStats setNewConfirmedCases(int newConfirmedCases) {
        this.newConfirmedCases = newConfirmedCases;
        return this;
    }

    public int getDeathCases() {
        return deathCases;
    }

    public LocationStats setDeathCases(int deathCases) {
        this.deathCases = deathCases;
        return this;
    }

    public int getNewDeathCases() {
        return newDeathCases;
    }

    public LocationStats setNewDeathCases(int newDeathCases) {
        this.newDeathCases = newDeathCases;
        return this;
    }

    public int getRecoveredCases() {
        return recoveredCases;
    }

    public LocationStats setRecoveredCases(int recoveredCases) {
        this.recoveredCases = recoveredCases;
        return this;
    }

    public int getNewRecoveredCases() {
        return newRecoveredCases;
    }

    public LocationStats setNewRecoveredCases(int newRecoveredCases) {
        this.newRecoveredCases = newRecoveredCases;
        return this;
    }

    @Override
    public String toString() {
        return "LocationStats{" +
                "state='" + state + '\'' +
                ", country='" + country + '\'' +
                ", confirmedCases=" + confirmedCases +
                ", newConfirmedCases=" + newConfirmedCases +
                ", deathCases=" + deathCases +
                ", newDeathCases=" + newDeathCases +
                ", recoveredCases=" + recoveredCases +
                ", newRecoveredCases=" + newRecoveredCases +
                '}';
    }
}
