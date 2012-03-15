package com.salathe.populationmemetics;


import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;

import java.util.Random;
import java.util.Set;

public class Simulation {

    Graph<Person, Connection> g;
    int currentTimestep = 0;
    Person[] people;
    Random random;
    boolean socialContagion = false;

    public static void main(String[] args) {
        Simulation simulation = new Simulation();
        simulation.run();
    }

    private void run() {
        this.random = new Random();
        this.initGraph();
        this.setRandomPersonTo11();
        this.runTimesteps();
    }

    private void setRandomPersonTo11() {
        this.people[this.random.nextInt(SimulationSettings.getInstance().getNumberOfPeople())].setMemotype("11");
    }

    private void runTimesteps() {
        while(true) {
            if (this.currentTimestep==0) this.socialContagion = true;
            if (this.socialContagion) {
                this.socialContagion();
                this.gatherData();
                this.currentTimestep++;
            }
            else break;
        }
    }

    private void gatherData() {
        System.out.println(this.currentTimestep + "\t" + this.getFrequencyOfMemotype("00") + "\t" + this.getFrequencyOfMemotype("01") + "\t" + this.getFrequencyOfMemotype("11"));
    }

    private double getFrequencyOfMemotype(String memotype) {
        double counter  = 0.;
        for (Person person:this.g.getVertices()) {
            if (person.getMemotype().equals(memotype)) counter++;
        }
        return counter / SimulationSettings.getInstance().getNumberOfPeople();
    }

    private void socialContagion() {
        double transmissionRate00 = SimulationSettings.getInstance().getTransmissionRate00();
        double transmissionRate01 = SimulationSettings.getInstance().getTransmissionRate01();
        double transmissionRate11 = SimulationSettings.getInstance().getTransmissionRate11();
        for (Person person:this.g.getVertices()) {
            // person is the exposed
            for (Person neighbour:this.g.getNeighbors(person)) {
                // neighbour is the exposing
                double r = random.nextDouble();
                boolean transmissionOccurs = false;
                if (neighbour.getMemotype().equals("00") && r < transmissionRate00) transmissionOccurs = true;
                else if (neighbour.getMemotype().equals("01") && r < transmissionRate01) transmissionOccurs = true;
                else if (neighbour.getMemotype().equals("11") && r < transmissionRate11) transmissionOccurs = true;
                if (transmissionOccurs) person.addTempMemotype(this.getOffspringMemotype(person.getMemotype(), neighbour.getMemotype()));
            }
        }
        // assign new opinions according to temp value and reset
        for (Person person:this.g.getVertices()) {
            if (person.getTempMemotypes().size() > 0) {
                person.setMemotype(person.getTempMemotypes().get(this.random.nextInt(person.getTempMemotypes().size())));
                person.resetTempMemotypes();
            }
        }
    }

    private String getOffspringMemotype(String memotype_exposed, String memotype_exposing) {
        if (memotype_exposing.equals("00")) {
            return memotype_exposed;
        }
        else if (memotype_exposing.equals("01")) {
            return memotype_exposed;
        }
        else {
            if (memotype_exposed.equals("00")) return "01";
            else if (memotype_exposed.equals("01")) return "11";
            else return "11";
        }
    }

    private void initGraph() {
        Set components;
        int numberOfPeople = SimulationSettings.getInstance().getNumberOfPeople();
        int k = SimulationSettings.getInstance().getK();
        this.people = new Person[numberOfPeople];
        do {
            this.g = new SparseGraph<Person, Connection>();
            for (int i = 0; i < numberOfPeople; i++) {
                // initialize all as having a positive vaccination opinion
                Person person = new Person(Integer.toString(i),"00");
                this.people[i] = person;
                this.g.addVertex(person);
            }
            // connect in ring
            for (int i = 0; i < numberOfPeople; i++) {
                for (int ii = 0; ii < k; ii++) {
                    int diff = ii/2 + 1; // integer division
                    if (ii%2 == 1) diff *= -1;
                    int newIndex = i + diff;
                    if (newIndex < 0) newIndex += numberOfPeople;
                    if (newIndex >= numberOfPeople) newIndex -= numberOfPeople;
                    this.g.addEdge(new Connection(),this.people[i],this.people[newIndex]);
                }
            }
            // random rewiring
            for (Connection edge:this.g.getEdges()) {
                if (this.random.nextDouble() < SimulationSettings.getInstance().getRewiringProbability()) {
                    // rewire this edge
                    Person source = this.g.getEndpoints(edge).getFirst();
                    Person newDestination;
                    do {
                        newDestination = this.people[this.random.nextInt(numberOfPeople)];
                    }
                    while (this.g.isNeighbor(source,newDestination) || source.equals(newDestination));
                    this.g.removeEdge(edge);
                    this.g.addEdge(new Connection(),source,newDestination);
                }
            }
            WeakComponentClusterer wcc = new WeakComponentClusterer();
            components = wcc.transform(this.g);
        }
        while (components.size() > 1);
        // make sure everything is connected
    }
}
