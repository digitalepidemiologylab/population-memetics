package com.salathe.populationmemetics;


import java.util.ArrayList;
import java.util.Collections;

public class Person {

    private String id;
    private String memotype;
    private ArrayList<String> tempMemotypes;


    public Person(String id, String memotype) {
        this.id = id;
        this.memotype = memotype;
        this.tempMemotypes = new ArrayList<String>();
    }

    public String toString() {
        return this.id;
    }

    public String getMemotype() {
        return this.memotype;
    }

    public void setMemotype(String memotype) {
        this.memotype = memotype;
    }

    public void addTempMemotype(String memotype) {
        this.tempMemotypes.add(memotype);
    }

    public ArrayList<String> getTempMemotypes() {
        return this.tempMemotypes;
    }

    public void resetTempMemotypes() {
        this.tempMemotypes.clear();
    }

    public void shuffleTempMemotypes() {
        Collections.shuffle(this.tempMemotypes);
    }
}
