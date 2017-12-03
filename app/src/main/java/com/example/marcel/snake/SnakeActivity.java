package com.example.marcel.snake;

import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.TextView;


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


        Log.wtf("","score is"+1);

    }

    //start thread
    @Override
    protected  void onResume(){
        super.onResume();
        snakeView.resume();
        Log.wtf("","score is"+1);
    }
    //pause
    @Override
    protected  void onPause(){
        super.onPause();
        snakeView.pause();
        Log.wtf("","score is"+1);
        //tu dat intent a poslat do main
       /* Intent intent = new Intent();
        intent.putExtra("score", snakeView.getScore());*/
    }


}
