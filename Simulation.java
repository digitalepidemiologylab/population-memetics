package com.salathe.populationmemetics;


import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.visualization.renderers.Renderer;

import java.util.Random;
import java.util.Set;

public class Simulation {

    public final static int GRAPH_TYPE_WATTS_STROGATZ_1D = 1;
    public final static int GRAPH_TYPE_WATTS_STROGATZ_2D = 2;

    public final static int EDGE_REWIRING = 1;
    public final static int EDGE_PERMUTATION = 2;


    Graph<Person, Connection> g;
    int currentTimestep = 0;
    Person[] people;
    Random random;
    boolean socialContagion = false;
    boolean hasSuccessfullyFinished = false;

    double[] fractionMemotype00;
    double[] fractionMemotype01;
    double[] fractionMemotype11;

    public static void main(String[] args) {
        Simulation simulation = new Simulation();
        simulation.run();
    }

    public void run() {
        this.random = new Random();
        this.init();
        this.runTimesteps();
    }

    private void init() {
        InheritanceMatrix.getInstance().generateMatrix();
        this.fractionMemotype00 = new double[SimulationSettings.getInstance().getSlidingWindow()];
        this.fractionMemotype01 = new double[SimulationSettings.getInstance().getSlidingWindow()];
        this.fractionMemotype11 = new double[SimulationSettings.getInstance().getSlidingWindow()];
        this.initGraph(Simulation.GRAPH_TYPE_WATTS_STROGATZ_2D, Simulation.EDGE_PERMUTATION);
//        this.setRandomNeighboursTo11();
        this.setRandomMemotypes();
    }

    private void setRandomMemotypes() {
        String[] memotypes = {"00","11"};
        for (Person person:this.g.getVertices()) {
            person.setMemotype(memotypes[this.random.nextInt(memotypes.length)]);
        }
    }

    private void setRandomNeighboursTo11() {
        Person randomPerson = this.people[this.random.nextInt(SimulationSettings.getInstance().getNumberOfPeople())];
        randomPerson.setMemotype("11");
        for (Person neighbour:this.g.getNeighbors(randomPerson)) {
            neighbour.setMemotype("11");
        }
    }

    private void runTimesteps() {
        while(true) {
            if (this.currentTimestep==0) this.socialContagion = true;
            if (this.socialContagion) {
                this.socialContagion();
                this.gatherData();
//                this.checkWhetherToStop();
                this.currentTimestep++;
            }
            else break;
        }
    }

    private void checkWhetherToStop() {
        // TODO make this generic
        // saturation, stop + success:
        if (this.getFrequencyOfMemotype("11") >= 0.99) {
            this.socialContagion = false;
            this.hasSuccessfullyFinished = true;
        }
        // no change, stop + no success:
        if ( this.fractionMemotype11[0] ==  this.fractionMemotype11[SimulationSettings.getInstance().getSlidingWindow()-1]) this.socialContagion = false;
    }

    private void gatherData() {
        int slidingWindow = SimulationSettings.getInstance().getSlidingWindow();
        this.fractionMemotype00[this.currentTimestep % slidingWindow] = this.getFrequencyOfMemotype("00");
        this.fractionMemotype01[this.currentTimestep % slidingWindow] = this.getFrequencyOfMemotype("01");
        this.fractionMemotype11[this.currentTimestep % slidingWindow] = this.getFrequencyOfMemotype("11");
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
        // assign new memotypes according to exposing memotype
        double viability00 = SimulationSettings.getInstance().getViability00();
        double viability01 = SimulationSettings.getInstance().getViability01();
        double viability11 = SimulationSettings.getInstance().getViability11();
        for (Person person:this.g.getVertices()) {
            if (person.getTempMemotypes().size() == 0) continue;
            else {
                // each exposing memotype gets to affect the exposed memotype. Because the order can matter, we simply shuffle.
                person.shuffleTempMemotypes();
                String currentMemotype = person.getMemotype();
                for (String exposing_memoype:person.getTempMemotypes()) {
                    currentMemotype = this.getOffspringMemotype(exposing_memoype,currentMemotype);
                }
                // now viability check
                boolean isViable = false;
                if (currentMemotype.equals("00") && this.random.nextDouble() < viability00) isViable = true;
                if (currentMemotype.equals("01") && this.random.nextDouble() < viability01) isViable = true;
                if (currentMemotype.equals("11") && this.random.nextDouble() < viability11) isViable = true;
                if (isViable) person.setMemotype(currentMemotype);
                // finally reset
                person.resetTempMemotypes();
            }
        }
    }

