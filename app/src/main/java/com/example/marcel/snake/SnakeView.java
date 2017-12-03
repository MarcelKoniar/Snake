package com.example.marcel.snake; /**
 * Created by Marcel on 24.11.2017.
 */

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Random;

public class SnakeView extends SurfaceView implements Runnable {

    private Thread thread = null;
    private volatile boolean playing;
    private Canvas canvas;
    private SurfaceHolder holder;
    private Paint paintSnake;
    private Paint paintMouse;
    private Paint paintScore;
    private Paint controlPanel;
    private Paint paintPause;
    private Paint paintPlay;
    private Context m_context; //reference to the activity
    private SoundPool soundPool;
    private int mouse_sound = -1;
    private int dead_sound = -1;
    private int highScore;

    //add sounds
    //direction
    public enum Direction {
        UP, RIGHT, DOWN, LEFT
    }

    //start heading direction
    private Direction direction = Direction.RIGHT;
    private int screenWidth;
    private int screenHeight;
    //control pausing between updates
    private long nextFrameTime;
    private final long FPS = 10;
    private final long ms = 1000;
    private int score;
    private int[] snakeX;
    private int[] snakeY;
    private int snakeLength;
    private int mouseX;
    private int mouseY;
    private int blockSize;
    //size in segments of the area
    private final int numBlockWide = 40;
    private int numBlocksHigh;

    public SnakeView(Context context, Point size) {
        super(context);
        m_context = context;
        screenWidth = size.x;
        screenHeight = size.y;
        // //Determine the size of each block/place on the game board
        blockSize = screenWidth / numBlockWide;
        // How many blocks of the same size will fit into the height
        numBlocksHigh = screenHeight / blockSize;

        //set sounds up
        loadSound();
        //initialize the drawing objects
        holder = getHolder();
        paintSnake = new Paint();
        paintMouse = new Paint();
        paintScore = new Paint();
        paintPause=new Paint();
        paintPlay=new Paint();
        controlPanel = new Paint();


        //if score 200 achievement
        snakeX = new int[200];
        snakeY = new int[200];

        startGame();


    }


    @Override
    public void run() {
        while (playing)//check if is not pause game
        {
            //update 10 times in second
            if (checkForUpdate()) {
                updateGame();
                drawGame();
            }
        }

    }

