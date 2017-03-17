package com.ryzer.games.tapemall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Created by pc on 24/01/15.
 */
public class Coin {

    public static final int LEFT_SIDE = 1022;
    public static final int RIGHT_SIDE = 5921;
    public static final int BOTTOM_SIDE = 2414;

    public static final int TYPE_COIN = 2053;
    public static final int TYPE_BOMB = 2076;

    private int attachmentPoint;

    private static float width, height;

    private float top = 1000.0f, left = 1000.0f;

    private Bitmap coin;
    private Bitmap coinRed;
    private Bitmap star;
    private Bitmap bomb;
    private Bitmap bombRed;

    private Paint coinPaint = new Paint(), starPaint = new Paint(), bombPaint = new Paint();

    private Timer starTimer = new Timer();
    private Timer lostTimer = new Timer();

    public static float leeway;

    private int currentType;

    private boolean drawRedCoin = false;
    private boolean drawRedBomb = false;

    private int animationCounter = 0;

    private boolean animationStarted = false;

    private SoundPool soundPool;
    private int wrongSound;

    private boolean drawGreenCoin = false;

    public Coin(Context context, int screenWidth, int type){

        currentType = type;

        width = 0.11f * screenWidth;
        height = width;

        leeway = 0.07f * screenWidth;

        coin = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.goldcoin), (int)width, (int)height, true);
        coinRed = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.goldcoinred), (int)width, (int)height, true);
        star = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.star), (int)width, (int)height, true);
        bomb = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.bomb), (int)width, (int)height, true);
        bombRed = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.bombred), (int)width, (int)height, true);

        starPaint.setAlpha(0);

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        wrongSound = soundPool.load(context, R.raw.beep, 1);

    }

    public void draw(Canvas canvas){
        if(currentType == TYPE_COIN) {
            canvas.drawBitmap(coin, left, top, coinPaint);
            canvas.drawBitmap(star, left, top, starPaint);

            if(drawRedCoin){
                canvas.drawBitmap(coinRed, left, top, null);
            }
        }else{
            canvas.drawBitmap(bomb, left, top, bombPaint);

            if(drawRedBomb){
                canvas.drawBitmap(bombRed, left, top, bombPaint);
            }
        }
    }

    public void update(float deltaTime){
        top += deltaTime * Game.getScrollSpeed();
        if(starTimer.check() > 700){
            starPaint.setAlpha(0);
            starTimer.reset();
        }

    }

    public void attach(Coin targetCoin, int targetSide){
       // SIDES ->    1 = Left    2 = Top     3 = Right

        switch(targetSide){
            case 1:
                top = targetCoin.top;
                left = targetCoin.left - (width * 2);

                attachmentPoint = RIGHT_SIDE;
                break;

            case 2:
                top = targetCoin.top - (width * 2);
                left = targetCoin.left;

                attachmentPoint = BOTTOM_SIDE;
                break;

            case 3:
                top = targetCoin.top;
                left = targetCoin.left + (Coin.width * 2);

                attachmentPoint = LEFT_SIDE;
                break;

        }

    }

    public boolean playLostAnimation(int type){
        if (animationCounter >= 4) {
            animationStarted = false;
            animationCounter = 0;
            return true;
        }

        if (!animationStarted) {
            animationStarted = true;
         }

        lostTimer.start();

        if (lostTimer.check() >= 500) {
                animationCounter++;
            if(type == TYPE_COIN) {
                if (!drawRedCoin) {
                    soundPool.play(wrongSound, 1.0f, 1.0f, 1, 0, 1.0f);
                    drawRedCoin = true;
                } else {
                    drawRedCoin = false;
                    }
            }else{
                if (!drawRedBomb) {
                    soundPool.play(wrongSound, 0.7f, 0.7f, 1, 0, 1.0f);
                    drawRedBomb = true;
                } else {
                    drawRedBomb = false;
                }
            }
            lostTimer.reset();
        }


        return false;
    }

    public void touched(int type){
        if(type == TYPE_COIN) {
            starPaint.setAlpha(255);
            coinPaint.setAlpha(0);
            starTimer.start();
        }
    }

    public void hide(int type){
        if(type == TYPE_COIN){
            coinPaint.setAlpha(0);
        }else {
            bombPaint.setAlpha(0);
        }
    }

    public void reset(int type){
        coinPaint.setAlpha(255);
        starPaint.setAlpha(0);
        currentType = type;
        drawRedCoin = false;
        drawRedBomb = false;
        bombPaint.setAlpha(255);
        drawGreenCoin = false;
        animationCounter = 0;
    }

    //"Moves" the coin back up by its height pixels on the screen (used for when user loses by not pressing coin in time)
    public void moveBack(float moveBackAmount){
        top -= moveBackAmount;
    }


    public void setLeft(float left) {
        this.left = left;
    }
    public float getLeft() {
        return left;
    }

    public void setTop(float top) {
        this.top = top;
    }
    public float getTop() {
        return top;
    }

    public static float getWidth() {
        return width;
    }

    public static float getHeight() {
        return height;
    }

    public int getAttachmentPoint() {
        return attachmentPoint;
    }

    public int getCurrentType() {
        return currentType;
    }
}
