package hr.knezzz.snake.game;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import hr.knezzz.snake.R;
import hr.knezzz.snake.TitleScreen;

public class GameScreen extends Activity {

    private Game game;
    private Activity mActivity;
    SharedPreferences userPreferences;
    protected Button LeftUp, RightDown;
    IntentFilter songNameFilter;
    String songName = "No Music";
    public static final String SONG_NAME_ERROR = " Couldn't get song name ";
    private boolean isGamePause = false;
    AudioManager manager;
    TextView songNameText, artistNameText, songState;

    private boolean newWall, newGlitch;
    private int newSpeed;

    // Initialize Game Screen
    @Override
    public void onCreate(Bundle savedInstanceState) {

        // Set Theme, Controls Mode, View Mode & Speed According to Settings
        // Speed Setting is Stored in a Different File Because It Should Not Be
        // Synced Across Devices
        userPreferences = getSharedPreferences("settings", 0);
        boolean glitch = userPreferences.getBoolean("glitch", false);
        boolean walls = userPreferences.getBoolean("walls", true);

        int snakeColor = userPreferences.getInt("color", 0);

        switch(snakeColor){
            case 0: snakeColor = Color.DKGRAY; break;
            case 1: snakeColor = Color.LTGRAY; break;
            case 2: snakeColor = Color.BLUE; break;
            case 3: snakeColor = Color.GREEN; break;
            case 4: snakeColor = Color.YELLOW; break;
        }

        int speed = userPreferences.getInt("speed", 1);

        // Create Game View & Add Handler to Current Activity
        super.onCreate(savedInstanceState);
        musicReceiver();
        /*
         * if(snakeOriented) setContentView(R.layout.game_2arrow); else
		 * setContentView(R.layout.game_4arrow);
		 */
        setContentView(R.layout.game);

        mActivity = this;

        songNameText = (TextView)findViewById(R.id.songName);
        artistNameText = (TextView)findViewById(R.id.artistName);
        songState = (TextView)findViewById(R.id.songState);

        LeftUp = (Button) this.findViewById(R.id.buttonLeftUp);
        RightDown = (Button) this.findViewById(R.id.buttonRightDown);

        LeftUp.setOnClickListener(change);
        RightDown.setOnClickListener(change);

        // Grab Score TextView Handle, Create Game Object & Add Game to Frame
        TextView score = (TextView) findViewById(R.id.score);
        TextView prcent = (TextView) findViewById(R.id.prcent);

        game = new Game(this, score, prcent, walls, glitch, snakeColor, speed,
                1, 0, false);
        FrameLayout frameView = (FrameLayout) findViewById(R.id.gameFrame);
        frameView.addView(game);

        newWall = walls;
        newGlitch = glitch;
        newSpeed = speed;

        //For music.. TODO: find a way to get current song name.
        manager = (AudioManager)mActivity.getSystemService(Context.AUDIO_SERVICE);

        if(getIntent().hasExtra("songName") && getIntent().hasExtra("artistName")) {
            Intent i = getIntent();
            String name = i.getStringExtra("songName");
            String artist = i.getStringExtra("artistName");

            songName = artist + " - " + name;

            if(manager.isMusicActive())
                songState.setText(">");
            else
                songState.setText("||");

            songNameText.setText(name);
            artistNameText.setText(artist);
        }else if(manager.isMusicActive()){
            songName = SONG_NAME_ERROR;

            songState.setText(">");
            songNameText.setText(SONG_NAME_ERROR);
        }else
            songState.setText("||");
    }

