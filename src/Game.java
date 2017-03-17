package com.ryzer.games.tapemall;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import java.util.Random;

/**
 * Created by pc on 03/09/14.
 */

//COLOR END RGB = 65,116,138

public class Game implements View.OnTouchListener {

    private Thread drawingThread;

    private Canvas canvas;

    private static float blackLineLeft, blackLineTop;

    private Bitmap middleBlackLine;

    //four different screens (left and right, and two more left and right above the first ones on the screen) for scrolling down
    private static PatternHolder leftFirstScreen, leftSecondScreen;
    private static PatternHolder rightFirstScreen, rightSecondScreen;

    //screenWidth and screenHeight hold screen width pixels and screen height pixels
    private static int screenWidth, screenHeight;

    private static Context context;

    //startTime, endTime, and difference used for calculating the deltaTime variable. deltaTime holds the the time(in milliseconds) drawing one frame takes
    private static long startTime = 0, endTime = 0;
    private static float deltaTime, difference;

    //if this is not true, then BOTH updating and drawing wont happen (i.e. game is quit completely)
    private static boolean running = false;

    //states of the game. Used to differentiate between which part game is in to draw appropriate graphics and update appropriately
    public static final int STATE_LOADING = 4215;
    public static final int STATE_START_SCREEN = 3541;
    public static final int STATE_IN_GAME = 2541;

    public static int currentState;

    //paint used for score
    private static Paint scorePaint;

    //the start screen
    static StartScreen startScreen;

    //Arrays for holding touch coordinates for left and right screen (screen divided in half for multi-touch capability)
    private static CustomArray xArrayRight = new CustomArray();
    private static CustomArray yArrayRight = new CustomArray();
    private static CustomArray xArrayLeft = new CustomArray();
    private static CustomArray yArrayLeft = new CustomArray();

    //score var for keeping updating score, scoreCheck set to score at certain points (to be checked with scoreInterval to see to increase speed or not),
    //scoreInterval for the intervals in the score for increasing the speed
    private static int score = 0, scoreCheck = 0, scoreInterval = 5;

    private Bitmap loadingScreen;

    private static float scoreTextLeft;

    private static Bitmap arrowLeft, arrowRight;
    private static float arrowTop;
    private static boolean arrowUpdating = false;
    private static boolean arrowBlinking = true;
    private static Paint arrowPaint = new Paint();

    public static int gameHeight;

    private static int bombIncrease = 0;

    private static int touchResults;

    private static Timer timer = new Timer();

    private static boolean USER_CONTROL = false;

    private static float scrollSpeed;

    private static Timer gameLostTimer = new Timer();

    private static SoundPool soundPool;
    private static int coinSoundId;

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private static Bitmap livesBitmaps[];
    private static int lives = 3;

    private static boolean paused;

    private static int backgroundR = 115, backgroundG = 209, backgroundB = 250;

    private static int currentHighScore;

    private static Handler handler;
    private static Message message;

    private static boolean exitCompletely = false;

    private static Random adChance = new Random();

    private MediaPlayer backgroundMusic;

    private static int colors[];

    public Game(Context cxt, Handler mHandler){
        context = cxt;

        handler = mHandler;

        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;

        sharedPreferences = context.getSharedPreferences("DATA", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        scorePaint = new Paint();
        scorePaint.setTextSize((float) (0.10 * cxt.getResources().getDisplayMetrics().widthPixels));
        scorePaint.setColor(Color.YELLOW);

        startScreen = new StartScreen(cxt);

        loadingScreen = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(cxt.getResources(), R.drawable.loadingscreen), screenWidth, screenHeight, true);

        currentHighScore = getHighScoreFromFile();

        scoreTextLeft = screenWidth/2 - (0.02f * screenWidth);

        gameHeight = screenHeight - (int)(screenHeight * 0.20f);

        scrollSpeed = 0.30f * screenHeight;

        colors = new int[10];
        colors[0] = Color.rgb(115, 209, 250);
        colors[1] = Color.rgb(255, 216, 140);
        colors[2] = Color.rgb(140, 154, 255);
        colors[3] = Color.rgb(255, 129, 129);

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        coinSoundId = soundPool.load(context, R.raw.coinsoundeffect, 1);

        backgroundMusic = MediaPlayer.create(context, R.raw.backgroundmusic);
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.7f, 0.7f);

