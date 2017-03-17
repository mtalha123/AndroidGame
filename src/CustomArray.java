package com.ryzer.games.tapemall;

import java.util.LinkedList;

/**
 * Created by pc on 25/10/14.
 */
public class CustomArray {

    private LinkedList<Float> list;

    public CustomArray(){
        list = new LinkedList<Float>();
    }

    public synchronized void add(float a){
        list.add(a);
    }

    public synchronized float get(int index){
        if(list.size() > 0) {
            return list.get(index);
        }else{
            return -1;
        }
    }

    public synchronized void remove(int index){
        list.remove(index);
    }

    public synchronized int getLength(){
        return list.size();
    }
}
