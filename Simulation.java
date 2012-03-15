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
        this.setTwoRandomNeighboursTo11();
        this.runTimesteps();
    }

    private void setTwoRandomNeighboursTo11() {
        Person randomPerson = this.people[this.random.nextInt(SimulationSettings.getInstance().getNumberOfPeople())];
        randomPerson.setMemotype("11");
        for (Person neighbour:this.g.getNeighbors(randomPerson)) {
            neighbour.setMemotype("11");
            break;
        }
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
                if (transmissionOccurs) person.addTempMemotype(neighbour.getMemotype());
            }
        }
        // assign new opinions according to temp value and reset
        for (Person person:this.g.getVertices()) {
            if (person.getTempMemotypes().size() == 0) continue;
            else {
//                System.out.println("# temp memotypes = " +  person.getTempMemotypes().size());
                // each exposing memotype gets to affect the exposed memotype. Because the order can matter, we simply shuffle.
                person.shuffleTempMemotypes();
                String currentMemotype = person.getMemotype();
                for (String exposing_memoype:person.getTempMemotypes()) {
//                    if (currentMemotype.equals("11")) System.out.println("11");
                    currentMemotype = this.getOffspringMemotype(currentMemotype,exposing_memoype);
                }
                // now viability check
                boolean isViable = false;
                if (currentMemotype.equals("00") && this.random.nextDouble() < SimulationSettings.getInstance().getViability00()) isViable = true;
                if (currentMemotype.equals("01") && this.random.nextDouble() < SimulationSettings.getInstance().getViability01()) isViable = true;
                if (currentMemotype.equals("11") && this.random.nextDouble() < SimulationSettings.getInstance().getViability11()) isViable = true;
                if (isViable) person.setMemotype(currentMemotype);
                person.resetTempMemotypes();
            }
        }
    }

    private String getOffspringMemotype(String memotype_exposed, String memotype_exposing) {
        String offspringMemotype = memotype_exposed;
        // for the stanard model the exposing should always be 00, so we only need to take care of 11
        if (memotype_exposing.equals("11")) {
            if (memotype_exposed.equals("00")) offspringMemotype = "01";
            else if (memotype_exposed.equals("01")) offspringMemotype = "11";
            else offspringMemotype = "11";
        }
        return offspringMemotype;
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
