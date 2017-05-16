package com.example.lg.work10;

/**
 * Created by LG on 2017-05-11.
 */

public class Memo {
    String name;
    String date;
    public Memo(String name, String date){
        this.name = name;
        this.date = date;
    }

    @Override
    public String toString() {
        return name;
    }
}