    public void pause() {
        playing = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            //error
        }
    }

    public void resume() {
        playing = true;
        thread = new Thread(this);
        thread.start();
    }

    public void startGame() {
        //snake with just head in the middle f screen
        snakeLength = 1;
        snakeX[0] = numBlockWide / 2;
        snakeY[0] = numBlocksHigh / 2;

        //point to eat(mouse)
        spawnMouse();
        score = 0;
        // Setup m_NextFrameTime so an update is triggered immediately
        nextFrameTime = System.currentTimeMillis();
    }

       public void loadSound(){
       soundPool=new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        try{
           AssetManager manager=m_context.getAssets();
           AssetFileDescriptor descriptor;

           //prepare sounds in memory
          descriptor=manager.openFd("heal.ogg");
         mouse_sound=soundPool.load(descriptor,0);
           descriptor=manager.openFd("dead.ogg");
           dead_sound=soundPool.load(descriptor,0);

        }catch(IOException e){

        }
    }
    public void spawnMouse() {
        Random r = new Random();
        mouseX = r.nextInt(numBlockWide - 10) + 1;
        mouseY = r.nextInt(numBlocksHigh - 1);

    }

    public void eatMouse() {
        //increase length
        snakeLength++;
        spawnMouse();
        score++;
        soundPool.play(mouse_sound, 1, 1, 0, 0, 1);
    }

    private void moveSnake() {
        // Move the body
        for (int i = snakeLength; i > 0; i--) {
            // Start at the back and move it
            // to the position of the segment in front of it
            snakeX[i] = snakeX[i - 1];
            snakeY[i] = snakeY[i - 1];
            // Exclude the head because
            // the head has nothing in front of it
        }
        // Move the head in the appropriate m_Direction
        switch (direction) {
            case UP:
                snakeY[0]--;
                break;
            case RIGHT:
                snakeX[0]++;
                break;
            case DOWN:
                snakeY[0]++;
                break;
            case LEFT:
                snakeX[0]--;
                break;

        }

    }

    private boolean detectDeath() {
        boolean dead = false;
        //wall colission
        if (snakeX[0] == -1) dead = true;
        if (snakeX[0] >= numBlockWide - 9) dead = true;
        if (snakeY[0] == -1) dead = true;
        if (snakeY[0] >= numBlocksHigh-1) dead = true;

        //body collision
        for (int i = snakeLength - 1; i > 0; i--) {
            if ((i > 4) && (snakeX[0] == snakeX[i]) && (snakeY[0] == snakeY[i])) {
                dead = true;
            }
        }
        return dead;
    }

    public void updateGame() {
        if (snakeX[0] == mouseX && snakeY[0] == mouseY) {
            eatMouse();
        }
        moveSnake();
        if (detectDeath()) {
            soundPool.play(dead_sound, 1, 1, 0, 0, 1);

            startGame();
        }
    }

    public void drawGame() {
        if (holder.getSurface().isValid()) {

            controlPanel.setColor(Color.argb(255, 0, 102, 0));
            canvas = holder.lockCanvas();
            //background
            canvas.drawColor(Color.argb(255, 0, 0, 0));
            // Set the color of the paint to draw the snake and mouse with
            paintSnake.setColor(Color.argb(255, 0, 102, 0));
            // Choose how big the score will be
            paintScore.setTextSize(50);
            paintScore.setColor(Color.argb(255, 255, 255, 255));
            paintMouse.setColor(Color.argb(255, 255, 255, 255));
            paintMouse.setTextSize(100);

            controlPanel.setColor(Color.BLUE);
            canvas.drawRect(screenWidth - 300, 0, screenWidth, screenHeight, controlPanel);
            canvas.drawText("Score:" + score, screenWidth - 230, 60, paintScore);

            //draw snake

            for (int i = 0; i < snakeLength; i++) {
                canvas.drawRect(snakeX[i] * blockSize, (snakeY[i] * blockSize), (snakeX[i] * blockSize) + blockSize, (snakeY[i] * blockSize) + blockSize, paintSnake);

            }
            //draw mouse
            canvas.drawRect(mouseX * blockSize,
                    (mouseY * blockSize),
                    (mouseX * blockSize) + blockSize,
                    (mouseY * blockSize) + blockSize,
                    paintMouse);
            //draw the whole frame
            //bitmap buttons...
            Bitmap bmpP1 = BitmapFactory.decodeResource(getResources(), R.mipmap.arrow);
             Bitmap pauseButton = BitmapFactory.decodeResource(getResources(), R.mipmap. pause);
            Bitmap playButton = BitmapFactory.decodeResource(getResources(), R.mipmap.play);

            bmpP1 = Bitmap.createScaledBitmap(bmpP1, 300, 300, false);
            pauseButton = Bitmap.createScaledBitmap( pauseButton,100, 100, false);
            playButton=Bitmap.createScaledBitmap( playButton,100, 100, false);

            canvas.drawBitmap(bmpP1, screenWidth - 300, screenHeight - 350, null);
            canvas.drawBitmap(pauseButton, screenWidth - 195, screenHeight - 500, null);
            canvas.drawBitmap(playButton, screenWidth - 195, screenHeight - 620, null);


            holder.unlockCanvasAndPost(canvas);


        }
    }

    public boolean checkForUpdate() {
        // Are we due to update the frame
        if (nextFrameTime <= System.currentTimeMillis()) {
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            nextFrameTime = System.currentTimeMillis() + ms / FPS;

            // Return true so that the update and draw
            // functions are executed
            return true;
        }

        return false;
    }

    public boolean onTouchEvent(MotionEvent event){
        int action = event.getAction();
        float x = event.getX(); // or getRawX();
        float y = event.getY();

        switch(action){
            case MotionEvent.ACTION_DOWN:

                if(direction==Direction.LEFT){
                    if (x >= screenWidth - 200 && x < (screenWidth - 100)
                            && y >= screenHeight - 350 && y < (screenHeight - 250) )  {
                        direction=Direction.UP;

                    }
                    if (x >= screenWidth - 200 && x < (screenWidth - 100)
                            && y >= 500 ) {
                        direction=Direction.DOWN;
                    }

                }
                if(direction==Direction.RIGHT){
                    if (x >= screenWidth - 200 && x < (screenWidth - 100)
                            && y >= screenHeight - 350 && y < (screenHeight - 250) )  {
                        direction=Direction.UP;

                    }
                    if (x >= screenWidth - 200 && x < (screenWidth - 100)
                            && y >= 500 ) {
                        direction=Direction.DOWN;
                    }
                }
                if(direction==Direction.UP){
                    if (x >= screenWidth - 300 && x < (screenWidth - 200)
                            && y >= 500 ) {
                        direction=Direction.LEFT;
                    }
                    if (x >= screenWidth - 100
                       && y >= 500 ) {
                       direction=Direction.RIGHT;
               }
                }
                if(direction==Direction.DOWN){
                    if (x >= screenWidth - 300 && x < (screenWidth - 200)
                            && y >= 500 ) {
                        direction=Direction.LEFT;
                    }
                    if (x >= screenWidth - 100
                            && y >= 500 ) {
                        direction=Direction.RIGHT;
                    }
                }

                if (x >= screenWidth - 195 && x < (screenWidth - 95)
                        && y >= screenHeight - 620 && y < (screenHeight - 520) ) {

                    //resume
                    Log.wtf("x:"+x,"y:"+y);
                    resume();
                }
                if (x >= screenWidth - 195 && x < (screenWidth - 95)
                        && y >= screenHeight - 500 && y < (screenHeight - 400) ) {

                    //pauseGame
                    Log.wtf("x:"+x,"y:"+y);
                    pause();
                }


                break;//  canvas.drawBitmap(bmpP1, screenWidth - 300, screenHeight - 350, null);screenHeight - 350
        }
        return true;
    }
    public int getScore(){
        return score;
    }
}

