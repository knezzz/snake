package hr.knezzz.snake.game;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.TextView;

import hr.knezzz.snake.OptionsScreen;
import hr.knezzz.snake.R;

/**
 * Author: Luka Knezic
 * Description: Activity that is called as pause menu in game.
 * Assigning listeners to buttons and handling actions.
 */

public class GamePause extends Activity {
    //Intent for response
    Intent responseIntent;
    IntentFilter songNameFilter;
    TextView songNameText;
    String songName;
    private final static String LOG = "hr.knezzz.snake.game.GamePause";

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideSystemUi();
        }
    };

    AudioManager mAudioManager;
  //  public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";
    public static final String SERVICECMD = "com.android.music.musicservicecommand";
    public static final String CMDNAME = "command";
    //  public static final String CMDSTOP = "stop";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.game_pause);

        songNameText = (TextView)findViewById(R.id.songName);

        Button exit = (Button)findViewById(R.id.pause_exit);
        Button resume = (Button)findViewById(R.id.pause_continue);
        Button options = (Button)findViewById(R.id.pause_change_options);
        Button again = (Button)findViewById(R.id.pause_restart);

        Button playPause = (Button)findViewById(R.id.pause_play_pause);
        Button nextSong = (Button)findViewById(R.id.pause_song_next);
        Button prevSong = (Button)findViewById(R.id.pause_song_prev);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                exitPause(true);
            }
        });

        resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitPause(false);
            }
        });

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optionsPause();
            }
        });

        again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                againPause();
            }
        });

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAudioManager.isMusicActive()) {
                    Intent i = new Intent(SERVICECMD);
                    i.putExtra(CMDNAME, CMDPAUSE);
                    GamePause.this.sendBroadcast(i);
                }else
                    startMusic();
            }
        });

        nextSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAudioManager.isMusicActive()) {
                    Intent i = new Intent(SERVICECMD);
                    i.putExtra(CMDNAME , CMDNEXT);
                    GamePause.this.sendBroadcast(i);
                }
            }
        });

        prevSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAudioManager.isMusicActive()) {
                    Intent i = new Intent(SERVICECMD);
                    i.putExtra(CMDNAME , CMDPREVIOUS);
                    GamePause.this.sendBroadcast(i);
                }
            }
        });

        songName = getIntent().getStringExtra("songName");

        songNameText.setText(songName);

        musicReceiver();

        if(Build.VERSION.SDK_INT >= 14 && !ViewConfiguration.get(this).hasPermanentMenuKey()) {
            hideSystemUi();

            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if (visibility == 0) {
                        mHideHandler.postDelayed(mHideRunnable, 2000);
                    }
                }
            });
        }
    }

    public void musicReceiver(){
        songNameFilter = new IntentFilter();
        songNameFilter.addAction("com.android.music.metachanged");
        songNameFilter.addAction("com.android.music.playstatechanged");
        songNameFilter.addAction("com.android.music.playbackcomplete");
        songNameFilter.addAction("com.android.music.queuechanged");

        songNameFilter.addAction("com.htc.music.metachanged");
        songNameFilter.addAction("fm.last.android.metachanged");
        songNameFilter.addAction("com.sec.android.app.music.metachanged");
        songNameFilter.addAction("com.nullsoft.winamp.metachanged");
        songNameFilter.addAction("com.amazon.mp3.metachanged");
        songNameFilter.addAction("com.miui.player.metachanged");
        songNameFilter.addAction("com.real.IMP.metachanged");
        songNameFilter.addAction("com.sonyericsson.music.metachanged");
        songNameFilter.addAction("com.rdio.android.metachanged");
        songNameFilter.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
        songNameFilter.addAction("com.andrew.apollo.metachanged");

        registerReceiver(mReceiver, songNameFilter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            Log.v("tag ", action + " / " + cmd);
            String artist = intent.getStringExtra("artist");
            String album = intent.getStringExtra("album");
            String track = intent.getStringExtra("track");
            boolean playing = intent.getBooleanExtra("playing", false);

            //TODO: Not working. Find better way to recognise if music is playing.
            //Tracking if music isk playing. If so song name will be shown.
            if(!playing)
                songName = "No Music";
            else
                songName = artist+" - "+track;

            Log.d("hr.knezzz.snake.game.GameScreen","Song - "+songName);

            songNameText.setText(songName);
        }
    };

    private void startMusic(){
        long eventtime = SystemClock.uptimeMillis();
        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent downEvent = new KeyEvent(eventtime, eventtime,
                KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        sendOrderedBroadcast(downIntent, null);

        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent upEvent = new KeyEvent(eventtime, eventtime,
                KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
        sendOrderedBroadcast(upIntent, null);
    }

    //Class for exiting the menu and continuation of game.
    //Sending intent with "exit" extra.
    public void exitPause(boolean action){
        if(responseIntent == null)
            responseIntent = new Intent();//Making new intent for saving extras

        responseIntent.putExtra("exit", action);//Adding exit extra

        Log.d(LOG, "Sending intent: " + responseIntent.getExtras());//Show what extras are saved in intent.

        //Send the data.
        setResult(RESULT_OK, responseIntent);
        unregisterReceiver(mReceiver);
        finish();
    }

    public void optionsPause(){
        startActivityForResult(new Intent(this, OptionsScreen.class), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                int color = data.getIntExtra("color", 0);
                int speed = data.getIntExtra("speed", 1);
                boolean glitch = data.getBooleanExtra("glitch", false);
                boolean walls = data.getBooleanExtra("walls", true);
                Log.d(LOG, "Got new options -- color: "+ color + " speed: " + speed +" walls: "+walls);

                changeValues(color, speed, walls, glitch);
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
                Log.e(LOG, "Something went wrong [NO DATA]");
            }
        }
    }

    public void changeValues(int color, int speed, boolean walls, boolean glitch){
        switch(color){
            case 0: color = Color.DKGRAY; break;
            case 1: color = Color.LTGRAY; break;
            case 2: color = Color.BLUE; break;
            case 3: color = Color.GREEN; break;
            case 4: color = Color.YELLOW; break;
        }

        responseIntent = new Intent();

        responseIntent.putExtra("options", true);

        responseIntent.putExtra("glitch", glitch);
        responseIntent.putExtra("color", color);
        responseIntent.putExtra("speed", speed);
        responseIntent.putExtra("wall", walls);
    }

    public void againPause(){
        if(responseIntent == null)
            responseIntent = new Intent();

        responseIntent.putExtra("again", true);

        Log.d(LOG, "Sending intent: "+responseIntent.getExtras());

        setResult(RESULT_OK, responseIntent);
        unregisterReceiver(mReceiver);
        finish();
    }

    @SuppressLint("InlinedApi")
    public void hideSystemUi(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        |View.SYSTEM_UI_FLAG_FULLSCREEN
                        |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Rect dialogBounds = new Rect();
        getWindow().getDecorView().getHitRect(dialogBounds);

        if (!dialogBounds.contains((int) ev.getX(), (int) ev.getY())) {
            Log.i("Pause", "Outside of window");

            return false;
        }

        return super.dispatchTouchEvent(ev);
    }
}
