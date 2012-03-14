package com.salathe.populationmemetics;

public class SimulationSettings {

    private static SimulationSettings ourInstance = new SimulationSettings();


    private int numberOfPeople = 1000;
    private int k = 10;
    private double rewiringProbability = 0.02;



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

}
