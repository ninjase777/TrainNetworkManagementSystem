package com.mycompany.trainnetworkmanagement;
public class Edge {
    private Station destination;
    private int distance;

    public Edge(Station destination, int distance) {
        this.destination = destination;
        this.distance = distance;
    }

    

    public Station getDestination() {
        return destination;
    }

    public void setDestination(Station destination) {
        this.destination = destination;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
    
}
