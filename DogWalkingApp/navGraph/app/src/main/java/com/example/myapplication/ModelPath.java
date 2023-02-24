package com.example.myapplication;

public class ModelPath {
    String ID;
    String name;
    String friendName;



    public ModelPath(String name, String ID, String friendName)// need to add button back if break
    {
        this.ID = ID;
        this.name = name;
        this.friendName = friendName;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
