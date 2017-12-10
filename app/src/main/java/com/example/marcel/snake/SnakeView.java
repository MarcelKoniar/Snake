package com.example.marcel.snake; /**
 * Created by Marcel on 24.11.2017.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.FileOutputStream;
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
    private Context m_context;//reference to the activity
    private Paint snakeHead;
    private SoundPool soundPool;
    private int mouse_sound = -1;
    private int dead_sound = -1;
    private int highScore;




//    mySharedEditor=mySharedPref.edit();
//        mySharedEditor.putInt("month", globalMonth);
//        mySharedEditor.putInt("day",dayOfMonth);
//        mySharedEditor.apply();;
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
    private final long ms = 1500;
    private static int score;
    private int[] snakeX;
    private int[] snakeY;
    private int snakeLength;
    private int mouseX;
    private int mouseY;
    private int blockSize;
    //size in segments of the area
    private final int numBlockWide = 40;
    private int numBlocksHigh;
    private boolean sounds;
    private Vibrator mVibrator;

    SharedPreferences mySharedPref;
    SharedPreferences.Editor mySharedEditor;
    public SnakeView(Context context, Point size,boolean Settingssounds) {
        super(context);
        m_context = context;
        screenWidth = size.x;
        screenHeight = size.y;
        // //Determine the size of each block/place on the game board
        blockSize = screenWidth / numBlockWide;
        // How many blocks of the same size will fit into the height
        numBlocksHigh = screenHeight / blockSize;
       sounds=Settingssounds;
        Log.wtf("sounds:  "+Settingssounds,"");
        //set sounds up

            loadSound();
        mVibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);


        //initialize the drawing objects
        holder = getHolder();
        paintSnake = new Paint();
        paintMouse = new Paint();
        paintScore = new Paint();
        paintPause=new Paint();
        paintPlay=new Paint();
        snakeHead=new Paint();
        controlPanel = new Paint();



//        mySharedPref=getSharedPreferences("myPref", context.MODE_PRIVATE);
//        mySharedEditor=mySharedPref.edit();
//        mySharedEditor.putInt("score", getScore());
//        mySharedEditor.putInt("day",dayOfMonth);
      //  mySharedEditor.apply();;

        //if score 200 achievement
        snakeX = new int[200];
        snakeY = new int[200];

        startGame();


    }
//    public SnakeView(boolean sounds){
//        super(sounds);
//        this.sounds=sounds;
//
//    }

    public void vibrate(){
        Vibrator v = (Vibrator) m_context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
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
           // mVibrator.vibrate(300);

            startGame();
        }
    }

    public void drawGame() {
        if (holder.getSurface().isValid()) {

            controlPanel.setColor(Color.argb(255, 0, 102, 0));
            canvas = holder.lockCanvas();
            //background
            canvas.drawColor(Color.argb(255, 255, 255, 255));
            // Set the color of the paint to draw the snake and mouse with
            paintSnake.setColor(Color.argb(255, 0, 102, 0));
//            Paint snakeHead=new Paint();
//            snakeHead.setColor(Color.argb(255, 0, 102, 0));
            // Choose how big the score will be
            paintScore.setTextSize(50);
            paintScore.setColor(Color.argb(255, 255, 255, 255));
            paintMouse.setColor(Color.argb(255, 255, 255, 255));
            paintMouse.setTextSize(100);
//            snakeHead.setColor (Color.argb(255, 255, 255, 255));
            snakeHead.setColor(Color.BLUE);
            controlPanel.setColor(Color.BLUE);
            canvas.drawRect(screenWidth - 298, 0, screenWidth, screenHeight, controlPanel);
            canvas.drawText("Score:" + score, screenWidth - 230, 60, paintScore);

            //draw snake

            for (int i = 0; i < snakeLength; i++) {
                if(i==0){
                    canvas.drawRect(snakeX[i] * blockSize, (snakeY[i] * blockSize), (snakeX[i] * blockSize) + blockSize, (snakeY[i] * blockSize) + blockSize, snakeHead);
                }
                else
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
            Bitmap fly= BitmapFactory.decodeResource(getResources(), R.mipmap.mucha);

            bmpP1 = Bitmap.createScaledBitmap(bmpP1, 300, 300, false);
            pauseButton = Bitmap.createScaledBitmap( pauseButton,100, 100, false);
            playButton=Bitmap.createScaledBitmap( playButton,100, 100, false);
            fly=Bitmap.createScaledBitmap( fly,blockSize, blockSize, false);

            canvas.drawBitmap(bmpP1, screenWidth - 300, screenHeight - 350, null);
            canvas.drawBitmap(pauseButton, screenWidth - 195, screenHeight - 500, null);
            canvas.drawBitmap(playButton, screenWidth - 195, screenHeight - 620, null);
            canvas.drawBitmap(fly, mouseX * blockSize, mouseY * blockSize, null);


            holder.unlockCanvasAndPost(canvas);


        }
    }
    public static void vibrateDevice(Context mContext){
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
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
                    Log.wtf("score: "+getScore(),"y:"+y);
                    pause();
                }


                break;//  canvas.drawBitmap(bmpP1, screenWidth - 300, screenHeight - 350, null);screenHeight - 350
        }
        return true;
    }
//    public static void setScore(int s) {
//        score=s;
//    }

    public static int getScore(){
        return score;
    }

//    String FILENAME = "hello_file";
//    String string = "hello world!";
//
//    FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
//fos.write(string.getBytes());
//fos.close();
//SharedPreferences prefs = m_context.getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
//    Editor editor = prefs.edit();
//editor.putInt("key", 1);
//editor.commit();
    //getting preferences
    //SharedPreferences prefs = this.getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
    //int score = prefs.getInt("key", 0); //0 is the default value
}

