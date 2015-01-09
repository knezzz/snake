package hr.knezzz.snake;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameScreen extends Activity {

	private Game game;
	private FrameLayout frameView;
	private TextView score, prcent;
	private Activity mActivity;
	SharedPreferences userPreferences, speedSetting, colorSetting;
	private boolean walls = false, AI = false;
	private int speed, color, mode, length;
	private Button LeftUp, RightDown;

	// Initialize Game Screen
	@Override
	public void onCreate(Bundle savedInstanceState) {

		// Set Theme, Controls Mode, View Mode & Speed According to Settings
		// Speed Setting is Stored in a Different File Because It Should Not Be
		// Synced Across Devices
		userPreferences = getSharedPreferences("settings", 0);
		speedSetting = getSharedPreferences("speed", 0);
		if (userPreferences.getInt("ai", 0) == 1)
			AI = true;
		if (userPreferences.getInt("walls", 0) == 1)
			walls = true;
		speed = speedSetting.getInt("speed", 1);
		color = userPreferences.getInt("color", 0);
		mode = userPreferences.getInt("mode", 0);
		
		length = userPreferences.getInt("length", 3);

		// Create Game View & Add Handler to Current Activity
		super.onCreate(savedInstanceState);
		/*
		 * if(snakeOriented) setContentView(R.layout.game_2arrow); else
		 * setContentView(R.layout.game_4arrow);
		 */
		setContentView(R.layout.game);

		mActivity = this;

		LeftUp = (Button) this.findViewById(R.id.buttonLeftUp);
		RightDown = (Button) this.findViewById(R.id.buttonRightDown);

		LeftUp.setOnClickListener(change);
		RightDown.setOnClickListener(change);

		// Grab Score TextView Handle, Create Game Object & Add Game to Frame
		score = (TextView) findViewById(R.id.score);
		prcent = (TextView) findViewById(R.id.prcent);
		game = new Game(this, this, score, prcent, walls, AI, color, speed,
				mode);
		frameView = (FrameLayout) findViewById(R.id.gameFrame);
		frameView.addView(game);

	}

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

	//Called when from Game -- Offers 3 options and 
	public void gameOver() {

		final CharSequence[] menuItems = { "Play Again", "Change Options",
				"Go Back" };
		final AlertDialog.Builder gameOver = new AlertDialog.Builder(this);
		gameOver.setTitle("Score: " + game.score);
		gameOver.setItems(menuItems, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
				// Play Again
				case 0:
					game.setup();
					game.invalidate();
					/******************************************************
					 * Added to put controls back, if AI is 
					 * on controles will be removed once again in Game
					 *******************************************************/
					findViewById(R.id.buttonLeftUp).setVisibility(Button.VISIBLE);
					findViewById(R.id.buttonRightDown).setVisibility(Button.VISIBLE);
					break;

				case 1:
					startActivity(new Intent(mActivity, OptionsScreen.class));
					break;

				// Go Back
				default:
					mActivity.finish();
				}
			}
		});
		
		//Removing buttons so background is listener to bring popup window back to front
		findViewById(R.id.buttonLeftUp).setVisibility(Button.GONE);
		findViewById(R.id.buttonRightDown).setVisibility(Button.GONE);

		gameOver.create().show();
		
		//Reopen window by pressing anywhere on screen.
		frameView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(game.gameOver)
					gameOver.create().show();				
			}
		});
	}

	// On Game Pause, Stop Snake & Make Alert Dialog
	public void pauseGame() {
		if (game.gameOver)
			return;

		game.snake.stopped = true;

		final CharSequence[] menuItems = { "Continue", "Start Over",
				"Change Options", "Go Back" };
		AlertDialog.Builder pauseGame = new AlertDialog.Builder(this);
		pauseGame.setTitle("Pause current score: " + game.score);
		pauseGame.setItems(menuItems, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
				// New Game (Start Over)
				case 1:
					game.setup();
					game.invalidate();
				break;
				// Show options
				case 2:
					game.snake.stopped = false; //Needed for successful Intent operation
					game.invalidate();
					startActivity(new Intent(mActivity, OptionsScreen.class));
				break;
				// End Game (Go Back)
				case 3:
					mActivity.finish();
				break;
				// Continue Game
				default:
					game.snake.stopped = false;
					game.invalidate();
				}
			}
		});

		pauseGame.setCancelable(false);
		pauseGame.create().show();
	}

	// Pause Game when Activity Paused
	@Override
	public void onPause() {
		super.onPause();
		pauseGame();
	}

}
