package com.salathe.populationmemetics;

import java.util.ArrayList;

public class SimulationManager {

    public static void main(String[] args) {
        SimulationManager sm = new SimulationManager();
        sm.runStandardModelComplexContagion();
    }

    private void runStandardModelComplexContagion() {
        SimulationSettings.getInstance().setNumberOfPeople(200*200);
        SimulationSettings.getInstance().setGraphType(Simulation.GRAPH_TYPE_WATTS_STROGATZ_2D);
        SimulationSettings.getInstance().setRewiringType(Simulation.EDGE_REWIRING);
        int repeatsPerP = 10;
        ArrayList<Double> ps_list = new ArrayList<Double>();
        ps_list.add(0.0001);
        double nextValue;
        do {
            nextValue = ps_list.get(ps_list.size()-1);
            nextValue *= 1.1;
            ps_list.add(nextValue);
        }
        while (nextValue < 0.5);
        double[] ps = new double[ps_list.size()];
        for (int i = 0; i < ps_list.size(); i++) {
            ps[i] = ps_list.get(i);
        }
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
