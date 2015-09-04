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
    private MediaPlayer player;
    private int currentAlarmVolume;
    private int currentMusicVolume;
    private int currentTalkVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
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
        Button alarm = (Button) findViewById(R.id.btnAlarm);
        Button alarmStop = (Button) findViewById(R.id.btnAlarmStop);

        Toast.makeText(this, "Playing alarm...", Toast.LENGTH_SHORT).show();
        play(this, getAlarmSound());
        alarmStop.setVisibility(View.VISIBLE);
        alarm.setVisibility(View.INVISIBLE);
    }

    public void alarmStopButtonClickHandler(View view) {
        Button alarm = (Button) findViewById(R.id.btnAlarm);
        Button alarmStop = (Button) findViewById(R.id.btnAlarmStop);
        final AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        final Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        Toast.makeText(this, "Stopping alarm...", Toast.LENGTH_SHORT).show();
        player.stop();
        player.release();
        vib.cancel();
        audio.setStreamVolume(AudioManager.STREAM_ALARM, currentAlarmVolume, 0);
        //audio.setStreamVolume(AudioManager.STREAM_MUSIC, currentMusicVolume, 0);
        //audio.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currentTalkVolume, 0);
        alarmStop.setVisibility(View.INVISIBLE);
        alarm.setVisibility(View.VISIBLE);
    }


    // THIS FUNCTIION NEEDS TO PLAY THE ALARM, NO MATTER WHAT
    //  TEST SCENARIOS INCLUDE:
    //   IF THE USER IS ON THE PHONE
    //   resolved with setStreamVolume for music IF THE USER IS LISTENING TO MUSIC
    //   resolved with setStreamVolume for alarm IF THE ALARM VOLUME HAS BEEN SET TO ZERO, OR LOW
    //   IF THE USER HAS HEADPHONES ON
    private void play(Context context, Uri alert) {
        player = new MediaPlayer();
        long[] times = {0, 1000};
        try {
            player.setDataSource(context, alert);
            final AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            final Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            currentAlarmVolume = audio.getStreamVolume(AudioManager.STREAM_ALARM);
            //currentMusicVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            //currentTalkVolume = audio.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            //audio.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            //audio.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, 0);
            audio.setStreamVolume(AudioManager.STREAM_ALARM, audio.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
            Log.d("HeyWhatsUp", Integer.toString(audio.getStreamMaxVolume(AudioManager.STREAM_ALARM)));
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.prepare();
            player.start();
            if (vib.hasVibrator()) {
                vib.vibrate(times, 0);
            }
        } catch (IOException e) {
            // this may be the worst error message I've ever seen...
            Log.e("Error...", "Check code...");
        }
    }

    private Uri getAlarmSound() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Uri alertSound;
        String alarmString = sharedPref.getString("alarm_tone", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString());
        alertSound = Uri.parse(alarmString);
        return alertSound;
    }
}
