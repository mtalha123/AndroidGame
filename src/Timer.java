package com.ryzer.games.tapemall;

/**
 * Created by pc on 08/11/14.
 */
public class Timer {

    private long startTime = 0, endTime = 0;

    private boolean stopped = false;
    private boolean started = false;

    public void start(){
        if(!started) {
            startTime = System.currentTimeMillis();
            started = true;
        }
    }

    public int check(){
        if(started) {
            if (!stopped) {
                endTime = System.currentTimeMillis();
            }
            return (int) (endTime - startTime);
        }else{
            return 0;
        }
    }

    public void reset(){
        startTime = 0;
        endTime = 0;
        stopped = false;
        started = false;
    }
}
