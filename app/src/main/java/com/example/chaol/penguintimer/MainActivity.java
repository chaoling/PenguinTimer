package com.example.chaol.penguintimer;

import android.content.Context;
import android.media.AudioManager;
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
    private boolean mIsTicking;
    private CountDownTimer mTimer;
    private MediaPlayer mPlayer;
    private AudioManager mAudioManager;
    private long mTimeLeft; //time in milliseconds until done
    private int mLoopPos; //loop to location for media player

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                // The AUDIOFOCUS_LOSS_TRANSIENT case means that we've lost audio focus for a
                // short amount of time. The AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK case means that
                // our app is allowed to continue playing sound but at a lower volume. We'll treat
                // both cases the same way because our app is playing short sound files.

                // Pause playback and reset player to the start of the file. That way, we can
                // play the word from the beginning when we resume playback.
                mPlayer.pause();
                mPlayer.seekTo(mLoopPos);
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // The AUDIOFOCUS_GAIN case means we have regained focus and can resume playback.
                mPlayer.start();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // The AUDIOFOCUS_LOSS case means we've lost audio focus and
                // Stop playback and clean up resources
                releaseMediaPlayer();
            }
        }
    };

    /**
     * This listener gets triggered when the {@link MediaPlayer} has completed
     * playing the audio file.
     */
    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            // Now that the sound file has finished playing, release the media player resources.
            releaseMediaPlayer();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.chaol.penguintimer.R.layout.activity_main);
        Log.v("Activity Life Cycle: ", "onCreate");
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            Log.v("Activity Life Cycle: ", "retriving activity state...");
            //mIsTicking = savedInstanceState.getBoolean("wasTimerTicking", false);
            mLoopPos = savedInstanceState.getInt("mediaLoop", 0);
            mTimeLeft = savedInstanceState.getLong("timeLeft", 0L);
            mIsTicking = mTimeLeft > 0L;
            Log.v("Activity", "isTicking: " + (mIsTicking ? "true" : "false"));
            Log.v("Activity", "timeLeft:" + mTimeLeft);
        }
        // Create and setup the {@link AudioManager} to request audio focus
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (mTv == null) {
            mTv = (TextView) findViewById(R.id.timer_textview);
        }
        if (mSeekBar == null) {
            mSeekBar = (SeekBar) findViewById(R.id.timer_setting_bar);
            mSeekBar.setMax(600);
            mSeekBar.setProgress((int) mTimeLeft / 1000);
            mSeekBar.setEnabled(!mIsTicking);
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
        }

        if (mBtn == null) {
            mBtn = (Button) findViewById(R.id.controll_btn);
            mBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v("MainActivity", "Btn Clicked");
                    if (!mIsTicking) {
                        mBtn.setText(R.string.stop);
                        mTimeLeft = (mSeekBar.getProgress() * 1000 + 100);
                        startTimer();

                    } else {
                        stopTimer();
                        resetView();
                    }
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v("Activity Life Cycle: ", "onPause");
        releaseMediaPlayer();
        pauseTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("Activity Life Cycle:", "onResume");

        if (mTimeLeft > 0L) {
            resumeTimer();
        } else {
            stopTimer();
            resetView();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v("Activity Life Cycle: ", "onSavedInstanceState");
        //outState.putBoolean("wasTimerTicking", mIsTicking);
        if (mPlayer != null) {
            Log.v("mPlayer", "current media player position is:" + mLoopPos);
            mLoopPos = mPlayer.getCurrentPosition();
        }
        outState.putInt("mediaLoop", mLoopPos);
        outState.putLong("timeLeft", mTimeLeft);
    }

    private void pauseTimer() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
            mLoopPos = mPlayer.getCurrentPosition();
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    private void resumeTimer() {
        Log.v("Timer Activity:", "resume Timer");
        mSeekBar.setProgress((int) mTimeLeft / 1000);
        mSeekBar.setEnabled(!mIsTicking);
        mBtn.setText((mIsTicking ? "STOP" : "GO!"));
        Log.v("Timer Activity: ", "start timer at: " + mTimeLeft);
        startTimer();
    }

    private void resetView() {
        Log.v("View Activity:", "resetView");
        mBtn.setText(R.string.start);
        mTv.setText(R.string.initTime);
        mSeekBar.setEnabled(true);
        mSeekBar.setProgress(0);
    }

    private void stopTimer() {
        Log.v("Timer Activity:", "stopTimer");
        if (mTimer != null) {
            mTimer.cancel();
        }
        mIsTicking = false;
        mTimeLeft = 0L;
    }

    private void startTimer() {
        Log.v("Timer Activity", "startTimer");
        if (mTimeLeft > 0L) {
            mTimer = new CountDownTimer(mTimeLeft, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    mTimeLeft = millisUntilFinished;
                    int secondsUntilFinished = (int) (millisUntilFinished) / 1000;
                    Log.v("startTimer Activity", "tick: " + secondsUntilFinished);
                    updateTimerView(secondsUntilFinished);
                }

                @Override
                public void onFinish() {
                    Log.v("startTimer Activity", "timer paused/finished");
                    updateTimerView(0);
                    mTimeLeft = 0L;
                    mIsTicking = false;
                    playFinishedSound();
                    resetView();
                }
            };
            mTimer.start();
            mIsTicking = true;
            mSeekBar.setEnabled(false);
        }
    }

    private void playFinishedSound() {
        Log.v("mPlayer", "playFinishedSound");
        releaseMediaPlayer();

        // Request audio focus so in order to play the audio file. The app needs to play a
        // short audio file, so we will request audio focus with a short amount of time
        // with AUDIOFOCUS_GAIN_TRANSIENT.
        int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mPlayer = MediaPlayer.create(this, R.raw.doorbuzzer);
            mPlayer.start();
            mPlayer.setOnCompletionListener(mCompletionListener);
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

    private void releaseMediaPlayer() {
        // If the media player is not null, then it may be currently playing a sound.
        if (mPlayer != null) {
            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            mPlayer.release();
            mLoopPos = 0;

            // Set the media player back to null. For our code, we've decided that
            // setting the media player to null is an easy way to tell that the media player
            // is not configured to play an audio file at the moment.
            mPlayer = null;

            // Regardless of whether or not we were granted audio focus, abandon it. This also
            // unregisters the AudioFocusChangeListener so we don't get anymore callbacks.
            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
    }
}
