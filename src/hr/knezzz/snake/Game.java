package hr.knezzz.snake;

import java.util.ArrayList;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Game extends View {

	private boolean setupComplete = false;
	private int pxSquare, squaresWidth, squaresHeight, sqBorder = 0,
			padding = 0, color, mode;
	private static ArrayList<Block> walls;
	ArrayList<Integer> lastTurn = new ArrayList<Integer>();
	public Snake snake;
	private Food food;
	private Random random;
	private TextView scoreView, prcent;
	private GameScreen mActivity;
	public boolean gameOver = false;
	public int score;//So I can put score on GAME OVER screen.
	private int frameRate, size;//Size is used to calculate percents.
	private boolean wall, AI;

	public Game(Context context, GameScreen activity, TextView scoreView, TextView prcent,
			boolean wall, boolean AI, int color, int speed, int mode) {
		super(context);
		mActivity = activity;
		random = new Random();
		this.scoreView = scoreView;
		this.prcent = prcent;
		this.wall = wall;
		this.color = color;
		this.AI = AI;
		if(speed < 4)
			this.frameRate = 5 * (speed + 1);
		else if(speed == 4) //For FASTER speed
			this.frameRate = 5 * ((speed*10) + 1);
		else//Fastest speed (1000 fps) TODO:figrure out how to speed up snake and slow down FPS.
			this.frameRate = 1000;
		this.mode = mode;
	}

	// If User or AI Scores
	private void score() {
		score++;
		scoreView.setText(Integer.toString(this.score));
		
		//Writing percents on bottom right
		prcent.setText(String.format("%.4f", (double)(snake.length)/(size)*100) + "%");
				
		if(snake.length >= squaresWidth && snake.length < squaresWidth+2)
			scoreView.setText("Wdth");
		if(snake.length >= squaresHeight && snake.length < squaresHeight+2)
			scoreView.setText("Hght");	
	}

	// Draw View
	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas) {
		if (!setupComplete) {
			setup();
			this.invalidate();
			return;
		}

		// Draw Walls
		for (Block block : walls) {
			block.draw(canvas);
		}

		// Move & Draw Snake
		snake.draw(canvas);

		// Draw Food
		food.draw(canvas);
		
	//	AI goes live here.
		if(!AI)
			AI();

		// Invalidate View After Timer, Using New Thread to prevent Blocking UI
		// Thread
		// If Snake is Stopped, Wait and then call game over
		final View parent = this;
		if (!snake.stopped) {
			new Thread(new Runnable() {
				public void run() {
					parent.postDelayed(new Runnable() {
						public void run() {
							parent.invalidate();
						}
					}, 1000 / frameRate);
				}
			}).start();
		} else if (gameOver) {
			new Thread(new Runnable() {
				public void run() {
					parent.postDelayed(new Runnable() {
						public void run() {
							mActivity.gameOver();
						}
					}, 500);
				}
			}).start();
		}
	}

	// Setup View
	public void setup() {
		gameOver = false;

		// Calculate Width of View in Inches
		int pxWidth = getWidth();
		int pxHeight = getHeight();
		int dpi = (int)(0.4 * getResources().getDisplayMetrics().densityDpi);
		float cmWidth = ((float) pxWidth) / dpi;
		float cmHeight = ((float) pxHeight) / dpi;

		switch (mode) {
		case 0://Big
			squaresWidth = (int) (cmWidth * 3);
			squaresHeight = (int) (cmHeight * 3);
		break;
		case 1://Normal
			squaresWidth = (int) (cmWidth * 4);
			squaresHeight = (int) (cmHeight * 4);
		break;
		case 2://Small
			squaresWidth = (int)(cmWidth * 6);
			squaresHeight= (int)(cmHeight * 6);
		break;
		case 3://mm
			squaresWidth = (int)(cmWidth * 10);
			squaresHeight = (int)(cmHeight * 10);
		break;
		case 4:// Double pixel mode
			squaresWidth = (int) (pxWidth / 2);
			squaresHeight = (int) (pxHeight / 2);
		break;
		case 5:// Pixel game. (PIXel Gamer). <<< First pixel game.
			squaresWidth = (int) (pxWidth);
			squaresHeight = (int) (pxHeight);
		break;
		}

		if (squaresHeight < 20)
			squaresHeight = 20;
		if (squaresWidth < 15)
			squaresWidth = 15;

		// Calculate Size of Squares
		int pxSquareWidth = pxWidth / squaresWidth;
		int pxSquareHeight = pxHeight / squaresHeight;
		if (pxSquareWidth > pxSquareHeight)
			pxSquare = pxSquareHeight; // Extra Space on Sides
		else
			pxSquare = pxSquareWidth; // Extra Space on Top

		// Calculate Padding Around & Between Squares
		padding = (pxWidth - squaresWidth * pxSquare) / 2;
		
		//This makes snake padding on blocks
		sqBorder = pxSquare / 25;

		// Build List of Wall Objects
		walls = new ArrayList<Block>();
		for (int j = 0; j < squaresWidth+1; j++) {
			walls.add(new Block(j, -1, 0)); // Top Walls
			walls.add(new Block(j-1, squaresHeight, 0)); // Bottom Walls
		}
		for (int j = 0; j < (squaresHeight+1); j++) { // Left Walls
			walls.add(new Block(-1, j-1, 0)); // Left Walls
			walls.add(new Block(squaresWidth, j, 0)); // Right Walls
		}

		// Create Snake
		snake = new Snake();

		// Create Food
		food = new Food(snake, walls);
		
		// Reset Score
		score = -1;
		size = squaresHeight*squaresWidth;
		this.score();

		setupComplete = true;
	}

	// Snake Object contains a list of blocks, knows if it is moving and
	// which direction it is moving
	public class Snake {

		public ArrayList<Block> blocks;
		private int direction, length;
		public boolean stopped = false;

		// Create Snake with 3 Blocks
		public Snake() {

			// Create Leading Block
			blocks = new ArrayList<Block>();
			blocks.add(new Block(squaresWidth / 2, squaresHeight / 2, 1));
			
			length = 3;
			
			// Calculate Random Initial Direction and Add 2 Remaining Blocks
			direction = random.nextInt(4);
			for(int i = 1; i < length; i++){
				switch (direction) {
				case 0: // Going Right
					blocks.add(new Block(squaresWidth / 2 - i, squaresHeight / 2, 1));
					break;
				case 1: // Going Down
					blocks.add(new Block(squaresWidth / 2, squaresHeight / 2 - i, 1));
					break;
				case 2: // Going Left
					blocks.add(new Block(squaresWidth / 2 + i, squaresHeight / 2, 1));
					break;
				case 3: // Going Up
					blocks.add(new Block(squaresWidth / 2, squaresHeight / 2 + i, 1));
				}
			}

			LinearLayout layout = (LinearLayout) mActivity
					.findViewById(R.id.controls);
			if (direction == 2 || direction == 0)
				layout.setOrientation(LinearLayout.VERTICAL);
		}

		// Move & Draw Snake
		public void draw(Canvas canvas) {
			if (!stopped)
				move();
			for (Block block : blocks)
				block.draw(canvas);
		}
		
		//Testing for AI--
		public Block getHead(){
			return blocks.get(0);
		}
		
		public int getDirection(){
			return direction;
		}

		public void turnLeft() {
			if (this.direction != 0)
				this.direction = 2;
		}

		public void turnRight() {
			if (this.direction != 2)
				this.direction = 0;
		}

		public void turnDown() {
			if (this.direction != 3)
				this.direction = 1;
		}

		public void turnUp() {
			if (this.direction != 1)
				this.direction = 3;
		}

		// Move Snake 1 Space in Current Direction
		public void move() {

			// Grab Current Front Block
			Block frontBlock = blocks.get(0);

			// Create New Block at Front of Snake
			Block newBlock;
			switch (direction) {
			case 0: // Going Right
				newBlock = new Block(frontBlock.x + 1, frontBlock.y, 1);
				break;
			case 1: // Going Down
				newBlock = new Block(frontBlock.x, frontBlock.y + 1, 1);
				break;
			case 2: // Going Left
				newBlock = new Block(frontBlock.x - 1, frontBlock.y, 1);
				break;
			default: // Going Up
				newBlock = new Block(frontBlock.x, frontBlock.y - 1, 1);
			}

			// Maybe different color walls (Black - no passing, Light Gray - you
			// can pass)
			if (newBlock.collides(walls) && !wall) {// Going though walls.
													// (Spawn on other side)
				switch (direction) {
				case 0: // Going Right
					newBlock = new Block(0, frontBlock.y, 1);
					break;
				case 1: // Going Down
					newBlock = new Block(frontBlock.x, 0, 1);
					break;
				case 2: // Going Left
					newBlock = new Block(squaresWidth - 1, frontBlock.y, 1);
					break;
				default: // Going Up
					newBlock = new Block(frontBlock.x, squaresHeight - 1, 1);
				}
			}

			// If New Front Block Collides with Self
			if (this.collides(newBlock) || (newBlock.collides(walls) && wall)){
				stopped = true;
				for (Block block : blocks){
					//TODO:Not sure why this is here.
					if(block.getType() == Color.BLACK || block == getHead() || block == blocks.get(blocks.size()-1));
					else
						block.setType(3);
				}
								
				gameOver = true;
				// If New Block is Clear
			} else {

				// Add New Block to the Front
				blocks.add(0, newBlock);
				
				// If Collision with Food
				// Add additional types of food?.
				if (this.collides(food)) {
					food.move(this, walls);
					length++;
					newBlock.setType(2);
					score();
					// If No Collision with Food, Remove Last Block
				} else
					blocks.remove(length);
			}
		}

		// Check for Collisions
		public boolean collides(Block block) {
			for (Block oneBlock : this.blocks)
				if (block.collides(oneBlock))
					return true;
			return false;
		}

	}

	public class Block {
		public int x = 0, y = 0;
		ShapeDrawable shape;

		public Block() {
		}

		public Block(int x, int y, int type) {
			this.x = x;
			this.y = y;

			shape = new ShapeDrawable(new RectShape());
			shape.setBounds(padding + x * pxSquare + sqBorder, padding + y
					* pxSquare + sqBorder, padding + (x + 1) * pxSquare
					- sqBorder, padding + (y + 1) * pxSquare - sqBorder);
			
			this.setType(type);
		}

		public void draw(Canvas canvas) {
			shape.draw(canvas);
		}

		public boolean collides(Block block) {
			return block.x == this.x && block.y == this.y;
		}

		public boolean collides(ArrayList<Block> blocks) {
			for (Block block : blocks) {
				if (this.collides(block))
					return true;
			}
			return false;
		}

		public void setType(int type) {//More colors
			switch (type) {
			case 0: // If Wall
				if (wall)
					shape.getPaint().setColor(Color.argb(200, 200, 200, 200));
				else
					shape.getPaint().setColor(Color.argb(40, 150, 150, 150));
				break;
			case 1: // If Snake
				switch (color) {
				case 0://User
					shape.getPaint().setColor(Color.DKGRAY);
					break;
				case 1://AI
					shape.getPaint().setColor(Color.LTGRAY);
					break;
				case 2://User
					shape.getPaint().setColor(Color.BLUE);
					break;
				case 3://AI
					shape.getPaint().setColor(Color.YELLOW);
					break;
				}
				break;
			case 2: // If Food
				shape.getPaint().setColor(Color.BLACK);
				break;
			case 3: // If Collision
				shape.getPaint().setColor(Color.argb(150, 255, 20, 20));
				break;
			}
		}
		
		//Testing XXX 
		public int getType(){
			return shape.getPaint().getColor();
		}
	}

	class Food extends Block {

		public Food(Snake snake, ArrayList<Block> blocks) {
			shape = new ShapeDrawable(new RectShape());
			this.setType(2);
			this.move(snake, blocks);
		}

		public void move(Snake snake, ArrayList<Block> blocks) {
			while (true) {
				this.x = random.nextInt(squaresWidth - 3) + 1;
				this.y = random.nextInt(squaresHeight - 3) + 1;
				if (!snake.collides(this) && !this.collides(blocks))
					break;
			}
			shape.setBounds(padding + x * pxSquare + sqBorder, padding + y
					* pxSquare + sqBorder, padding + (x + 1) * pxSquare
					- sqBorder, padding + (y + 1) * pxSquare - sqBorder);
		}
		
		public int getFoodX(){
			return x;
		}
		
		public int getFoodY(){
			return y;
		}
	}
	
	//Works.
	//TODO:Figure out how to not to box self in. Start from beginning?.
	public void AI(){
		mActivity.findViewById(R.id.buttonLeftUp).setVisibility(View.GONE);
		mActivity.findViewById(R.id.buttonRightDown).setVisibility(View.GONE);
		
		int fx = food.getFoodX();
		int fy = food.getFoodY();
		
		int sx = snake.getHead().x;
		int sy = snake.getHead().y;
		
		int newDirection = snake.direction;
		
		Block frontBlock = snake.blocks.get(0);
		Block newBlock;
						
		//Main function GOAL: Find food and position snake towards food.
		switch(newDirection){
			case 0://Right
				if(fx > sx && !snake.collides(new Block(frontBlock.x + 1, frontBlock.y, 1)));
				else if(fx > sx && snake.collides(new Block(frontBlock.x + 1, frontBlock.y, 1)))
					newDirection = betterDirection(snake.direction, frontBlock);
				else if(fx > sx && (snake.collides(new Block(frontBlock.x+1, frontBlock.y+1,1)) || snake.collides(new Block(frontBlock.x+1, frontBlock.y-1,1))))
						newDirection = betterDirection(snake.direction, frontBlock);
				else{
					if(fy == sy && (!snake.collides(new Block(frontBlock.x+1, frontBlock.y+1,1)) || !snake.collides(new Block(frontBlock.x+1, frontBlock.y-1,1))))
						newDirection = betterDirection(snake.direction, frontBlock);
					else if(fy > sy && !snake.collides(new Block(frontBlock.x+1, frontBlock.y, 1)))
						newDirection = 1;
					else if(fy < sy && !snake.collides(new Block(frontBlock.x+1, frontBlock.y, 1)))
						newDirection = 3;
					else{
						if(lastTurn.size() > 0)
							newDirection = lastTurn.get(lastTurn.size()-1);
						else
							betterDirection(snake.direction, frontBlock);
					}
				}
			break;
			case 1://Down
				if(fy > sy && !snake.collides(new Block(frontBlock.x, frontBlock.y + 1, 1)));
				else if(fy > sy && snake.collides(new Block(frontBlock.x, frontBlock.y + 1, 1)))
					newDirection = betterDirection(snake.direction, frontBlock);
				else if(fy > sy && (snake.collides(new Block(frontBlock.x+1,frontBlock.y+1,1)) || snake.collides(new Block(frontBlock.x-1, frontBlock.y+1, 1))))
					newDirection = betterDirection(snake.direction, frontBlock);
				else{
					if(fx == sx && (!snake.collides(new Block(frontBlock.x+1,frontBlock.y+1,1)) || !snake.collides(new Block(frontBlock.x-1, frontBlock.y+1, 1))))			
						newDirection = betterDirection(snake.direction, frontBlock);
					else if(fx > sx && !snake.collides(new Block(frontBlock.x, frontBlock.y+1, 1)))
						newDirection = 0;
					else if(fx < sx && !snake.collides(new Block(frontBlock.x, frontBlock.y+1, 1)))
						newDirection = 2;
					else{
						if(lastTurn.size() > 0)
							newDirection = lastTurn.get(lastTurn.size()-1);
						else
							betterDirection(snake.direction, frontBlock);
					}
				}
			break;
			case 2://Left
				if(fx < sx && !snake.collides(new Block(frontBlock.x - 1, frontBlock.y, 1)));
				else if(fx < sx && snake.collides(new Block(frontBlock.x - 1, frontBlock.y, 1)))
					newDirection = betterDirection(snake.direction, frontBlock);
				else if(fx < sx && (snake.collides(new Block(frontBlock.x-1,frontBlock.y+1,1)) || snake.collides(new Block(frontBlock.x-1,frontBlock.y-1,1))))
						newDirection = betterDirection(snake.direction, frontBlock);
				else{
					if(fy == sy && (!snake.collides(new Block(frontBlock.x-1,frontBlock.y+1,1)) || !snake.collides(new Block(frontBlock.x-1,frontBlock.y-1,1))))
						newDirection = betterDirection(snake.direction, frontBlock);
					else if(fy > sy && !snake.collides(new Block(frontBlock.x-1, frontBlock.y, 1)))
						newDirection = 1;
					else if(fy < sy && !snake.collides(new Block(frontBlock.x-1, frontBlock.y, 1)))
						newDirection = 3;
					else{
						if(lastTurn.size() > 0)
							newDirection = lastTurn.get(lastTurn.size()-1);
						else
							betterDirection(snake.direction, frontBlock);
					}
				}
			break;
			case 3://Up
				if(fy < sy && !snake.collides(new Block(frontBlock.x, frontBlock.y - 1, 1)));
				else if(fy < sy && snake.collides(new Block(frontBlock.x, frontBlock.y - 1, 1)))
					newDirection = betterDirection(snake.direction, frontBlock);
				else if(fy < sy && (snake.collides(new Block(frontBlock.x+1,frontBlock.y-1,1)) || snake.collides(new Block(frontBlock.x-1,frontBlock.y-1,1))))
					newDirection = betterDirection(snake.direction, frontBlock);
				else{
					if(fx == sx && (!snake.collides(new Block(frontBlock.x+1,frontBlock.y-1,1)) || !snake.collides(new Block(frontBlock.x-1,frontBlock.y-1,1))))
						newDirection = betterDirection(snake.direction, frontBlock);
					else if(fx > sx && !snake.collides(new Block(frontBlock.x, frontBlock.y-1, 1)))
						newDirection = 0;
					else if(fx < sx && !snake.collides(new Block(frontBlock.x, frontBlock.y-1, 1)))
						newDirection = 2;
					else{
						if(lastTurn.size() > 0)
							newDirection = lastTurn.get(lastTurn.size()-1);
						else
							betterDirection(snake.direction, frontBlock);
					}
				}
			break;
		}
		boolean gotDirection = false;
		
		for(int i = 0; i < 5; i++){			
			switch(newDirection) {
				case 0: // Going Right
					newBlock = new Block(frontBlock.x + 1, frontBlock.y, 1);
					break;
				case 1: // Going Down
					newBlock = new Block(frontBlock.x, frontBlock.y + 1, 1);
					break;
				case 2: // Going Left
					newBlock = new Block(frontBlock.x - 1, frontBlock.y, 1);
					break;
				default: // Going Up
					newBlock = new Block(frontBlock.x, frontBlock.y - 1, 1);
			}
			
			if(newBlock.collides(walls) && !wall){
				switch (newDirection) {
				case 0: // Going Right
					newBlock = new Block(1, frontBlock.y, 1);
					break;
				case 1: // Going Down
					newBlock = new Block(frontBlock.x, 1, 1);
					break;
				case 2: // Going Left
					newBlock = new Block(squaresWidth - 2, frontBlock.y, 1);
					break;
				default: // Going Up
					newBlock = new Block(frontBlock.x, squaresHeight - 2, 1);
				}
			}
						
			if(!snake.collides(newBlock)){
				if(newBlock.collides(walls) && wall);
				else{
					int oldDirection = snake.direction;
					if(oldDirection != newDirection){
						switch(newDirection){
							case 0:
								snake.turnRight();
							break;
							case 1:
								snake.turnDown();
							break;
							case 2:
								snake.turnLeft();
							break;
							default:
								snake.turnUp();
						}
						
						if(lastTurn.size() <= 1){
							lastTurn.add(newDirection);
						}else if(oldDirection!=newDirection){
							lastTurn.remove(0);
							lastTurn.add(newDirection);
						}
					}
					break;
				}
			}else if(!gotDirection){
				switch(newDirection){
					case 0:
						if(snake.collides(new Block(frontBlock.x,frontBlock.y+1,1)) || snake.collides(new Block(frontBlock.x, frontBlock.y-1, 1)))
							newDirection = 0;
						if(snake.collides(new Block(frontBlock.x+1,frontBlock.y,1)))
							newDirection = betterDirection(snake.direction, frontBlock);
					break;
					case 1:
						if(snake.collides(new Block(frontBlock.x+1,frontBlock.y,1)) || snake.collides(new Block(frontBlock.x-1, frontBlock.y, 1)))
							newDirection = 1;
						if(snake.collides(new Block(frontBlock.x,frontBlock.y+1,1)))
							newDirection = betterDirection(snake.direction, frontBlock);
					break;
					case 2:
						if(snake.collides(new Block(frontBlock.x,frontBlock.y+1,1)) || snake.collides(new Block(frontBlock.x, frontBlock.y-1, 1)))
							newDirection = 2;
						if(snake.collides(new Block(frontBlock.x-1,frontBlock.y,1)))
							newDirection = betterDirection(snake.direction, frontBlock);
					break;
					case 3:
						if(snake.collides(new Block(frontBlock.x+1,frontBlock.y,1)) || snake.collides(new Block(frontBlock.x-1, frontBlock.y, 1)))
							newDirection = 3;
						if(snake.collides(new Block(frontBlock.x, frontBlock.y-1, 1)))
							newDirection = betterDirection(snake.direction, frontBlock);
					break;
				}
				
				gotDirection = true;
			}else{
				newDirection+=1;//TODO:Remove this with algorithm posted below. possibly with timer.	
				if(newDirection > 3) newDirection = 0;
			}
				
		}
	}
	
	/*
	 * TODO: make if inside switch to "see" trough walls, and adding switch from above to calculate where it has more space.
	 * */
	public int betterDirection(int direction, Block frontBlock){
		int dir = direction;
		int sonar = sonar(direction, frontBlock);
				
		switch (direction){
		case 0: case 2:
			int crashesDown = 0, crashesUp = 0;
			
			if(snake.collides(new Block(frontBlock.x+1, frontBlock.y+1,1)))
				crashesDown++;
			if(snake.collides(new Block(frontBlock.x, frontBlock.y+1,1)))
				crashesDown++;
			if(snake.collides(new Block(frontBlock.x-1,frontBlock.y+1,1)))
				crashesDown++;
			
			if(snake.collides(new Block(frontBlock.x+1, frontBlock.y-1,1)))
				crashesUp++;
			if(snake.collides(new Block(frontBlock.x, frontBlock.y-1,1)))
				crashesUp++;
			if(snake.collides(new Block(frontBlock.x-1,frontBlock.y-1,1)))
				crashesUp++;

			if(crashesDown < crashesUp)
				dir = 1;
			else if(crashesDown > crashesUp)
				dir = 3;
			else 
				dir = direction;
		break;
		case 1: case 3:
			int crashesLeft = 0,crashesRight = 0;
			
			if(snake.collides(new Block(frontBlock.x+1, frontBlock.y+1,1)))
				crashesRight++;
			if(snake.collides(new Block(frontBlock.x+1, frontBlock.y,1)))
				crashesRight++;
			if(snake.collides(new Block(frontBlock.x+1,frontBlock.y-1,1)))
				crashesRight++;
			
			if(snake.collides(new Block(frontBlock.x-1, frontBlock.y+1,1)))
				crashesLeft++;
			if(snake.collides(new Block(frontBlock.x-1, frontBlock.y,1)))
				crashesLeft++;
			if(snake.collides(new Block(frontBlock.x-1,frontBlock.y-1,1)))
				crashesLeft++;

			if(crashesRight < crashesLeft)
				dir = 0;
			else if(crashesRight > crashesLeft)
				dir = 2;
			else
				dir = direction;

		break;
		default:
			Log.e("hr.knezzz.snake.Game", "Something went wrong in betterDirection switch loop");
		}
				
		if(sonar == direction || sonar == dir)
			return dir;
		else if(dir == direction && sonar != direction)
			return sonar;
		else if(dir == direction && !lastTurn.isEmpty())
			return lastTurn.get(lastTurn.size()-1);
		else
			return direction;
	}
	
	//TODO:Fix still showing box (1) even if there is no box.
