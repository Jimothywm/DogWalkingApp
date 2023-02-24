package com.example.myapplication;

public class CSVModel {


    boolean checked;
    String name;

    public CSVModel(String name, boolean checked)
    {
        this.checked = checked;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
