package com.example.myapplication;

public class ModelShare {
    String pathID;
    String name;
    boolean sent;


    public ModelShare(String name, String pathID, boolean sent)// need to add button back if break
    {
        this.pathID = pathID;
        this.name = name;
        this.sent = sent;
    }

    public String getPathID() {
        return pathID;
    }

    public void setPathID(String pathID) {
        this.pathID = pathID;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