        currentState = STATE_LOADING;

    }
    //the SurfaceHolder parameter is final because the thread anonymous class is an inner class and in order to access the enclosing class method parameters
    //the parameters have to be final
    public void start(final SurfaceHolder sHolder){
        running = true;
        drawingThread = new Thread(){
            public void run(){
                while(!exitCompletely) {
                    if(running) {
                        draw(sHolder);
                        endTime = System.currentTimeMillis();
                        difference = endTime - startTime;
                        deltaTime = difference / 1000;
                        update(deltaTime);
                        if (currentState == STATE_LOADING) {
                            load();
                            //SurfaceViewActivity.showInterstitialAfterLoading();
                        }
                        processTouches();

                        startTime = System.currentTimeMillis();
                    }

                }
            }
        };

        drawingThread.start();
    }

    private void update(float deltatime){
        if(currentState == STATE_START_SCREEN){
            leftFirstScreen.scrollStartScreen(deltatime);
            leftSecondScreen.scrollStartScreen(deltatime);
            rightFirstScreen.scrollStartScreen(deltatime);
            rightSecondScreen.scrollStartScreen(deltatime);

            if (leftFirstScreen.hasPassed()) {
                leftFirstScreen.generateAndAttach(leftSecondScreen);
            }
            if (leftSecondScreen.hasPassed()) {
                leftSecondScreen.generateAndAttach(leftFirstScreen);
            }
            if (rightFirstScreen.hasPassed()) {
                rightFirstScreen.generateAndAttach(rightSecondScreen);
            }
            if (rightSecondScreen.hasPassed()) {
                rightSecondScreen.generateAndAttach(rightFirstScreen);
            }
        }

        if(currentState == STATE_IN_GAME) {

            if(paused){
                USER_CONTROL = false;
                PatternHolder.stopScrolling();
            }

            if (leftFirstScreen.hasLostByCoin() || leftSecondScreen.hasLostByCoin() || rightFirstScreen.hasLostByCoin() || rightSecondScreen.hasLostByCoin()) {

                USER_CONTROL = false;
                leftFirstScreen.moveBack();
                leftSecondScreen.moveBack();
                rightFirstScreen.moveBack();
                rightSecondScreen.moveBack();
            }

            updateScoreAndSpeedStuff();

            if(PatternHolder.getBombFreqPercent() < 30) {
                if (score >= bombIncrease) {
                    PatternHolder.increaseBombFreqPercent();

                    bombIncrease += 50;
                }
            }

            updateArrow(deltatime);

            leftFirstScreen.update(deltatime);
            leftSecondScreen.update(deltatime);
            rightFirstScreen.update(deltatime);
            rightSecondScreen.update(deltatime);

            if (leftFirstScreen.hasPassed()) {
                leftFirstScreen.generateAndAttach(leftSecondScreen);
            }
            if (leftSecondScreen.hasPassed()) {
                leftSecondScreen.generateAndAttach(leftFirstScreen);
            }
            if (rightFirstScreen.hasPassed()) {
                rightFirstScreen.generateAndAttach(rightSecondScreen);
            }
            if (rightSecondScreen.hasPassed()) {
                rightSecondScreen.generateAndAttach(rightFirstScreen);
            }
        }

    }

    private void draw(SurfaceHolder sHolder){
        canvas = sHolder.lockCanvas();
        if(currentState == STATE_LOADING) {
            canvas.drawBitmap(loadingScreen, 0.0f, 0.0f, null);
        }

        if (currentState == STATE_START_SCREEN) {

            canvas.drawRGB(backgroundR, backgroundG, backgroundB);
            canvas.drawBitmap(middleBlackLine, blackLineLeft, blackLineTop, null);

            leftFirstScreen.draw(canvas);
            leftSecondScreen.draw(canvas);
            rightFirstScreen.draw(canvas);
            rightSecondScreen.draw(canvas);

            startScreen.draw(canvas);
        }

        if(currentState == STATE_IN_GAME) {
            canvas.drawRGB(backgroundR, backgroundG, backgroundB);
            canvas.drawBitmap(middleBlackLine, blackLineLeft, blackLineTop, null);


            leftFirstScreen.draw(canvas);
            leftSecondScreen.draw(canvas);
            rightFirstScreen.draw(canvas);
            rightSecondScreen.draw(canvas);

            canvas.drawText("" + score, scoreTextLeft, (float) (0.15 * screenHeight), scorePaint);

            canvas.drawBitmap(arrowLeft, leftFirstScreen.getFirstCoin().getLeft() + (Math.round(Coin.getWidth()) / 2) - (arrowLeft.getWidth() / 2), arrowTop, arrowPaint);
            canvas.drawBitmap(arrowRight, rightFirstScreen.getFirstCoin().getLeft() + (Math.round(Coin.getWidth()) / 2) - (arrowRight.getWidth() / 2), arrowTop, arrowPaint);


            for(int c = 0; c < lives; c++){
                canvas.drawBitmap(livesBitmaps[c], c * (0.05f * screenWidth) + (0.01f * screenWidth), 0.01f * screenHeight, null);
            }
        }

        sHolder.unlockCanvasAndPost(canvas);
    }

    public static void stopRunning(){
        running = false;
    }

    private void load(){
        //Sets up the background and black line at the middle
        middleBlackLine = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.middleblackline), (int)(screenWidth * 0.01f), screenHeight, true);
        blackLineLeft = (screenWidth / 2) - (middleBlackLine.getWidth()/2);
        blackLineTop = 0.0f;
        ///////////

        //Sets up arrow stuff
        arrowLeft = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.arrow), (int) (screenWidth * 0.20f), (int) (screenHeight * 0.20f), true);
        arrowRight = Bitmap.createBitmap(arrowLeft);
        arrowTop = (screenHeight - arrowLeft.getHeight());
        arrowPaint.setAlpha(255);
        ///////////

        ///sets up lives (heart images)
        livesBitmaps = new Bitmap[3];
        livesBitmaps[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.heart), (int) (0.08f * screenHeight), (int) (0.08f * screenHeight), true);
        livesBitmaps[1] = Bitmap.createBitmap(livesBitmaps[0]);
        livesBitmaps[2] = Bitmap.createBitmap(livesBitmaps[0]);

        //////

        leftFirstScreen = new PatternHolder(context, PatternHolder.LEFT_SCREEN,screenWidth, screenHeight, middleBlackLine.getWidth());
        rightFirstScreen = new PatternHolder(context, PatternHolder.RIGHT_SCREEN, screenWidth, screenHeight, middleBlackLine.getWidth());

        leftSecondScreen = new PatternHolder(context, leftFirstScreen,PatternHolder.LEFT_SCREEN, screenWidth, screenHeight, middleBlackLine.getWidth());
        rightSecondScreen = new PatternHolder(context, rightFirstScreen, PatternHolder.RIGHT_SCREEN, screenWidth, screenHeight, middleBlackLine.getWidth());

        backgroundMusic.start();

        currentState = STATE_START_SCREEN;

        USER_CONTROL = true;
    }


    private static void processTouches() {
        //process right screen touches
        if (xArrayRight.getLength() != 0) {

            if (currentState == STATE_START_SCREEN) {
                if (startScreen.touched(xArrayRight.get(0), yArrayRight.get(0))) {

                    leftFirstScreen.reset();
                    leftSecondScreen.reset(leftFirstScreen);
                    rightFirstScreen.reset();
                    rightSecondScreen.reset(rightFirstScreen);

                    leftFirstScreen.allowTouch = true;
                    rightFirstScreen.allowTouch = true;

                    PatternHolder.stopScrolling();

                    currentState = STATE_IN_GAME;

                    message = Message.obtain();
                    message.arg1 = 1;
                    handler.sendMessage(message);
                }

                xArrayRight.remove(0);
                yArrayRight.remove(0);
            }

            if (currentState == STATE_IN_GAME) {
                if (xArrayRight.getLength() != 0 && xArrayRight.get(0) != -1) {

                        touchResults = rightFirstScreen.touched(xArrayRight.get(0), yArrayRight.get(0));
                        if (touchResults == 1) {
                            score++;
                            arrowUpdating = true;
                            arrowBlinking = false;
                            soundPool.play(coinSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
                            if(score > currentHighScore){
                                scorePaint.setColor(Color.RED);
                            }
                        } else if (touchResults == -1) {
                            saveCoinStuffToFile();
                            lost();
                        }

                        touchResults = rightSecondScreen.touched(xArrayRight.get(0), yArrayRight.get(0));
                        if (touchResults == 1) {
                            score++;
                            arrowUpdating = true;
                            arrowBlinking = false;
                            soundPool.play(coinSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
                            if(score > currentHighScore){
                                scorePaint.setColor(Color.RED);
                            }
                        } else if (touchResults == -1) {
                            saveCoinStuffToFile();
                            lost();
                        }

                        if (xArrayRight.getLength() > 0 && yArrayRight.getLength() > 0) {
                            xArrayRight.remove(0);
                            yArrayRight.remove(0);
                        }
                }
            }
        }
            //----END OF RIGHT SIDE OF SCREEN-----

            //LEFT SIDE OF SCREEN
            if (xArrayLeft.getLength() != 0) {
                if (currentState == STATE_START_SCREEN) {
                    if (startScreen.touched(xArrayLeft.get(0), yArrayLeft.get(0))) {

                        leftFirstScreen.reset();
                        leftSecondScreen.reset(leftFirstScreen);
                        rightFirstScreen.reset();
                        rightSecondScreen.reset(rightFirstScreen);

                        leftFirstScreen.allowTouch = true;
                        rightFirstScreen.allowTouch = true;


                        PatternHolder.stopScrolling();

                        currentState = STATE_IN_GAME;

                        message = Message.obtain();
                        message.arg1 = 1;
                        handler.sendMessage(message);
                    }
                    xArrayLeft.remove(0);
                    yArrayLeft.remove(0);
                }

                if (currentState == STATE_IN_GAME) {
                    if (xArrayLeft.getLength() != 0 && xArrayLeft.get(0) != -1) {

                        touchResults = leftFirstScreen.touched(xArrayLeft.get(0), yArrayLeft.get(0));
                            if (touchResults == 1) {
                                score++;
                                arrowUpdating = true;
                                arrowBlinking = false;
                                soundPool.play(coinSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
                                if(score > currentHighScore){
                                    scorePaint.setColor(Color.RED);
                                }
                            } else if (touchResults == -1) {
                                saveCoinStuffToFile();
                                lost();
                            }

                            touchResults = leftSecondScreen.touched(xArrayLeft.get(0), yArrayLeft.get(0));
                            if (touchResults == 1) {
                                score++;
                                arrowUpdating = true;
                                arrowBlinking = false;
                                soundPool.play(coinSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
                                if(score > currentHighScore){
                                    scorePaint.setColor(Color.RED);
                                }
                            } else if (touchResults == -1) {
                                saveCoinStuffToFile();
                                lost();
                            }

                            if (xArrayLeft.getLength() > 0 && yArrayLeft.getLength() > 0) {
                                xArrayLeft.remove(0);
                                yArrayLeft.remove(0);
                            }
                    }
                }
            }
            //----END OF LEFT SIDE OF SCREEN----
        }


    private static void lost(){

        score = 0;
        scoreCheck = 0;
        scoreInterval = 5;

        scoreTextLeft = screenWidth/2 - (0.02f * screenWidth);

        leftFirstScreen.reset();
        leftSecondScreen.reset(leftFirstScreen);

        rightFirstScreen.reset();
        rightSecondScreen.reset(rightFirstScreen);

        arrowTop = (screenHeight - (screenHeight * 0.20f));
        arrowUpdating = false;
        arrowBlinking = true;

        bombIncrease = 50;
        USER_CONTROL = true;

        scrollSpeed = 0.30f * screenHeight;

        gameLostTimer.reset();

        paused = false;

        lives = 3;

        backgroundR = 115;
        backgroundG = 209;
        backgroundB = 250;

        currentHighScore = getHighScoreFromFile();
        scorePaint.setColor(Color.YELLOW);

        if(currentState == STATE_IN_GAME) {
            if ((adChance.nextInt(2) + 1) == 1) {
                message = Message.obtain();
                message.arg1 = 2;
                handler.sendMessage(message);
            }
        }
    }


    private void updateScoreAndSpeedStuff(){
        switch(score){
            case 101:
                scoreInterval = 30;
                break;
            case 331:
                scoreInterval = 100;
                break;
            case 431:
                scoreInterval = 200;
                break;
        }

        //if score is less than 500, increases speed each time score passes the interval amount
        if(score < 800) {
            if (score >= scoreCheck + scoreInterval) {
                scrollSpeed += 0.038 * screenHeight;
                if(backgroundR > 50) {
                    backgroundR -= 2;
                    backgroundG -= 2;
                    backgroundB -= 2;
                }
                scoreCheck += scoreInterval;
            }
        }

        //aligns score text depending on if its 2 digits or 3 digits
        if(score >= 100){
            scoreTextLeft = screenWidth/2 - (0.08f * screenWidth);
        }else if(score >= 10){
            scoreTextLeft = screenWidth/2 - (0.06f * screenWidth);
        }
    }

    private void updateArrow(float deltatime){
        if(arrowUpdating){
            if(arrowTop < screenHeight) {
                arrowTop += deltatime * (0.30f * screenHeight);
            }
        }

        if(arrowBlinking){
            if(timer.check() == 0){
                timer.start();
            }

            if(timer.check() >= 500 && arrowPaint.getAlpha() == 255){
                arrowPaint.setAlpha(0);
                timer.reset();
                timer.start();
            }else if(timer.check() >= 500 && arrowPaint.getAlpha() == 0){
                arrowPaint.setAlpha(255);
                timer.reset();
                timer.start();
            }

        }else{
            arrowPaint.setAlpha(0);
        }
    }

    public static void userNoControl(){
        USER_CONTROL = false;
    }

    private static void addValues(float x, float y){
        if(x >= screenWidth / 2){
            xArrayRight.add(x);
            yArrayRight.add(y);
        }else{
            xArrayLeft.add(x);
            yArrayLeft.add(y);
        }

    }

    public static float getScrollSpeed(){
        return scrollSpeed;
    }

    public static float getScrollSpeedIncrement(){
        return scrollSpeed * deltaTime;
    }

    public boolean onTouch(View v, MotionEvent event) {
        if(USER_CONTROL) {
            int pointerId;

            if (currentState == STATE_IN_GAME) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    pointerId = 0;
                    addValues(event.getX(), event.getY());
                }

                if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                    pointerId = event.getPointerId(1);
                    addValues(event.getX(event.findPointerIndex(pointerId)), event.getY(event.findPointerIndex(pointerId)));
                }
            } else {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    addValues(event.getX(), event.getY());
                }

            }
        }

        return true;
    }


    public static void RESET_NOW(){
        saveCoinStuffToFile();
        lost();
    }

    public static void goBack(){
        if(currentState == STATE_IN_GAME) {
            currentState = STATE_START_SCREEN;
            lost();
        }
    }

    public static int getState(){
        return currentState;
    }

    public static void pause(){
        paused = true;
    }

    public static void resume(){
        paused = false;
        running = true;
        exitCompletely = false;
        if(currentState != STATE_LOADING) {
            USER_CONTROL = true;
        }
        if(currentState == STATE_START_SCREEN || (currentState == STATE_IN_GAME && score != 0)) {
            PatternHolder.resumeScrolling();
        }
    }

    public static int getHighScoreFromFile(){
        return sharedPreferences.getInt("score", 0);
    }

    public static float getTotalCoinsFromFile(){
        return sharedPreferences.getFloat("totalcoins", 0);
    }

    //saves both score and totalcoins to file
    private static void saveCoinStuffToFile(){
        if(sharedPreferences.getInt("score",0) < score) {
            editor.putInt("score", score);
        }
        editor.putFloat("totalcoins", sharedPreferences.getFloat("totalcoins",0) + score);
        editor.apply();
    }

    public static void reduceLife(){
        lives -= 1;
    }

    public static int getLives(){
        return lives;
    }

    public static void exitCompletely(){
        exitCompletely = true;
    }

    public static void stopShowingAds(){
        message = Message.obtain();
        message.arg1 = 1;
        handler.sendMessage(message);
    }

}
