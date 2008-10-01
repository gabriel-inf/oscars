package net.es.oscars.pathfinder.perfsonar.util;

import org.jgrapht.graph.DefaultWeightedEdge;

public class PSGraphEdge extends DefaultWeightedEdge {
    private double bandwidth;

    public PSGraphEdge() {
        super();
    }

    public double getBandwidth() {
        return this.bandwidth;
    }

    public void setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
    }
} 
