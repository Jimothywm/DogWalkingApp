package com.example.myapplication;

import android.widget.Button;

public class Model {
//    Button button;
    String name;

    public Model(String name)// need to add button back if break
    {
//        this.button = button;
        this.name = name;
    }
/*
    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }
*/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
