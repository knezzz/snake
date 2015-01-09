package hr.knezzz.snake;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.RadioButton;
//import android.widget.TextView;
import android.widget.ToggleButton;

public class OptionsScreen extends Activity {

    SharedPreferences userPreferences;
    SharedPreferences.Editor userPreferencesEditor;
    GRadioGroup colorChooser, speedChooser;
    ToggleButton toggleWall, toggleGlitch;
   // TextView speedText;
  //  SeekBar speedSlider;

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideSystemUi();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Grab Existing Settings
        userPreferences = getSharedPreferences("settings", 0);
        boolean walls = userPreferences.getBoolean("walls", true);
        int color = userPreferences.getInt("color", 0);
        boolean glitch = userPreferences.getBoolean("glitch", false);
        int speed = userPreferences.getInt("speed", 1);

        setContentView(R.layout.options);

        RadioButton dkGray = (RadioButton)findViewById(R.id.snake_dkgray);
        RadioButton ltGray = (RadioButton)findViewById(R.id.snake_ltgray);
        RadioButton blue = (RadioButton)findViewById(R.id.snake_blue);
        RadioButton green = (RadioButton)findViewById(R.id.snake_green);
        RadioButton yellow = (RadioButton)findViewById(R.id.snake_yellow);

        RadioButton slow = (RadioButton)findViewById(R.id.slow);
        RadioButton normal = (RadioButton)findViewById(R.id.normal);
        RadioButton fast = (RadioButton)findViewById(R.id.fast);

        // Grab Settings Spinners
        toggleWall = (ToggleButton) findViewById(R.id.toggleWall);
        colorChooser = new GRadioGroup(dkGray,ltGray,blue,green,yellow);
        speedChooser = new GRadioGroup(slow, normal, fast);
        toggleGlitch = (ToggleButton) findViewById(R.id.toggleGlitch);

        toggleWall.setChecked(walls);
        toggleGlitch.setChecked(glitch);
        colorChooser.setSelected(color);
        speedChooser.setSelected(speed);


        Button back = (Button) findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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

    // Go Back to Title Screen
    @Override
    public void onBackPressed() {

        // Get New Values
        boolean walls = toggleWall.isChecked();
        int color = colorChooser.getSelected();
        boolean glitch = toggleGlitch.isChecked();
        int speed = speedChooser.getSelected();

        // Save in Settings
        userPreferencesEditor = userPreferences.edit();
        userPreferencesEditor.putBoolean("walls", walls);
        userPreferencesEditor.putInt("color", color);
        userPreferencesEditor.putBoolean("glitch", glitch);
        userPreferencesEditor.putInt("speed", speed);
        userPreferencesEditor.apply();

        // Call for Backup
        BackupManager backupManager = new BackupManager(this);
        backupManager.dataChanged();

        // Go Home & Close Options Screen
      //  Intent intent = new Intent(this, TitleScreen.class);
     //   startActivity(intent);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("glitch", glitch);
        returnIntent.putExtra("walls",walls);
        returnIntent.putExtra("speed", speed);
        returnIntent.putExtra("color", color);

        setResult(RESULT_OK,returnIntent);
        this.finish();
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
}
