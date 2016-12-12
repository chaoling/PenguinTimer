package com.example.chaol.penguintimer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private SeekBar mSeekBar;
    private TextView mTv;
    private Button mBtn;
    private boolean isTicking;
    private CountDownTimer mTimer;
    private MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.chaol.penguintimer.R.layout.activity_main);

        mPlayer = MediaPlayer.create(this, R.raw.doorbuzzer);
        mTv = (TextView) findViewById(R.id.timer_textview);
        mSeekBar = (SeekBar) findViewById(R.id.timer_setting_bar);
        mSeekBar.setMax(600);
        mSeekBar.setProgress(0);
        mSeekBar.setEnabled(true);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                updateTimerView(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //TODO
                Log.v("ChangedListener", "onStartTracking");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //TODO
                Log.v("ChangedListener", "onStopTracking");
            }
        });

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.v("MediaPlayer State:", "Completed");
            }
        });

        mBtn = (Button) findViewById(R.id.controll_btn);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("MainActivity", "Btn Clicked");
                if (!isTicking) {
                    mBtn.setText("STOP");
                    startTimer();

                } else {
                    stopTimer();
                    resetView();
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseTimer();
    }

    private void pauseTimer() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
        if (mTimer != null && isTicking) {
            mTimer.cancel();
        }
    }

    private void resetView() {
        mBtn.setText("GO!");
        mTv.setText("00:00");
        mSeekBar.setEnabled(true);
        mSeekBar.setProgress(0);
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        isTicking = false;
    }

    private void startTimer() {
        if (!isTicking) {
            mTimer = new CountDownTimer((mSeekBar.getProgress() * 1000 + 100), 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    int secondsUntilFinished = (int) (millisUntilFinished) / 1000;
                    Log.v("startTimer", "tick: " + secondsUntilFinished);
                    updateTimerView(secondsUntilFinished);
                }

                @Override
                public void onFinish() {
                    Log.v("startTimer", "timer paused/finished");
                    updateTimerView(0);
                    playFinishedSound();
                    resetView();
                    isTicking = false;
                    /*
                    if (mSeekBar.getProgress() == 0) {
                        playFinishedSound();
                    }
                    */
                }
            };
            mTimer.start();
            isTicking = true;
            mSeekBar.setEnabled(!isTicking);
        }
    }

    private void playFinishedSound() {
        if (mPlayer != null && !mPlayer.isPlaying()) {
            mPlayer.start();
        }
    }

    private void updateTimerView(int progress) {
        mSeekBar.setProgress(progress);
        int minutes = (progress / 60);
        int seconds = progress - minutes * 60;
        String txtMinutes = String.valueOf(minutes);
        String txtSeconds = String.valueOf(seconds);
        txtMinutes = txtMinutes.length() == 2 ? txtMinutes : "0" + txtMinutes;
        txtSeconds = txtSeconds.length() == 2 ? txtSeconds : "0" + txtSeconds;
        mTv.setText(txtMinutes + ":" + txtSeconds);
    }
}
