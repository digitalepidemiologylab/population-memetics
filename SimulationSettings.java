package com.salathe.populationmemetics;

public class SimulationSettings {

    private static SimulationSettings ourInstance = new SimulationSettings();


    private int numberOfPeople = 2000;
    private int k = 8;
    private double rewiringProbability = 0.01;

    // fertility
    private double transmissionRate00   = 0.;
    private double transmissionRate01   = 0.;
    private double transmissionRate11   = 1.0;

    // viability
    private double viability00          = 1.0;
    private double viability01          = 0.;
    private double viability11          = 1.0;


    public static SimulationSettings getInstance() {
        return ourInstance;
    }

    private SimulationSettings() {
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public double getRewiringProbability() {
        return rewiringProbability;
    }

    public void setRewiringProbability(double rewiringProbability) {
        this.rewiringProbability = rewiringProbability;
    }

    public double getTransmissionRate00() {
        return transmissionRate00;
    }

    public void setTransmissionRate00(double transmissionRate00) {
        this.transmissionRate00 = transmissionRate00;
    }

    public double getTransmissionRate01() {
        return transmissionRate01;
    }

    public void setTransmissionRate01(double transmissionRate01) {
        this.transmissionRate01 = transmissionRate01;
    }

    public double getTransmissionRate11() {
        return transmissionRate11;
    }

    public void setTransmissionRate11(double transmissionRate11) {
        this.transmissionRate11 = transmissionRate11;
    }

    public double getViability00() {
        return viability00;
    }

    public void setViability00(double viability00) {
        this.viability00 = viability00;
    }

    public double getViability01() {
        return viability01;
    }

    public void setViability01(double viability01) {
        this.viability01 = viability01;
    }

    public double getViability11() {
        return viability11;
    }

    public void setViability11(double viability11) {
        this.viability11 = viability11;
    }
}
