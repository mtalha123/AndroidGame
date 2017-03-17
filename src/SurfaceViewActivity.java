package com.ryzer.games.tapemall;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Process;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;


//TEST AD ID ca-app-pub-3940256099942544/6300978111

public class SurfaceViewActivity extends Activity{

    TheSurfaceView sView;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    AlertDialog dialog;
    public static RelativeLayout relativeLayout;

    static AdView startScreenAd, startScreenAd2;
    AdRequest adRequest;

    static InterstitialAd interstitialAdInGame;
    static InterstitialAd interstitialAdStartScreen;

    static RelativeLayout adRelativeLayout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //The following two lines make the activity fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        RelativeLayout.LayoutParams  params = new RelativeLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        adRelativeLayout = new RelativeLayout(this);
        adRelativeLayout.setLayoutParams(params);

        interstitialAdStartScreen = new InterstitialAd(this);
        interstitialAdStartScreen.setAdUnitId("ca-app-pub-1826090010359736/8935233409");

        interstitialAdInGame = new InterstitialAd(this);
        interstitialAdInGame.setAdUnitId("ca-app-pub-1826090010359736/8935233409");

        adRequest = new AdRequest.Builder().build();

        interstitialAdStartScreen.loadAd(adRequest);
        interstitialAdInGame.loadAd(adRequest);

        interstitialAdStartScreen.setAdListener(new AdListener() {

            public void onAdClosed() {
                interstitialAdStartScreen.loadAd(adRequest);
            }

            public void onAdFailedToLoad(int errorCode) {
                interstitialAdStartScreen.loadAd(adRequest);
            }
        });

        interstitialAdInGame.setAdListener(new AdListener() {

            public void onAdClosed() {
                interstitialAdInGame.loadAd(adRequest);
            }

            public void onAdFailedToLoad(int errorCode) {
                interstitialAdInGame.loadAd(adRequest);
            }
        });

        interstitialAdStartScreen.show();

        startScreenAd = new AdView(this);
        startScreenAd.setAdSize(AdSize.SMART_BANNER);
        startScreenAd.setAdUnitId("ca-app-pub-1826090010359736/5981767004");
        //startScreenAd.setY(getResources().getDisplayMetrics().heightPixels - (int) (getResources().getDisplayMetrics().widthPixels * 0.08f));
        //startScreenAd.setX((getResources().getDisplayMetrics().widthPixels / 2 - (int)(getResources().getDisplayMetrics().widthPixels * 0.24f)));
        //startScreenAd.setScaleX(10.0f);
        //startScreenAd.setRotation(-90.0f);
        startScreenAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                super.onAdLoaded();
                startScreenAd.bringToFront();
            }
        });

//        startScreenAd2 = new AdView(this);
//        startScreenAd2.setAdSize(AdSize.BANNER);
//        startScreenAd2.setAdUnitId("ca-app-pub-1826090010359736/5981767004");
//        startScreenAd2.setX(getResources().getDisplayMetrics().widthPixels - adSize.getWidthInPixels(this));
//        startScreenAd2.setRotation(90.0f);
//
//        startScreenAd2.setAdListener(new AdListener() {
//            public void onAdLoaded() {
//                super.onAdLoaded();
//                startScreenAd2.bringToFront();
//            }
//        });

        //startScreenAd2.loadAd(adRequest);

        adRelativeLayout.addView(startScreenAd);

        relativeLayout = new RelativeLayout(this);
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        sharedPreferences = getSharedPreferences("DATA", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        sView = new TheSurfaceView(this);
        sView.setOnTouchListener(sView.mainGame);

        relativeLayout.addView(sView);
        startScreenAd.loadAd(adRequest);
        relativeLayout.addView(adRelativeLayout);

        setContentView(relativeLayout);
    }

    protected void onStop() {
        Game.exitCompletely();
        super.onStop();
        this.finish();
        android.os.Process.killProcess(Process.myPid());
    }

    protected void onDestroy() {
        startScreenAd.destroy();
        super.onDestroy();
    }

    protected void onPause() {
        super.onPause();
        Game.stopRunning();
    }

    protected void onResume() {
        super.onResume();
        Game.resume();
    }

    public void onBackPressed() {
            if (Game.getState() == Game.STATE_IN_GAME) {
                Game.pause();
                dialog = new AlertDialog.Builder(this)
                        .setTitle("Alert")
                        .setMessage("Are you sure you want to go to start menu?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Game.goBack();
                                adRelativeLayout.addView(startScreenAd);
                                interstitialAdStartScreen.show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Game.resume();
                            }
                        }).setOnKeyListener(new DialogInterface.OnKeyListener() {

                            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                if(keyCode == KeyEvent.KEYCODE_BACK){
                                    dialog.cancel();
                                    Game.resume();
                                }
                                return true;
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }else if (StartScreen.isOnHelpScreen()) {
                StartScreen.goBack();
                adRelativeLayout.addView(startScreenAd);
            }else {
                super.onBackPressed();
                Game.resume();
            }
    }

    public static void removeAds(){
        adRelativeLayout.removeView(startScreenAd);
    }

    public static void showInterstitial(){
        interstitialAdInGame.show();
    }
}
