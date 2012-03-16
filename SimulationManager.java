package com.salathe.populationmemetics;

public class SimulationManager {

    public static void main(String[] args) {
        SimulationManager sm = new SimulationManager();
        sm.run();
    }

    private void run() {
        int repeatsPerP = 10;
        double[] ps = {0.01, 0.011, 0.013, 0.017, 0.023, 0.031, 0.04, 0.1, 0.11, 0.13, 0.17, 0.23, 0.31, 0.4};
        for (int i = 0; i < ps.length; i++) {
            double sumTimesteps = 0;
            SimulationSettings.getInstance().setRewiringProbability(ps[i]);
            for (int ii = 0; ii < repeatsPerP; ii++) {
                Simulation simulation;
                do {
                    simulation = new Simulation();
                    simulation.run();
                }
                while (!simulation.hasSuccessfullyFinished());
                sumTimesteps += simulation.currentTimestep;
            }
            System.out.println(ps[i] + "\t" + sumTimesteps / repeatsPerP);
        }

    }
}
