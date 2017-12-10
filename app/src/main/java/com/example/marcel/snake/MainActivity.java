package com.example.marcel.snake;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button play;
    TextView score;
    Switch sounds;
    Intent playIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         playIntent = new Intent(this, SnakeActivity.class);
        //prozatim skore
//        Intent intent=getIntent();
//       int Snakescore =intent.getIntExtra("score",0);
//        score= (TextView)findViewById(R.id.scoreTxtView);
//        score.setText(Snakescore);
        play=(Button)findViewById(R.id.playBtn);
       // sounds= (Switch)findViewById(R.id.switchSounds);

       // score.onc
//        sounds=
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {




//                playIntent.putExtra("switch",sounds.isChecked());
                startActivity(playIntent);

                           }
        });

    }

//    public void onRadioButtonClicked(View view) {
//        // Is the button now checked?
//        boolean checked = ((RadioButton) view).isChecked();
//
//        // Check which radio button was clicked
//        switch(view.getId()) {
//            case R.id.radio_pirates:
//                if (checked)
//                    // Pirates are the best
//                    break;
//            case R.id.radio_ninjas:
//                if (checked)
//                    // Ninjas rule
//                    break;
//        }
//    }

}
