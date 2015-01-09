package hr.knezzz.snake;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import hr.knezzz.snake.R;

public class OptionsScreen extends Activity {

  SharedPreferences userPreferences, speedSetting;
  SharedPreferences.Editor userPreferencesEditor, speedSettingEditor, modeSettingEditor;
  Spinner wallSpinner,colorSpinner,AiSpinner,speedSpinner,modeSpinner;

  @Override
  public void onCreate(Bundle savedInstanceState) {

    // Grab Existing Settings
    // Speed Setting is Stored in a Different File Because It Should Not Be Synced Across Devices
    userPreferences  = getSharedPreferences("settings", 0);
    int walls = userPreferences.getInt("walls",0);
    int color = userPreferences.getInt("color",0);
    int ai  = userPreferences.getInt("ai",0);
    speedSetting = getSharedPreferences("speed", 0);
    int speed = speedSetting.getInt("speed",0);
    int mode = userPreferences.getInt("mode", 1);
    
    super.onCreate(savedInstanceState);
    setContentView(R.layout.options);

    // Grab Settings Spinners
    wallSpinner = (Spinner) findViewById(R.id.spinnerWall);
    colorSpinner = (Spinner) findViewById(R.id.spinnerColor);
    AiSpinner  = (Spinner) findViewById(R.id.spinnerAi);
    speedSpinner = (Spinner) findViewById(R.id.spinnerSpeed);
    modeSpinner = (Spinner) findViewById(R.id.spinnerMode);
    
    // Set Spinner Current Values
    wallSpinner.setSelection(walls);
    colorSpinner.setSelection(color);
    AiSpinner.setSelection(ai);
    speedSpinner.setSelection(speed);
    modeSpinner.setSelection(mode);
  }

  // Back Button in View
  public void back(View view){
    onBackPressed();
  }

  // Go Back to Title Screen
  @Override
  public void onBackPressed(){

    // Get New Values
    int walls = wallSpinner.getSelectedItemPosition();
    int color = colorSpinner.getSelectedItemPosition();
    int ai = AiSpinner.getSelectedItemPosition();
    int speed = speedSpinner.getSelectedItemPosition();
    int mode = modeSpinner.getSelectedItemPosition();
    
    // Save in Settings
    // Speed Setting is Stored in a Different File Because It Should Not Be Synced Across Devices
    userPreferencesEditor = userPreferences.edit();
    userPreferencesEditor.putInt("walls", walls);
    userPreferencesEditor.putInt("color", color);
    userPreferencesEditor.putInt("ai", ai);
    speedSettingEditor = speedSetting.edit();
    speedSettingEditor.putInt("speed", speed);
    userPreferencesEditor.putInt("mode", mode);    
    userPreferencesEditor.commit();
    speedSettingEditor.commit();

    // Call for Backup
    BackupManager backupManager = new BackupManager(this);
    backupManager.dataChanged();

    // Go Home & Close Options Screen
    Intent intent = new Intent(this, TitleScreen.class);
    startActivity(intent);
    this.finish();
  }
}
