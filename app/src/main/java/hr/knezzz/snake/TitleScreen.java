package hr.knezzz.snake;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import hr.knezzz.snake.game.Game;
import hr.knezzz.snake.game.GameScreen;

/**
 * @author knezzz
 *
 * Title or menu screen.
 */

public class TitleScreen extends Activity{

    private Game game;
    TextView songNameText, artistNameText;
    SharedPreferences userPreferences;
    Button playPause, nextSong, prevSong, musicButton;
    String songName;
    private boolean isMusic = true;
    private boolean startMusic = false;
    private boolean stopMusic = false;


    AudioManager mAudioManager;
    IntentFilter songNameFilter;

    public static final String CMDPAUSE = "pause";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDUPDATE = "appwidgetupdate";
    public static final String CMDNEXT = "next";
    public static final String SERVICECMD = "com.android.music.musicservicecommand";
    public static final String CMDNAME = "command";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.title_screen);

        newGame();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        musicReceiver();

        Intent response = getIntent();

        final Button start = (Button)findViewById(R.id.start);
        final Button options = (Button)findViewById(R.id.options);

        TextView exit = (TextView)findViewById(R.id.titleExit);

        songNameText = (TextView)findViewById(R.id.titleSongName);
        artistNameText = (TextView)findViewById(R.id.titleArtistName);

        if(response != null && response.hasExtra("songName") && response.hasExtra("artistName")){
            Log.d("hr.knezzz.snake.game.TitleScreen", "songName and artistName extras found");
            songNameText.setText(response.getStringExtra("songName"));
            artistNameText.setText(response.getStringExtra("artistName"));
        }

        playPause = (Button)findViewById(R.id.titlePlaySong);
        prevSong = (Button)findViewById(R.id.titlePrevSong);
        nextSong = (Button)findViewById(R.id.titleNextSong);

        musicButton = (Button)findViewById(R.id.music_button);

        final RelativeLayout musicOptions = (RelativeLayout)findViewById(R.id.music_options);
        final CheckBox startMusicBox = (CheckBox)findViewById(R.id.start_music);
        final CheckBox stopMusicBox = (CheckBox)findViewById(R.id.stop_music);

        startMusicBox.setChecked(startMusic);
        stopMusicBox.setChecked(stopMusic);

        musicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(musicOptions.getVisibility() == RelativeLayout.GONE)
                    musicOptions.setVisibility(RelativeLayout.VISIBLE);
                else{
                    SharedPreferences.Editor editor = userPreferences.edit();

                    editor.putBoolean("startMusic", startMusicBox.isChecked());
                    editor.putBoolean("stopMusic", stopMusicBox.isChecked());

                    editor.apply();

                    musicOptions.setVisibility(RelativeLayout.GONE);
                }
            }
        });

        //Checking if user wants to start music automaticly, and if music is already playing.
        if(startMusic && !mAudioManager.isMusicActive())
            startMusic();

        if(isMusic) {
            if (mAudioManager.isMusicActive() || startMusic)
                playPause.setText("||");
            else
                playPause.setText(">");


            playPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mAudioManager.isMusicActive()) {
                        Intent i = new Intent(SERVICECMD);
                        i.putExtra(CMDNAME, CMDPAUSE);
                        TitleScreen.this.sendBroadcast(i);
                        playPause.setText(">");
                    } else {
                        startMusic();
                        playPause.setText("||");
                    }
                }
            });

            nextSong.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mAudioManager.isMusicActive()) {
                        Intent i = new Intent(SERVICECMD);
                        i.putExtra(CMDNAME, CMDNEXT);
                        TitleScreen.this.sendBroadcast(i);
                    }
                }
            });

            prevSong.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mAudioManager.isMusicActive()) {
                        Intent i = new Intent(SERVICECMD);
                        i.putExtra(CMDNAME, CMDPREVIOUS);
                        TitleScreen.this.sendBroadcast(i);
                    }
                }
            });
        }else{
            playPause.setVisibility(LinearLayout.GONE);
            nextSong.setVisibility(LinearLayout.GONE);
            prevSong.setVisibility(LinearLayout.GONE);

            songNameText.setVisibility(LinearLayout.GONE);
            artistNameText.setVisibility(LinearLayout.GONE);
        }

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unregisterReceiver(mReceiver);

                if(stopMusic) {
                    Intent i = new Intent(SERVICECMD);
                    i.putExtra(CMDNAME, CMDPAUSE);
                    TitleScreen.this.sendBroadcast(i);
                }

                finish();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(TitleScreen.this, GameScreen.class);
                if(songNameText != null && artistNameText != null){
                    Log.i("hr.knezzz.snake.game.TitleScreen", "Sending song - " + songNameText.getText() + " - " + artistNameText.getText());
                    i.putExtra("songName", songNameText.getText());
                    i.putExtra("artistName", artistNameText.getText());
                }
                startActivity(i);
                unregisterReceiver(mReceiver);
                TitleScreen.this.finish();
            }
        });

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(TitleScreen.this, OptionsScreen.class), 1);
           //     TitleScreen.this.finish();
                game.snake.stopped = false;
            }
        });

        FrameLayout frameView = (FrameLayout) findViewById(R.id.gameFrame);
        frameView.addView(game);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                int color = data.getIntExtra("color", 0);
                int speed = data.getIntExtra("speed", 1);
                boolean walls = data.getBooleanExtra("walls", false);
                boolean glitch = data.getBooleanExtra("glitch", false);
                Log.d("Got new options", "color: "+ color + " speed: " + speed +" walls: "+walls);

                changeValues(color, speed, walls, glitch);
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
                Log.e("Something went wrong", "[NO DATA]");
            }
        }
    }

    //Changing values (since title snake changes @ same time when settings screen is closed without restarting)
    public void changeValues(int color, int speed, boolean walls, boolean glitch){
        switch(color){
            case 0: color = Color.DKGRAY; break;
            case 1: color = Color.LTGRAY; break;
            case 2: color = Color.BLUE; break;
            case 3: color = Color.GREEN; break;
            case 4: color = Color.YELLOW; break;
        }

        switch(speed){
            case 0: speed = 15; break;
            case 1: speed = 30; break;
            case 2: speed = 45; break;
        }

        game.color = color;
        game.frameRate = (speed / 2) + 1;
        game.wall = walls;
        game.isGlitch = glitch;
    }

    public void gameOver(){
        game.setup();
        game.invalidate();
    }

    public void newGame(){
        userPreferences = getSharedPreferences("settings", 0);
        boolean walls = userPreferences.getBoolean("walls", false);
        startMusic = userPreferences.getBoolean("startMusic", false);
        stopMusic = userPreferences.getBoolean("stopMusic", false);

        int snakeColor = userPreferences.getInt("color", 0);

        switch(snakeColor){
            case 0: snakeColor = Color.DKGRAY; break;
            case 1: snakeColor = Color.LTGRAY; break;
            case 2: snakeColor = Color.BLUE; break;
            case 3: snakeColor = Color.GREEN; break;
            case 4: snakeColor = Color.YELLOW; break;
        }

        int speed = userPreferences.getInt("speed", 1);

      //  TextView highScoreTV = (TextView)findViewById(R.id.highScore);

     //   int highScore = userPreferences.getInt("highScore", 0);
  //          highScoreTV.setText(""+highScore);

     //   TextView highScoreTitle = (TextView)findViewById(R.id.highScoreTitle);

        boolean glitch = userPreferences.getBoolean("glitch", false);

        game = new Game(this, null, null, walls, glitch, snakeColor, speed,
                1, 0, true);
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

            if(isMusic) {
                if (!playing)
                    songName = "No Music";
                else {
                    songName = artist + " - " + track;

                    songNameText.setText(track);
                    artistNameText.setText(artist);
                }
                Log.d("hr.knezzz.snake.game.GameScreen", "Song - " + songName);
            }
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
}
