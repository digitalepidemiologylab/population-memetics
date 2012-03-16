package com.salathe.populationmemetics;

public class SimulationManager {

    public static void main(String[] args) {
        SimulationManager sm = new SimulationManager();
        sm.run();
    }

    private void run() {
        int repeatsPerP = 10;
        double[] ps = {0.42,0.44, 0.46, 0.48};
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