/*	public int sonar(int direction, Block frontBlock){
		int up = 0, down = 0, left = 0, right = 0;
		boolean Bup = false, Bdown = false, Bleft = false, Bright = false;
		int dir = direction;
		
		switch(direction){
		case 0://right
			for(int c = 1; c < snake.length/2; c++){
				//Check down
				if(snake.collides(new Block(frontBlock.x,frontBlock.y+c,1))){
					down=c;
					for(int l = 0; l < snake.length/2; l++)
						if(snake.collides(new Block(frontBlock.x-l, frontBlock.y+c, 1)))
							for(int ll = 0; ll < snake.length/2; ll++)
								if(snake.collides(new Block(frontBlock.x-l, frontBlock.y-ll, 1))){
									if(down == ll)
										Bdown = true;
								//	break sloop;
								}
				//	break sloop;
				}
				//Check up
				if(snake.collides(new Block(frontBlock.x,frontBlock.y-c,1))){
					up = c;
					for(int l = 0; l < snake.length/2; l++)
						if(snake.collides(new Block(frontBlock.x-l, frontBlock.y-c, 1)))
							for(int ll = 0; ll < snake.length/2; ll++)
								if(snake.collides(new Block(frontBlock.x-l, frontBlock.y+ll, 1))){
									if(up == ll)
										Bup = true;
								//	break sloop;
								}
				//	break;
				}
			}
			
			if(Bup == true) Log.e("BOX", "There is box up (" + up + ")");
			if(Bdown == true) Log.e("BOX", "There is box down (" + down + ")");
			
			if(Bdown == true && Bup == true)
				if(up > down) return 3;
				else return 1;
			else if(Bdown == true) return 3;
			else if(Bup == true) return 1;
			else if(up > down && down != 0) return 3;
			else if(down > up && up != 0) return 1;
			else if(lastTurn.size() > 0) return lastTurn.get(lastTurn.size()-1);
			else return dir;
		case 1://down
			for(int c = 1; c < snake.length/2; c++){
				if(snake.collides(new Block(frontBlock.x+c,frontBlock.y,1))){
					right = c;
					for(int l = 0; l < snake.length/2; l++)
						if(snake.collides(new Block(frontBlock.x+c, frontBlock.y-l, 1)))
							for(int ll = 0; ll < snake.length/2; ll++)
								if(snake.collides(new Block(frontBlock.x-ll, frontBlock.y-l, 1))){
									if(right == ll)
										Bright = true;
								//	break sloop;
								}
				//	break;
				}
				if(snake.collides(new Block(frontBlock.x-c,frontBlock.y,1))){
					left = c;
					for(int l = 0; l < snake.length/2; l++)
						if(snake.collides(new Block(frontBlock.x-c, frontBlock.y-l, 1)))
							for(int ll = 0; ll < snake.length/2; ll++)
								if(snake.collides(new Block(frontBlock.x+ll, frontBlock.y-l, 1))){
									if(left == ll)
										Bleft = true;
								//	break sloop;
								}
				//	break;
				}
			}
			
			if(Bleft == true) Log.e("BOX", "There is box left (" + left + ")");
			if(Bright == true) Log.e("BOX", "There is box right (" + right + ")");
			
			if(Bleft == true && Bright == true)
				if(right > left) return 0;
				else return 2;
			else if(Bleft == true) return 0;
			else if(Bright == true) return 2;
			else if(right > left && left != 0) return 0;
			else if(left > right && right != 0) return 2;
			else if(lastTurn.size() > 0) return lastTurn.get(lastTurn.size()-1);
			else return dir;
		case 2://left
			for(int c = 1; c < snake.length/2; c++){
				if(snake.collides(new Block(frontBlock.x,frontBlock.y+c,1))){
					down=c;
					for(int l = 0; l < snake.length/2; l++)
						if(snake.collides(new Block(frontBlock.x+l, frontBlock.y+c, 1)))
							for(int ll = 0; ll < snake.length/2; ll++)
								if(snake.collides(new Block(frontBlock.x+l, frontBlock.y-ll, 1))){
									if(down == ll)
										Bdown = true;
								//	break sloop;
								}
				//	break;
				}
				if(snake.collides(new Block(frontBlock.x,frontBlock.y-c,1))){
					up = c;
					for(int l = 0; l < snake.length/2; l++)
						if(snake.collides(new Block(frontBlock.x+l, frontBlock.y-c, 1)))
							for(int ll = 0; ll < snake.length/2; ll++)
								if(snake.collides(new Block(frontBlock.x+l, frontBlock.y+ll, 1))){
									if(up == ll)
										Bup = true;
								//	break sloop;
								}
				//	break;
				}
			}
			
			if(Bup == true) Log.e("BOX", "There is box up (" + up + ")");
			if(Bdown == true) Log.e("BOX", "There is box down (" + down + ")");
			
			if(Bdown == true && Bup == true)
				if(up > down) return 3; 
				else return 1;
			else if(Bdown == true) return 3;
			else if(Bup == true) return 1;
			else if(up > down && down != 0) return 3;
			else if(down > up && up != 0) return 1;
			else if(lastTurn.size() > 0) return lastTurn.get(lastTurn.size()-1);
			else return dir;
		case 3://up
			for(int c = 1; c < snake.length/2; c++){
				if(snake.collides(new Block(frontBlock.x+c,frontBlock.y,1))){
					right = c;
					for(int l = 0; l < snake.length/2; l++)
						if(snake.collides(new Block(frontBlock.x+c, frontBlock.y+l, 1)))
							for(int ll = 0; ll < snake.length/2; ll++)
								if(snake.collides(new Block(frontBlock.x-ll, frontBlock.y+l, 1))){
									if(right == ll)
										Bright = true;
								//	break sloop;
								}
				//	break;
				}
				if(snake.collides(new Block(frontBlock.x-c,frontBlock.y,1))){
					left = c;
					for(int l = 0; l < snake.length/2; l++)
						if(snake.collides(new Block(frontBlock.x-c, frontBlock.y+l, 1)))
							for(int ll = 0; ll < snake.length/2; ll++)
								if(snake.collides(new Block(frontBlock.x+ll, frontBlock.y+l, 1))){
									if(left == ll)
										Bleft = true;
								//	break sloop;
								}
				//	break;
				}
			}
				
			if(Bleft == true) Log.e("BOX", "There is box left (" + left + ")");
			if(Bright == true) Log.e("BOX", "There is box right (" + right + ")");
				
			if(Bleft == true && Bright == true)
				if(right > left) return 0;
				else return 2;
			else if(Bleft == true) return 0;
			else if(Bright == true) return 2;
			else if(right > left && left != 0) return 0;
			else if(left > right && right != 0) return 2;
			else if(lastTurn.size() > 0) return lastTurn.get(lastTurn.size()-1);
			else return dir;	
		default:
			Log.e("hr.knezzz.snake.Game.sonar", "Something went wrong in sonar (Direction: " + direction + ")");
			return dir;
		}
	}*/
	
	public int sonar(int direction, Block frontBlock){
		int up = 0, down = 0, left = 0, right = 0;
		boolean Bup = false, Bdown = false, Bleft = false, Bright = false;
		int dir = direction;
		
		switch(direction){
		case 0:case 2://right
			for(int c = 1; c < snake.length/2; c++){
				//Check down
				if(snake.collides(new Block(frontBlock.x,frontBlock.y+c,1))){
					down=c;
					break;
				}
				//Check up
				if(snake.collides(new Block(frontBlock.x,frontBlock.y-c,1))){
					up = c;
					break;
				}
			}
			
			if(up > down && down != 0)
				return 3;
			else if(down > up && up != 0)
				return 1;
			else if(down == 0)
				return 1;
			else if(up == 0)
				return 3;
			else if(lastTurn.size() > 0)
				return lastTurn.get(lastTurn.size()-1);
			else
				return dir;
			
		case 1:case 3://down
			for(int c = 1; c < snake.length/2; c++){
				if(snake.collides(new Block(frontBlock.x+c,frontBlock.y,1))){
					right = c;
					break;
				}
				if(snake.collides(new Block(frontBlock.x-c,frontBlock.y,1))){
					left = c;
					break;
				}
			}
			
			if(right > left && left != 0)
				return 0;
			else if(left > right && right != 0)
				return 2;
			else if(left == 0)
				return 2;
			else if(right == 0)
				return 0;
			else if(lastTurn.size() > 0)
				return lastTurn.get(lastTurn.size()-1);
			else
				return dir;
		default:
			Log.e("hr.knezzz.snake.Game.sonar", "Something went wrong in sonar (Direction: " + direction + ")");
			return dir;
		}
	}
}
