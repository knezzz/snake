package hr.knezzz.snake;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import hr.knezzz.snake.R;

public class TitleScreen extends Activity {

  SharedPreferences settings;

  @Override
  public void onCreate(Bundle savedInstanceState) {

    setTheme(android.R.style.Theme_Light_NoTitleBar);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.title_screen);
  }

  public void startGame(View view){
    startActivity(new Intent(this, GameScreen.class));
  }

  public void options(View view){
    startActivity(new Intent(this, OptionsScreen.class));
    this.finish();
  }
  
 /* public void about(View view){
	  startActivity(new Intent(this, AboutScreen.class));
  }*/

}
