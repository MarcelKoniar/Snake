package com.example.marcel.snake;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.TextView;


public class SnakeActivity extends AppCompatActivity {
    SnakeView snakeView;
    Vibrator mVibrator;
    SharedPreferences mySharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display =getWindowManager().getDefaultDisplay();
        Point size=new Point();
      boolean isChecked = getIntent().getBooleanExtra("switch", true);


        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        Log.wtf("","score is" +isChecked);
        display.getSize(size);
        snakeView=new SnakeView(this, size,isChecked);

    setContentView(snakeView);

       SnakeView.getScore();


}


    //start thread
    @Override
    protected  void onResume(){
        super.onResume();
        snakeView.resume();
//        Log.wtf("","score is "+snakeView.getScore());
//        int score= mySharedPref.getInt("score", 1);
       // snakeView.vibrateDevice(getApplicationContext());
//        Log.wtf("","score is "+score);
    }
    //pause
    @Override
    protected  void onPause(){
        super.onPause();
        snakeView.pause();
        Log.wtf("","score is "+snakeView.getScore());
        //tu dat intent a poslat do main
        Intent intent = new Intent();
        intent.putExtra("score", snakeView.getScore());
        Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(100);

    }




}
