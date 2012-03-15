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
        this.runTimesteps();
    }

    private void runTimesteps() {
        while(true) {
            if (this.currentTimestep==0) this.socialContagion = true;
            if (this.socialContagion) {
                this.socialContagion();
                this.currentTimestep++;
            }
            else break;
        }
    }

    private void socialContagion() {
        for (Person person:this.g.getVertices()) {
            for (Person neighbour:this.g.getNeighbors(person)) {
                person.addTempMemotype(this.getOffspringMemotype(person.getMemotype(), neighbour.getMemotype()));
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
            else return "00";
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
