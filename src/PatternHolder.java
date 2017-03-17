package com.ryzer.games.tapemall;

import android.content.Context;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.Random;

/**
 * Created by pc on 03/09/14.
 */
public class PatternHolder {

    public static final int LEFT_SCREEN = 5756;
    public static final int RIGHT_SCREEN = 3565;
    private int screenWidthFull, screenWidthHalf, screenHeight;
    private static Context context;

    private static boolean scrolling = false;

    private Coin coins[] = new Coin[10];

    private Random random = new Random();

    private int screenType;

    private int currentCoinIndex = 0;

    private static int bombFreqPercent = 0;

    private static boolean LOSTBYCOIN = false;
    private static boolean LOSTBYBOMB = false;

    private boolean CURRENTPATTERNHOLDERINLOSS = false;

    public boolean allowTouch = false;

    private static SoundPool soundPool;
    private static int plopSoundId;

    public PatternHolder(Context cxt, int screentype, int screenwidth, int screenheight, int middleblacklinewidth){
        context = cxt;

        screenType = screentype;

        screenWidthFull = screenwidth;
        screenWidthHalf = screenwidth - (screenwidth / 2) + (middleblacklinewidth/2);
        screenHeight = screenheight;

        newCoins(true);

        attachments(screenType, 0);

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        plopSoundId = soundPool.load(context, R.raw.plop, 1);
    }


    public PatternHolder(Context cxt, PatternHolder pHolder, int screentype, int screenwidth, int screenheight, int middleblacklinewidth){
        context = cxt;

        screenType = screentype;

        screenWidthFull = screenwidth;
        screenWidthHalf = screenwidth - (screenwidth / 2) + (middleblacklinewidth/2);
        screenHeight = screenheight;

        newCoins(true);

        coins[0].attach(pHolder.getLastCoin(), 2);
        coins[1].attach(coins[0], 2);

        attachments(screenType, 2);

    }

    public void update(float deltaTime){
        if(scrolling) {
            scroll(deltaTime);
        }

        if(LOSTBYCOIN){
            if(CURRENTPATTERNHOLDERINLOSS) {
                if (coins[currentCoinIndex].playLostAnimation(Coin.TYPE_COIN)) {
                    LOSTBYCOIN = false;
                    Game.RESET_NOW();
                }
            }
        }else if(LOSTBYBOMB){
            if (currentCoinIndex >= 10) {
                if (coins[currentCoinIndex -1].playLostAnimation(Coin.TYPE_BOMB)) {
                    LOSTBYBOMB = false;
                    Game.RESET_NOW();
                }
            }else{
                if (coins[currentCoinIndex].playLostAnimation(Coin.TYPE_BOMB)) {
                    LOSTBYBOMB = false;
                    Game.RESET_NOW();
                }
            }
        }
    }

    public void draw(Canvas canvas){
        for(int i = 0; i < coins.length; i++) {
            coins[i].draw(canvas);
        }
    }

    private void scroll(float deltaTime){
        for (int i = 0; i < coins.length; i++) {
            coins[i].update(deltaTime);
        }
    }

    public void scrollStartScreen(float deltaTime){
        scrolling = true;
        scroll(deltaTime);
    }

    public void generateAndAttach(PatternHolder patternHolder){

        newCoins(false);
        currentCoinIndex = 0;

        coins[0].attach(patternHolder.getLastCoin(), 2);
        coins[1].attach(coins[0], 2);

        attachments(patternHolder.screenType, 2);
    }

