package com.salathe.populationmemetics;


import java.util.ArrayList;

public class SimulationManager {

    public static void main(String[] args) {
        SimulationManager sm = new SimulationManager();
//        sm.runStandardModelComplexContagion();
        sm.runBidrectionalModelComplexContagion();
    }

    private void runBidrectionalModelComplexContagion() {
        // the network
        SimulationSettings.getInstance().setNumberOfPeople(50*50);
        SimulationSettings.getInstance().setGraphType(Simulation.GRAPH_TYPE_WATTS_STROGATZ_1D);
        SimulationSettings.getInstance().setRewiringType(Simulation.EDGE_REWIRING);
        SimulationSettings.getInstance().setK(8);
        SimulationSettings.getInstance().setRewiringProbability(0.01);
        // the settings
        SimulationSettings.getInstance().setTransmissionRate00(0.1);
        SimulationSettings.getInstance().setTransmissionRate01(0.0);
        SimulationSettings.getInstance().setTransmissionRate11(0.1);
        SimulationSettings.getInstance().setViability00(1.0);
        SimulationSettings.getInstance().setViability00(0.0);
        SimulationSettings.getInstance().setViability00(1.0);
        SimulationSettings.getInstance().setMemotype_exposing_to_exposed(new String[][]{{"00"},{"00"},{"01"},{"00"},{"01"},{"11"},{"01"},{"11"},{"11"}});

        Simulation simulation = new Simulation();
        simulation.run();
    }

    private void runStandardModelComplexContagion() {
        // MAKE SURE YOU CALL setRandomNeighboursTo11 in Simulation!
        // ALSO: CALL checkWhetherToStop() in Simulation!
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
