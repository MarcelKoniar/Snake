package com.example.marcel.snake;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;



public class SnakeActivity extends AppCompatActivity {
    SnakeView snakeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display =getWindowManager().getDefaultDisplay();

        Point size=new Point();
        display.getSize(size);

        snakeView=new SnakeView(this, size);

        setContentView(snakeView);
    }

    //start thread
    @Override
    protected  void onResume(){
        super.onResume();
        snakeView.resume();
    }
    //pause
    @Override
    protected  void onPause(){
        super.onPause();
        snakeView.pause();
    }

}