    private void attachments(int screenType, int startingIndex){

        int randomNumber = 2;
        boolean leftPossible = true;
        boolean rightPossible = true;
        Random weightedChance = new Random();

        if(startingIndex == 0){
            boolean redo = true;

            while(redo) {
                if (screenType == LEFT_SCREEN) {
                    coins[0].setLeft(random.nextInt(screenWidthHalf / 2));
                    coins[0].setTop(Game.gameHeight - Coin.getHeight());

                    if(((coins[0].getLeft() - (Coin.getWidth() * 2)) <= 0) && ((coins[0].getLeft() + (Coin.getWidth() * 3)) >= screenWidthHalf)){

                    }else{
                        redo = false;
                    }

                } else {
                    coins[0].setLeft(random.nextInt(screenWidthHalf / 2) + screenWidthHalf);
                    coins[0].setTop(Game.gameHeight - coins[0].getHeight());

                    if(((coins[0].getLeft() - (Coin.getWidth() * 2)) <= screenWidthHalf) && ((coins[0].getLeft() + (Coin.getWidth() * 3)) >= screenWidthFull)){

                    }else{
                        redo = false;
                    }
                }
            }

            coins[1].attach(coins[0], 2);
            startingIndex+=2;
        }

        if(screenType == LEFT_SCREEN){

            for(int c = startingIndex; c<coins.length;c++) {

                if(leftPossible && rightPossible){
                    if(weightedChance.nextInt(100) + 1 > 60){
                        randomNumber = 2;
                    }else if(weightedChance.nextInt(100) + 1 < 30){
                        randomNumber = 1;
                    }else{
                        randomNumber = 3;
                    }
                }else if(!leftPossible && !rightPossible) {
                    randomNumber = 2;
                }else if(!rightPossible){
                    if(weightedChance.nextInt(100) + 1 > 60){
                        randomNumber = 2;
                    }else {
                        randomNumber = 1;
                    }
                }else if(!leftPossible){
                    if(weightedChance.nextInt(100) + 1 > 60){
                        randomNumber = 2;
                    }else {
                        randomNumber = 3;
                    }

                }

                switch (randomNumber) {
                    case 1:
                        if(c > 1 && (coins[c-1].getAttachmentPoint() != Coin.LEFT_SIDE) && (coins[c-2].getAttachmentPoint() != Coin.LEFT_SIDE)) {
                            if ((coins[c - 1].getLeft() - (coins[c].getWidth() * 2)) > 0) {
                                coins[c].attach(coins[c - 1], 1);
                            } else {
                                leftPossible = false;
                                c -= 1;
                            }
                        }else{
                            leftPossible = false;
                            c-=1;
                        }
                        break;

                    case 2:
                        if(c > coins.length - 2) {
                            coins[c].attach(coins[c - 1], 2);
                        }else{
                            coins[c].attach(coins[c - 1], 2);
                            c+=1;
                            coins[c].attach(coins[c - 1], 2);
                        }
                        leftPossible = true;
                        rightPossible = true;
                        break;

                    case 3:
                        if(c > 1 && (coins[c-1].getAttachmentPoint() != Coin.RIGHT_SIDE) && (coins[c-2].getAttachmentPoint() != Coin.RIGHT_SIDE)) {
                            if ((coins[c - 1].getLeft() + (coins[c].getWidth() * 3)) < screenWidthHalf) {
                                coins[c].attach(coins[c - 1], 3);
                            } else {
                                rightPossible = false;
                                c -= 1;
                            }
                        }else{
                            rightPossible = false;
                            c-=1;
                        }
                        break;
                }
            }

        }else if(screenType == RIGHT_SCREEN){

            for(int c = startingIndex; c<coins.length;c++) {

                if(leftPossible && rightPossible){
                    randomNumber = random.nextInt(3) + 1;
                }else if(!leftPossible && !rightPossible) {
                    randomNumber = 2;
                }else if(!rightPossible){
                    randomNumber = random.nextInt(2) + 1;
                }else if(!leftPossible){
                    randomNumber = random.nextInt(2) + 2;
                }

                switch (randomNumber) {
                    case 1:
                        if(c > 1 && (coins[c-1].getAttachmentPoint() != Coin.LEFT_SIDE) && (coins[c-2].getAttachmentPoint() != Coin.LEFT_SIDE)) {
                            if ((coins[c - 1].getLeft() - (coins[c].getWidth() * 2)) > screenWidthHalf) {
                                coins[c].attach(coins[c - 1], 1);
                            } else {
                                leftPossible = false;
                                c -= 1;
                            }
                        }else{
                            leftPossible = false;
                            c-=1;
                        }
                        break;

                    case 2:
                        if(c > coins.length - 2) {
                            coins[c].attach(coins[c - 1], 2);
                        }else{
                            coins[c].attach(coins[c - 1], 2);
                            c+=1;
                            coins[c].attach(coins[c - 1], 2);
                        }
                        leftPossible = true;
                        rightPossible = true;
                        break;

                    case 3:
                        if(c > 1 && (coins[c-1].getAttachmentPoint() != Coin.RIGHT_SIDE) && (coins[c-2].getAttachmentPoint() != Coin.RIGHT_SIDE)) {
                            if ((coins[c - 1].getLeft() + (coins[c].getWidth() * 3)) < screenWidthFull) {
                                coins[c].attach(coins[c - 1], 3);
                            } else {
                                rightPossible = false;
                                c -= 1;
                            }
                        }else{
                            rightPossible = false;
                            c-=1;
                        }
                        break;
                }
            }

        }
    }