    private String getOffspringMemotype(String memotype_exposing, String memotype_exposed) {
        String[] memoypes = InheritanceMatrix.getInstance().getInheritanceMemotypes(memotype_exposing, memotype_exposed);
        double[] probabilities = InheritanceMatrix.getInstance().getInheritanceProbabilities(memotype_exposing, memotype_exposed);
        if (probabilities.length == 1) return memoypes[0];
        else {
            // resampling wheel algorithm for one sample
            int N = probabilities.length;
            int index = random.nextInt(N);
            double max = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < N; i++) {
                if (probabilities[i] > max) max = probabilities[i];
            }
            double beta = random.nextDouble();
            beta += random.nextDouble() * 2.0 * max;
            while (beta > probabilities[index]) {
                beta -= probabilities[index];
                index = (index + 1) % N;
            }
            return memoypes[index];
        }
    }

    public boolean hasSuccessfullyFinished() {
        return this.hasSuccessfullyFinished;
    }


    private void initGraph(int graphType, int rewiringType) {
        Set components;
        int numberOfPeople = SimulationSettings.getInstance().getNumberOfPeople();
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
            if (graphType == Simulation.GRAPH_TYPE_WATTS_STROGATZ_1D) this.init1DLattice();
            if (graphType == Simulation.GRAPH_TYPE_WATTS_STROGATZ_2D) this.init2DLattice();
            // random rewiring or permutation
            if (rewiringType == Simulation.EDGE_REWIRING) this.rewireEdges();
            if (rewiringType == Simulation.EDGE_PERMUTATION) this.permuteEdges();
            WeakComponentClusterer wcc = new WeakComponentClusterer();
            components = wcc.transform(this.g);
        }
        while (components.size() > 1);
        // make sure everything is connected
    }

     private void init1DLattice() {
        int numberOfPeople = SimulationSettings.getInstance().getNumberOfPeople();
        int k = SimulationSettings.getInstance().getK();
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
    }

    private void init2DLattice() {
        int numberOfPeople = SimulationSettings.getInstance().getNumberOfPeople();
        int dimension = (int)Math.sqrt(numberOfPeople);
        for (int i = 0; i < dimension; i++) {
            for (int ii = 0; ii < dimension; ii++) {
                this.g.addEdge(new Connection(),this.people[i*dimension + ii],this.people[this.getIndex(i-1,ii-1,dimension)]);
                this.g.addEdge(new Connection(),this.people[i*dimension + ii],this.people[this.getIndex(i-1,ii,dimension)]);
                this.g.addEdge(new Connection(),this.people[i*dimension + ii],this.people[this.getIndex(i-1,ii+1,dimension)]);
                this.g.addEdge(new Connection(),this.people[i*dimension + ii],this.people[this.getIndex(i,ii-1,dimension)]);
                this.g.addEdge(new Connection(),this.people[i*dimension + ii],this.people[this.getIndex(i,ii+1,dimension)]);
                this.g.addEdge(new Connection(),this.people[i*dimension + ii],this.people[this.getIndex(i+1,ii-1,dimension)]);
                this.g.addEdge(new Connection(),this.people[i*dimension + ii],this.people[this.getIndex(i+1,ii,dimension)]);
                this.g.addEdge(new Connection(),this.people[i*dimension + ii],this.people[this.getIndex(i+1,ii+1,dimension)]);
            }
        }
    }

    private void rewireEdges() {
        int numberOfPeople = SimulationSettings.getInstance().getNumberOfPeople();
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
    }

    private void permuteEdges() {
        int numberOfEdgesToPermute = (int)(SimulationSettings.getInstance().getRewiringProbability() * this.g.getEdgeCount());
        while (numberOfEdgesToPermute > 0) {
            // permute this edge
            Object[] allEdges = this.g.getEdges().toArray();
            Connection randomEdge, edge;
            Person source1, source2, destination1, destination2;
            do {
                edge  = (Connection)(allEdges[this.random.nextInt(allEdges.length)]);
                randomEdge  = (Connection)(allEdges[this.random.nextInt(allEdges.length)]);
                source1 = this.g.getEndpoints(edge).getFirst();
                destination1 = this.g.getEndpoints(edge).getSecond();
                source2 = this.g.getEndpoints(randomEdge).getFirst();
                destination2 = this.g.getEndpoints(randomEdge).getSecond();
            }
            while (edge.equals(randomEdge) || source1.equals(source2) || destination1.equals(destination2)|| source1.equals(destination2) || source2.equals(destination1));
            this.g.removeEdge(edge);
            this.g.removeEdge(randomEdge);
            this.g.addEdge(new Connection(),source1,destination2);
            this.g.addEdge(new Connection(),source2,destination1);
            numberOfEdgesToPermute--;
         }
    }



    private int getIndex(int i, int ii, int dimension) {
        if (i < 0) i += dimension;
        if (ii < 0) ii += dimension;
        if (i >= dimension) i -= dimension;
        if (ii >= dimension) ii -= dimension;
        return i*dimension + ii;
    }


}
