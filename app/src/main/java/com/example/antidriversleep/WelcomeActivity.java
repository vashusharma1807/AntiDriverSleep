package com.example.antidriversleep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

public class WelcomeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private Button startButton , play;
    private SeekBar seekBar ;
    private Spinner spinner ;
    private static final String[] items = new String[]{"Sound 1", "Sound 2", "Sound 3"};
    private MediaPlayer mp ;
    private int SPLASH_TIME_OUT= 6000;
    private String sound ;
    private int time ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        startButton=(Button) findViewById(R.id.start_button);
        seekBar=(SeekBar) findViewById(R.id.seekBar);
        spinner=(Spinner) findViewById(R.id.spinner1);
        play =(Button) findViewById(R.id.play);

        time = seekBar.getProgress();

        this.setVolumeControlStream(1000000);

        seekBar.setMax(5);
        seekBar.setKeyProgressIncrement(1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(WelcomeActivity.this, android.R.layout.simple_spinner_item , items);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(WelcomeActivity.this);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent startIntent = new Intent(WelcomeActivity.this,CameraActivity.class);
                startIntent.putExtra("Tune",sound);
                startIntent.putExtra("Time",time);
                startActivity(startIntent);
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(mp!=null)
                    mp.pause();

                if (sound.equals("Sound2")) {
                    mp = MediaPlayer.create(WelcomeActivity.this, R.raw.expert_jatt);

                }
                else if (sound.equals("Sound3")) {
                    mp = MediaPlayer.create(WelcomeActivity.this, R.raw.shiv);

                }
                else
                    mp = MediaPlayer.create(WelcomeActivity.this , R.raw.friends_theme_song);


                mp.start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mp.pause();

                    }
                },SPLASH_TIME_OUT);

            }
        });


    }

    @Override
    protected void onStop() {
        super.onStop();
        mp.release();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

        switch (position) {
            case 0:
                sound="Sound1";
                if (mp!=null)
                    mp.pause();
                break;
            case 1:
                sound="Sound2";
                if (mp!=null)
                    mp.pause();
                break;
            case 2:
                sound="Sound3";
                if (mp!=null)
                    mp.pause();
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }
}
