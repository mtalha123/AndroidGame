package com.ryzer.games.tapemall;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by pc on 08/09/14.
 */
public class TheSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    SurfaceHolder holder;
    Game mainGame;
    Context context;

    Handler handler;

    public TheSurfaceView(Context context) {
        super(context);
        this.context = context;
        holder = getHolder();
        holder.addCallback(this);

        handler = new Handler(){
            public void handleMessage(Message msg) {
                if(msg.arg1 == 1) {
                    SurfaceViewActivity.removeAds();
                }

                if(msg.arg1 == 2) {
                    SurfaceViewActivity.showInterstitial();
                }
            }
        };

        mainGame = new Game(context, handler);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mainGame.start(holder);
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

}
