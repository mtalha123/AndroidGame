package com.ryzer.games.tapemall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;


/**
 * Created by pc on 24/09/14.
 */
public class StartScreen {

    private Bitmap title, scoreBackground, playButton, helpButton, helpScreen;
    static private int screenWidth, screenHeight;
    private int playButtonLeft, playButtonTop;
    private int helpButtonLeft;
    Paint boardTextPaint;
    Paint numberPaint;

    private static boolean drawHelpScreen = false;

    private Context context;

    public StartScreen(Context context){

        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;

        title = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.titletext), screenWidth + (int)(0.01f * screenWidth), screenHeight, true);
        scoreBackground = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.highscoreboard2), (int)(screenWidth * 0.60f), (int)(screenHeight * 0.50f), true);
        helpScreen = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.helpscreen), screenWidth, screenHeight, true);
        playButton = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.playbutton), (int)(screenWidth * 0.10f), (int)(screenWidth * 0.10f), true);
        helpButton = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.helpbutton), (int)(screenWidth * 0.10f), (int)(screenWidth * 0.10f), true);


        playButtonLeft = (int)(screenWidth * 0.05f);// screenWdith / 2 - (playButton.getWidth() + playButton.getWidth()/2);
        playButtonTop =  screenHeight - (int)(screenHeight/2 + (screenHeight * 0.07f));//screenHeight - (int)(screenWidth * 0.11f);

        helpButtonLeft = screenWidth - (int)(screenWidth * 0.05f + helpButton.getWidth());// screenWidth / 2 + (helpButton.getWidth()/2);

        boardTextPaint = new Paint();
        boardTextPaint.setTypeface(Typeface.create("Cooper Std", Typeface.BOLD));
        boardTextPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.highscoresize));

        numberPaint = new Paint();
        numberPaint.setTypeface(Typeface.create("Cooper Std", Typeface.BOLD));
        numberPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.highscoresize));
        numberPaint.setColor(Color.RED);
        this.context = context;
    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(title, -1 * (float) (screenWidth * 0.01), 0.0f, null);
        canvas.drawBitmap(scoreBackground, screenWidth / 2 - (screenWidth * 0.30f), (screenHeight / 2) - ((screenHeight * 0.50f) / 2), null);
        canvas.drawBitmap(playButton, playButtonLeft, playButtonTop, null);
        canvas.drawBitmap(helpButton, helpButtonLeft, playButtonTop, null);

        canvas.drawText("Highscore: ", ((screenWidth / 2) - (screenWidth * 0.20f)), ((screenHeight / 2) - (float)(screenHeight * 0.08)), boardTextPaint);
        canvas.drawText("Total Coins Collected: ", ((screenWidth / 2) - (screenWidth * 0.20f)), ((screenHeight / 2) + (float)(screenHeight * 0.08)), boardTextPaint);
        canvas.drawText("" + Game.getHighScoreFromFile(), ((screenWidth / 2) - (screenWidth * 0.20f)) + boardTextPaint.measureText("Highscore: ")/*(screenWidth / 2) - (0.03f * screenWidth)*/, ((screenHeight / 2) - (float)(screenHeight * 0.08)), numberPaint);

        if(Game.getTotalCoinsFromFile() < 10000){
            canvas.drawText("" + (int)Game.getTotalCoinsFromFile(), ((screenWidth / 2) - (screenWidth * 0.20f)) + boardTextPaint.measureText("Total Coins Collected: ")/*((screenWidth / 2) + (screenWidth * 0.15f))*/, ((screenHeight / 2) + (screenHeight * 0.08f)), numberPaint);
        }else if(Game.getTotalCoinsFromFile() >= 10000 && Game.getTotalCoinsFromFile() < 1000000){
            canvas.drawText("" + String.format("%.1f", Game.getTotalCoinsFromFile() / 1000) + "K", ((screenWidth / 2) - (screenWidth * 0.20f)) + boardTextPaint.measureText("Total Coins Collected: "), ((screenHeight / 2) + (screenHeight * 0.08f)), numberPaint);
        }else {
            canvas.drawText("" + String.format("%.1f", Game.getTotalCoinsFromFile() / 1000000) + "M", ((screenWidth / 2) - (screenWidth * 0.20f)) + boardTextPaint.measureText("Total Coins Collected: ")/*((screenWidth / 2) + (screenWidth * 0.15f))*/, ((screenHeight / 2) + (screenHeight * 0.08f)), numberPaint);
        }

        if(drawHelpScreen){
            canvas.drawBitmap(helpScreen, 0.0f, 0.0f, null);
        }

    }

    public boolean touched(float x, float y){
        if (x >= playButtonLeft && x <= playButtonLeft + (int) (screenWidth * 0.10f) && y >= playButtonTop && y <= playButtonTop + (int) (screenWidth * 0.10f)) {
            return true;
        }else if(x >= helpButtonLeft && x <= helpButtonLeft + (int) (screenWidth * 0.10f) && y >= playButtonTop && y <= playButtonTop + (int) (screenWidth * 0.10f)){
            drawHelpScreen = true;
            Game.stopShowingAds();
        }

        return false;
    }

    public static boolean isOnHelpScreen(){
        if(drawHelpScreen){
            return true;
        }else{
            return false;
        }
    }

    public static void goBack(){
        drawHelpScreen = false;
    }
}
