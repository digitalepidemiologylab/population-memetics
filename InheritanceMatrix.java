package com.salathe.populationmemetics;


import java.util.HashMap;

public class InheritanceMatrix {

    private static InheritanceMatrix ourInstance = new InheritanceMatrix();

    private InhertianceRule[][] inheritanceMatrix;

    HashMap<String,Integer> memotypeToIndex = new HashMap<String,Integer>();


    public static InheritanceMatrix getInstance() {
        return ourInstance;
    }

    private InheritanceMatrix() {
    }

    private class InhertianceRule {

        private String[] memotypes;
        private double[] probabilities;

        public String[] getMemotypes() {
            return memotypes;
        }

        public void setMemotypes(String[] memotypes) {
            this.memotypes = memotypes;
        }

        public double[] getProbabilities() {
            return probabilities;
        }

        public void setProbabilities(double[] probabilities) {
            this.probabilities = probabilities;
        }
    }

    public void generateMatrix() {
        this.memotypeToIndex.put("00",0);
        this.memotypeToIndex.put("01",1);
        this.memotypeToIndex.put("11",2);
        int numberOfGenotypes = 3;
        String[][] memotype_exposing_to_exposed = SimulationSettings.getInstance().getMemotype_exposing_to_exposed();
        double[][] probability_exposing_to_exposed = SimulationSettings.getInstance().getProbability_exposing_to_exposed();
        this.inheritanceMatrix = new InhertianceRule[numberOfGenotypes][numberOfGenotypes];
        for (int i = 0; i < numberOfGenotypes; i++) {
            for (int ii = 0; ii < numberOfGenotypes; ii++) {
                this.inheritanceMatrix[i][ii] = InheritanceMatrix.getInstance().new InhertianceRule();
                this.inheritanceMatrix[i][ii].setMemotypes(memotype_exposing_to_exposed[i*3 + ii]);
                this.inheritanceMatrix[i][ii].setProbabilities(probability_exposing_to_exposed[i * 3 + ii]);
            }
        }
    }

    public String[] getInheritanceMemotypes(String exposing_memotype, String exposed_memotype) {
        return this.inheritanceMatrix[this.memotypeToIndex.get(exposing_memotype)][this.memotypeToIndex.get(exposed_memotype)].getMemotypes();
    }

    public double[] getInheritanceProbabilities(String exposing_memotype, String exposed_memotype) {
        return this.inheritanceMatrix[this.memotypeToIndex.get(exposing_memotype)][this.memotypeToIndex.get(exposed_memotype)].getProbabilities();
    }





}
