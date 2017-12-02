package com.example.marcel.snake; /**
 * Created by Marcel on 24.11.2017.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class SnakeView extends SurfaceView implements Runnable{

    private Thread thread=null;
    private volatile  boolean playing;
    private Canvas canvas;
    private SurfaceHolder holder;
    private Paint paint;
    private Paint paintMouse;
    private Context m_context; //reference to the activity
    private SoundPool soundPool;
    private int mouse_sound = -1;
    private int dead_sound = -1;
    //add sounds
    //direction
    public enum Direction{UP,RIGHT,DOWN,LEFT}
    //start heading direction
    private Direction direction= Direction.RIGHT;
    private int screenWidth;
    private int screenHeight;
    //control pausing between updates
    private long nextFrameTime;
    private final long FPS=10;
    private final long ms=1000;
    private int score;
    private int[] snakeX;
    private int[] snakeY;
    private int snakeLength;
    private int mouseX;
    private int mouseY;
    private int blockSize;
    //size in segments of the area
    private final int numBlockWide=40;
    private int numBlocksHigh;

    public SnakeView(Context context, Point size){
        super(context);
        m_context=context;
        screenWidth=size.x;
        screenHeight=size.y;
        // //Determine the size of each block/place on the game board
        blockSize=screenWidth/numBlockWide;
        // How many blocks of the same size will fit into the height
        numBlocksHigh=screenHeight/blockSize;

        //set sounds up
        //loadSound();
        //initialize the drawing objects
        holder=getHolder();
        paint=new Paint();
        paintMouse=new Paint();

        //if score 200 achievement
        snakeX=new int[200];
        snakeY=new int[200];

        startGame();



    }


    @Override
    public void run() {
        while(playing)//check if is not pause game
        {
            //update 10 times in second
            if(checkForUpdate()){
                updateGame();
                drawGame();
            }
        }

    }
    public void pause(){
        playing=false;
        try {
            thread.join();
        }catch (InterruptedException e){
            //error
        }
    }
    public void resume(){
        playing=true;
        thread=new Thread(this);
        thread.start();
    }
    public void startGame(){
        //snake with just head in the middle f screen
        snakeLength=1;
        snakeX[0]=numBlockWide/2;
        snakeY[0]=numBlocksHigh/2;

        //point to eat(mouse)
        spawnMouse();
        score=0;
        // Setup m_NextFrameTime so an update is triggered immediately
        nextFrameTime=System.currentTimeMillis();
    }
//    public void loadSound(){
//        soundPool=new SoundPool(10, AudioManager.STREAM_MUSIC,0);
//        try{
//            AssetManager manager=m_context.getAssets();
//            AssetFileDescriptor descriptor;
//
//            //prepare sounds in memory
//            descriptor=manager.openFd("");
//            mouse_sound=soundPool.load(descriptor,0);
//            descriptor=manager.openFd("");
//            dead_sound=soundPool.load(descriptor,0);
//
//        }catch(IOException e){
//
//        }
//    }
    public void spawnMouse(){
        Random r=new Random();
        mouseX=r.nextInt(numBlockWide-10)+1;
        mouseY=r.nextInt(numBlocksHigh-1);

    }
    public void eatMouse(){
        //increase length
        snakeLength++;
        spawnMouse();
        score++;
        //m_SoundPool.play(m_get_mouse_sound, 1, 1, 0, 0, 1);
    }
    private void moveSnake(){
        // Move the body
        for(int i=snakeLength;i>0;i--){
            // Start at the back and move it
            // to the position of the segment in front of it
            snakeX[i]=snakeX[i-1];
            snakeY[i]=snakeY[i-1];
            // Exclude the head because
            // the head has nothing in front of it
        }
        // Move the head in the appropriate m_Direction
        switch (direction){
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
    private boolean detectDeath(){
        boolean dead=false;
        //wall colission
        if(snakeX[0]==-1)dead=true;
        if(snakeX[0]>=numBlockWide-10)dead=true;
        if(snakeY[0]==-1)dead=true;
        if(snakeY[0]>=numBlocksHigh)dead=true;

        //body collision
        for(int i=snakeLength-1;i>0;i--){
            if((i>4)&&(snakeX[0]==snakeX[i])&&(snakeY[0]==snakeY[i])){
               dead=true;
            }
        }
        return dead;
    }
    public void updateGame(){
        if(snakeX[0]==mouseX && snakeY[0]==mouseY){
            eatMouse();
        }
        moveSnake();
        if(detectDeath()){
            //m_SoundPool.play(m_dead_sound, 1, 1, 0, 0, 1);

            startGame();
        }
    }
    public void drawGame(){
        if(holder.getSurface().isValid()){
            Paint controlPanel=new Paint();
            controlPanel.setColor(Color.argb(255, 0, 102, 0));
            canvas=holder.lockCanvas();
            //background
            canvas.drawColor(Color.argb(255, 0,0,0));
            // Set the color of the paint to draw the snake and mouse with
            paint.setColor(Color.argb(255, 0, 102, 0));
            // Choose how big the score will be
            paint.setTextSize(30);
            paintMouse.setColor(Color.argb(255,255,255,255));
            paintMouse.setTextSize(100);
            controlPanel.setColor(Color.BLUE);
//            controlPanel.setStrokeWidth(0);
//            controlPanel.setStyle(Paint.Style.STROKE);
//            canvas.drawRect( screenWidth-300, 0, 0, 0, controlPanel);
            canvas.drawRect(screenWidth-300, 0, screenWidth, screenHeight, controlPanel);
            canvas.drawText("Score:" + score, screenWidth-150, 30, paint);

            //draw snake

            for(int i=0;i<snakeLength;i++){
                canvas.drawRect(snakeX[i]*blockSize,(snakeY[i]*blockSize),(snakeX[i]*blockSize)+blockSize,(snakeY[i]*blockSize)+blockSize,paint);

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
            Bitmap bmpP2 = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);
            bmpP1 = Bitmap.createScaledBitmap(bmpP1,300,300,false);
            bmpP2 = Bitmap.createScaledBitmap(bmpP2,20,20,false);



                        canvas.drawBitmap(bmpP1, screenWidth-300,screenHeight-350, null);




            holder.unlockCanvasAndPost(canvas);



        }
    }
    public boolean checkForUpdate(){
        // Are we due to update the frame
        if(nextFrameTime <= System.currentTimeMillis()){
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            nextFrameTime =System.currentTimeMillis() + ms / FPS;

            // Return true so that the update and draw
            // functions are executed
            return true;
        }

        return false;
    }

    @Override public boolean onTouchEvent(MotionEvent motionEvent){
        switch (motionEvent.getAction()& MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_UP:
                if(motionEvent.getX()>=screenWidth/2){

                   switch (direction){
                      case UP:
                          direction= Direction.RIGHT;
                           break;
                      case RIGHT:
                           direction= Direction.DOWN;
                            break;
                        case DOWN:
                           direction= Direction.LEFT;
                           break;
                       case LEFT:
                           direction= Direction.UP;
                           break;
                   }

                }else {

                  switch (direction){
                        case UP:
                           direction= Direction.LEFT;
                            break;
                       case LEFT:
                           direction= Direction.DOWN;
                            break;
                        case DOWN:
                           direction= Direction.RIGHT;
                            break;
                       case RIGHT:
                           direction= Direction.UP;
                            break;
                  }
                }


        }
        return true;
    }
}
