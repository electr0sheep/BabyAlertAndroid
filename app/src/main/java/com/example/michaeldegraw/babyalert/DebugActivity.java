// resolved with android:configChanges in manifest : MEDIAPLAYER STOPS WHEN SWITCHED TO LANDSCAPE

package com.example.michaeldegraw.babyalert;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class DebugActivity extends Activity {

    final Context context = this;
    AudioManager.OnAudioFocusChangeListener audioListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {

        }
    };
    // NOTE TO SELF:
    // I BELIEVE THE REASON THE ALARM SOUND IS SO WONKY IS THAT
    // THE MediaPlayer OBJECT IS GLOBAL, BUT IT'S STATE IS CONSTANTLY
    // BEING ALTERED. ONLY START IT AND STOP IT ONCE
    private MediaPlayer player;
    private AudioManager audio;
    private Vibrator vib;
    private int currentVolume;
    private Button alarm;
    private Button alarmStop;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        player = new MediaPlayer();
        alarm = (Button) findViewById(R.id.btnAlarm);
        alarmStop = (Button) findViewById(R.id.btnAlarmStop);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // Temporary string variable to make code more readable
        String alarmString = sharedPref.getString("alarm_tone", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString());
        try {
            player.setDataSource(context, Uri.parse(alarmString));
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
        } catch (IOException e) {
            Log.e("MediaPlayerError", "Error preparing media player in onCreate method");
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (player.isPlaying()) {
            simulateStopAlarm();
            alarm.setVisibility(Button.VISIBLE);
            alarmStop.setVisibility(Button.INVISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_debug, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void alarmButtonClickHandler(View view) {
        // Play sound
        simulateAlarm();

        // Swap buttons
        alarmStop.setVisibility(Button.VISIBLE);
        alarm.setVisibility(Button.INVISIBLE);
    }

    public void alarmStopButtonClickHandler(View view) {
        simulateStopAlarm();

        // Swap buttons
        alarmStop.setVisibility(Button.INVISIBLE);
        alarm.setVisibility(Button.VISIBLE);
    }


    // THIS METHOD NEEDS TO PLAY THE ALARM, NO MATTER WHAT
    //  TEST SCENARIOS INCLUDE:
    //   IF THE USER IS ON THE PHONE
    //   resolved with setStreamVolume for music : IF THE USER IS LISTENING TO MUSIC
    //   resolved with setStreamVolume for alarm : IF THE ALARM VOLUME HAS BEEN SET TO ZERO, OR LOW
    //   IF THE USER HAS HEADPHONES ON
    private void simulateAlarm() {
        // Set inverval for vibrate
        long[] times = {0, 1000};

        // Store current system volumes
        currentVolume = audio.getStreamVolume(AudioManager.STREAM_ALARM);

        // Request audio focus
        audio.requestAudioFocus(audioListener, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

        // Change the alarm volume to be maximum
        audio.setStreamVolume(AudioManager.STREAM_ALARM, audio.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);

        // Prepare and start the alarm sound
        try {
            player.prepare();
        } catch (IOException e) {
            Log.e("MediaPlayerError", "Error preparing player in simulateAlarm");
        }
        player.start();

        // If the system has a vibrate function, use it
        if (vib.hasVibrator()) {
            vib.vibrate(times, 0);
        }
    }

    private void simulateStopAlarm() {
        // Stop sound if a sound is playing
        if (player.isPlaying()) {
            player.stop();
        }

        // Stop the vibrate
        vib.cancel();

        // Return alarm volume to what it was previously set to
        audio.setStreamVolume(AudioManager.STREAM_ALARM, currentVolume, 0);

        boolean audioFocusBool = sharedPref.getBoolean("alarm_resume_playback", true);
        if (audioFocusBool) {
            audio.abandonAudioFocus(audioListener);
        }
    }


    protected Uri getAlarmSound() {
        String alarmString = sharedPref.getString("alarm_tone", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString());
        return Uri.parse(alarmString);
    }
}