    private void newCoins(boolean firstTime){
        boolean previousCoinTypeBomb = false;

        for(int c = 0; c < coins.length; c++){
            if(random.nextInt(100) + 1 <= bombFreqPercent && c < coins.length - 1) {
                if(!previousCoinTypeBomb) {
                    if(firstTime){
                        coins[c] = new Coin(context, screenWidthFull, Coin.TYPE_BOMB);
                    }else {
                        coins[c].reset(Coin.TYPE_BOMB);
                    }
                    previousCoinTypeBomb = true;
                }else{
                    c-=1;
                }
            }else{
                previousCoinTypeBomb = false;
                if(firstTime){
                    coins[c] = new Coin(context, screenWidthFull, Coin.TYPE_COIN);
                }else {
                    coins[c].reset(Coin.TYPE_COIN);
                }
            }
        }
    }

    public void reset(){
        bombFreqPercent = 0;

        newCoins(false);

        attachments(screenType, 0);

        scrolling = false;

        currentCoinIndex = 0;

        allowTouch = true;

    }

    public void reset(PatternHolder patternHolder){
        bombFreqPercent = 0;

        newCoins(false);

        coins[0].attach(patternHolder.getLastCoin(), 2);
        attachments(screenType, 1);

        currentCoinIndex = 0;

        allowTouch = false;

    }

    public static void increaseBombFreqPercent(){
        bombFreqPercent += 5;
    }

    public boolean hasPassed(){
        if(coins[coins.length - 1].getTop() >= screenHeight){
            return true;
        }else {
            return false;
        }
    }

    public int touched(float x,float y) {
        if (currentCoinIndex < coins.length) {

            if (coins[currentCoinIndex].getCurrentType() == Coin.TYPE_BOMB) {

                if ((x >= coins[currentCoinIndex + 1].getLeft() - Coin.leeway && x <= coins[currentCoinIndex + 1].getLeft() + Coin.getWidth() + Coin.leeway) && y >= coins[currentCoinIndex + 1].getTop() - Coin.leeway
                        && y <= coins[currentCoinIndex + 1].getTop() + Coin.getHeight() + Coin.leeway) {

                    coins[currentCoinIndex + 1].touched(Coin.TYPE_COIN);
                    currentCoinIndex += 2;
                    scrolling = true;
                    return 1;
                }
            }
            if ((x >= coins[currentCoinIndex].getLeft() - Coin.leeway && x <= coins[currentCoinIndex].getLeft() + Coin.getWidth() + Coin.leeway) && y >= coins[currentCoinIndex].getTop() - Coin.leeway
                    && y <= coins[currentCoinIndex].getTop() + Coin.getHeight() + Coin.leeway) {

                if (coins[currentCoinIndex].getCurrentType() == Coin.TYPE_BOMB) {

                    bombFreqPercent = 0;
                    scrolling = false;
                    LOSTBYBOMB = true;
                    Game.userNoControl();
                    return 1;

                } else {

                    coins[currentCoinIndex].touched(Coin.TYPE_COIN);
                    currentCoinIndex++;
                    scrolling = true;
                    return 1;
                }

            }

        }
        return 0;
    }

    public boolean hasLostByCoin() {
        if (currentCoinIndex < coins.length) {
            if (coins[currentCoinIndex].getCurrentType() != Coin.TYPE_BOMB) {
                if (coins[currentCoinIndex].getTop() >= screenHeight) {
                    if (Game.getLives() > 0) {
                        Game.reduceLife();
                        coins[currentCoinIndex].hide(Coin.TYPE_COIN);
                        soundPool.play(plopSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
                        currentCoinIndex++;
                    } else {
                        CURRENTPATTERNHOLDERINLOSS = true;
                        Game.userNoControl();
                        bombFreqPercent = 0;
                        scrolling = false;
                        LOSTBYCOIN = true;
                        return true;
                    }
                }
            }else {
                if (coins[currentCoinIndex + 1].getTop() >= screenHeight) {
                    if (Game.getLives() > 0) {
                        Game.reduceLife();
                        coins[currentCoinIndex + 1].hide(Coin.TYPE_COIN);
                        soundPool.play(plopSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
                        currentCoinIndex += 2;
                    } else {
                        CURRENTPATTERNHOLDERINLOSS = true;
                        bombFreqPercent = 0;
                        scrolling = false;
                        LOSTBYCOIN = true;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Coin getFirstCoin(){
        return coins[0];
    }
    public Coin getLastCoin(){
        return coins[coins.length - 1];
    }

    public static void stopScrolling(){
        scrolling = false;
    }

    public static int getBombFreqPercent() {
        return bombFreqPercent;
    }

    public void moveBack(){
        for (int i = 0; i < coins.length; i++) {
            coins[i].moveBack(Coin.getHeight() + (0.01f * screenHeight) + Game.getScrollSpeedIncrement());
        }
    }

    public static void resumeScrolling(){
        scrolling = true;
    }

}
