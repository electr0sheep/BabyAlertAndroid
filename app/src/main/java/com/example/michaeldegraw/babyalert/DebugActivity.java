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
import android.widget.Toast;

import java.io.IOException;

public class DebugActivity extends Activity {

    final Context context = this;
    // NOTE TO SELF:
    // I BELIEVE THE REASON THE ALARM SOUND IS SO WONKY IS THAT
    // THE MediaPlayer OBJECT IS GLOBAL, BUT IT'S STATE IS CONSTANTLY
    // BEING ALTERED. ONLY START IT AND STOP IT ONCE
    private MediaPlayer player;
    private AudioManager audio;
    private Vibrator vib;
    private int[] currentVolumes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        currentVolumes = new int[7];
        player = new MediaPlayer();
        try {
            player.setDataSource(context, getAlarmSound());
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.prepare();
        } catch (IOException e) {
            // this may be the worst error message I've ever seen...
            Log.e("Error...", "Check code...");
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(context, "Arrived", Toast.LENGTH_SHORT).show();
                //finish();
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        player.stop();
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
        // Set variables for alarm start and stop buttons
        Button alarm = (Button) findViewById(R.id.btnAlarm);
        Button alarmStop = (Button) findViewById(R.id.btnAlarmStop);

        // Store current system volumes
        currentVolumes[0] = audio.getStreamVolume(AudioManager.STREAM_ALARM);
        currentVolumes[1] = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        currentVolumes[2] = audio.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        currentVolumes[3] = audio.getStreamVolume(AudioManager.STREAM_RING);
        currentVolumes[4] = audio.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        currentVolumes[5] = audio.getStreamVolume(AudioManager.STREAM_SYSTEM);
        currentVolumes[6] = audio.getStreamVolume(AudioManager.STREAM_DTMF);

        // Modify system volumes
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        audio.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, 0);
        audio.setStreamVolume(AudioManager.STREAM_ALARM, audio.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);

        // Play sound
        if (player.isPlaying()) {
            Toast.makeText(this, "Quit button mashing", Toast.LENGTH_SHORT).show();
        } else {
            playAlarmSound();
        }

        // Swap buttons
        alarmStop.setVisibility(View.VISIBLE);
        alarm.setVisibility(View.INVISIBLE);
    }

    public void alarmStopButtonClickHandler(View view) {
        // Set variables for alarm start and stop buttons
        Button alarm = (Button) findViewById(R.id.btnAlarm);
        Button alarmStop = (Button) findViewById(R.id.btnAlarmStop);

        // Stop sound and stop vibrate
        if (player.isPlaying()) {
            player.pause();
            vib.cancel();
        } else {
            Toast.makeText(this, "I told you, quit button mashing!", Toast.LENGTH_SHORT).show();
        }

        // Return system volumes to previoius
        audio.setStreamVolume(AudioManager.STREAM_ALARM, currentVolumes[0], 0);
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolumes[1], 0);
        audio.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currentVolumes[2], 0);
        audio.setStreamVolume(AudioManager.STREAM_RING, currentVolumes[3], 0);
        audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, currentVolumes[4], 0);
        audio.setStreamVolume(AudioManager.STREAM_SYSTEM, currentVolumes[5], 0);
        audio.setStreamVolume(AudioManager.STREAM_DTMF, currentVolumes[6], 0);

        // Swap buttons
        alarmStop.setVisibility(View.INVISIBLE);
        alarm.setVisibility(View.VISIBLE);
    }


    // THIS METHOD NEEDS TO PLAY THE ALARM, NO MATTER WHAT
    //  TEST SCENARIOS INCLUDE:
    //   IF THE USER IS ON THE PHONE
    //   resolved with setStreamVolume for music : IF THE USER IS LISTENING TO MUSIC
    //   resolved with setStreamVolume for alarm : IF THE ALARM VOLUME HAS BEEN SET TO ZERO, OR LOW
    //   IF THE USER HAS HEADPHONES ON
    private void playAlarmSound() {
        long[] times = {0, 1000};
        player.start();
        if (vib.hasVibrator()) {
            vib.vibrate(times, 0);
        }
    }


    private Uri getAlarmSound() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String alarmString = sharedPref.getString("alarm_tone", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString());
        return Uri.parse(alarmString);
    }
}