    public void musicReceiver(){
        songNameFilter = new IntentFilter();
        songNameFilter.addAction("com.android.music.metachanged");
        songNameFilter.addAction("com.android.music.playstatechanged");
        songNameFilter.addAction("com.android.music.playbackcomplete");
        songNameFilter.addAction("com.android.music.queuechanged");

        //Support for other players.. no control yet on them.
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

            Log.i("hr.knezzz.snake.game.GameScreen", "Song details - "+intent.getExtras());

            //TODO: Find way to control music with buttons in app.
            //Tracking if music isk playing. If so song name will be shown.
            if(!playing) {
                songName = "No Music";
                songState.setText("||");
            }else {
                songName = artist + " " + track;
                songState.setText(">");

                songNameText.setText(""+track);
                artistNameText.setText(""+artist);
            }

            Log.d("hr.knezzz.snake.game.GameScreen","Song - "+songName);
        }
    };

    //Changing controls and buttons accordingly
    final OnClickListener change = new OnClickListener() {
        @Override
        public void onClick(View v) {
            LinearLayout layout = (LinearLayout) mActivity
                    .findViewById(R.id.controls);

            switch (v.getId()) {
                case R.id.buttonRightDown:
                    if (layout.getOrientation() == LinearLayout.HORIZONTAL) {
                        game.snake.turnRight();
                        layout.setOrientation(LinearLayout.VERTICAL);
                    } else {
                        game.snake.turnDown();
                        layout.setOrientation(LinearLayout.HORIZONTAL);
                    }
                    break;
                case R.id.buttonLeftUp:
                    if (layout.getOrientation() == LinearLayout.HORIZONTAL) {
                        game.snake.turnLeft();
                        layout.setOrientation(LinearLayout.VERTICAL);
                    } else {
                        game.snake.turnUp();
                        layout.setOrientation(LinearLayout.HORIZONTAL);
                    }
                    break;
            }
        }
    };

    //Called when from Game -- Brings up new activity (in form of panel)
    public void gameOver() {
        SharedPreferences.Editor userPreferencesEditor;

        Log.i("hr.knezzz.GameScreen", "gameOver");

        int highScore = userPreferences.getInt("highScore", 0);

        userPreferencesEditor = userPreferences.edit();
/*
        Log.e("Speed", ""+newSpeed+"/"+userPreferences.getInt("speed", 15));

        if(newSpeed != userPreferences.getInt("speed", 1))
            userPreferencesEditor.putInt("speed", newSpeed);

        if(newWall != userPreferences.getBoolean("wall", true))
            userPreferencesEditor.putBoolean("wall", newWall);*/

        if(game.score > highScore) {
            userPreferencesEditor.putInt("highScore", game.score);
            Log.i("hr.knezzz.GameScreen", "NEW HIGH SCORE!!! - "+game.score);
        }

        userPreferencesEditor.apply();

        Intent gameOverIntent = new Intent(mActivity, GameOver.class);

        gameOverIntent.putExtra("songName", songName);

        startActivityForResult(gameOverIntent, 1);
    }

    // On Game Pause, Stop Snake & Make Alert Dialog
    public void pauseGame() {
        if (game.gameOver)
            return;

        Log.d("Snake stopped",""+game.snake.stopped);

        game.snake.stopped = true; //Needed for successful Intent operation
        game.invalidate();

        Intent gamePauseIntent = new Intent(mActivity, GamePause.class);

        gamePauseIntent.putExtra("songName", songName);

        startActivityForResult(gamePauseIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                isGamePause = false;

                if(data.hasExtra("exit")) {
                    Log.d("Intent data found", "EXIT - [" + data.getBooleanExtra("exit", true) + "]");

                    if(data.hasExtra("options"))
                        changingOptions(data);

                    if(data.getBooleanExtra("exit", true)){
                        Intent i = new Intent(mActivity, TitleScreen.class);

                            if(data.hasExtra("songName") && data.hasExtra("artistName")){
                                i.putExtra("songName", data.getStringExtra("songName"));
                                i.putExtra("artistName", data.getStringExtra("artistName"));
                            }else{
                                i.putExtra("songName", songNameText.getText());
                                i.putExtra("artistName", artistNameText.getText());
                            }

                        startActivity(i);
                        game.gameOver = true;
                        unregisterReceiver(mReceiver);
                        mActivity.finish();
                    }else{
                        game.snake.stopped = false;
                        game.invalidate();
                    }
                }else if(data.hasExtra("again")){
                    Log.d("Intent data found", "AGAIN - ["+data.getBooleanExtra("again", true)+"]");

                    if(data.hasExtra("options"))
                        changingOptions(data);

                    if(data.getBooleanExtra("again", false)) {
                        game.setup();
                        game.changeOptions(newWall, newSpeed, newGlitch);//Changing options in game.
                        game.invalidate();
                    }
                }else
                    Log.e("No intent data found", "[DATA IS EMPTY]");
            }else if(resultCode == RESULT_CANCELED){
                Log.e("Something went wrong with receiving data:", "[NO DATA]");
                game.gameOver = true;
                unregisterReceiver(mReceiver);
                mActivity.finish();
            }
        }
    }

    public void changingOptions(Intent data){
        Log.d("Intent data found", "OPTIONS - ["+data.getExtras().toString()+"]");

        game.color = data.getIntExtra("color", Color.GREEN);

        newGlitch = data.getBooleanExtra("glitch", false);
        newWall = data.getBooleanExtra("wall", true);
        newSpeed = data.getIntExtra("speed", 1);
    }


    // Pause Game when Activity Paused
    @Override
    public void onPause() {
        super.onPause();
        Log.d("onPause", "onPause called");
        if(!game.snake.stopped || game.gameOver)
            pauseGame();
    }

    @Override
    public void onBackPressed() {
        super.onPause();
        Log.d("onPause", "onBackPressed called");
        //TODO: Put if statement in case back key is pressed when in pause menu.(Probably to close menu and continue game? or just ignore it)
        if(!isGamePause) {
            pauseGame();
            isGamePause = true;
        }
    }
}
