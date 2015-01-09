package hr.knezzz.snake.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import hr.knezzz.snake.R;
import hr.knezzz.snake.TitleScreen;

public class Game extends View {

    private boolean setupComplete = false;
    public int pxSquare, squaresWidth, squaresHeight, sqBorder = 0,
            padding = 0, color, boardSize, snakeLength;
    private static ArrayList<Block> walls;
    private static ArrayList<Block> titleWalls;
    ArrayList<Integer> lastTurn = new ArrayList<Integer>();
    public Snake snake;
    private Food food;
    private Random random;
    private TextView scoreView, prcent;
    private GameScreen mActivity;
    private TitleScreen tActivity;
    public boolean gameOver = false;
    public int score;//So I can put score on GAME OVER screen.
    public int frameRate, size;//Size is used to calculate percents.
    public boolean wall, AI, isGlitch;
    private boolean isTitle;
    private boolean gameOverCalled = false;

    public Game(Context context, TextView scoreView, TextView prcent,
                boolean wall, boolean glitch, int color, int speed, int size, int boardSize, boolean isTitle) {
        super(context);
        if(!isTitle)
            mActivity = (GameScreen) context;
        else
            tActivity = (TitleScreen) context;
        random = new Random();
        this.scoreView = scoreView;
        this.prcent = prcent;
        this.wall = wall;
        this.color = color;
        this.AI = !isTitle;//TODO: Remove AI from normal game
        this.frameRate = (((speed+1) * 15)/2)+1;
        this.boardSize = boardSize;
        this.snakeLength = size;
        this.isTitle = isTitle;
        this.isGlitch = glitch;

        Log.d("Game started speed", "- " + frameRate);

        if(Build.VERSION.SDK_INT >= 14 && !ViewConfiguration.get(getContext()).hasPermanentMenuKey()) {
            hideSystemUi();

            if (!isTitle) {
                mActivity.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if (visibility == 0) {
                            mHideHandler.postDelayed(mHideRunnable, 2000);
                        }
                    }
                });
            } else {
                tActivity.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if (visibility == 0) {
                            mHideHandler.postDelayed(mHideRunnable, 2000);
                        }
                    }
                });
            }
        }
    }

    // If User or AI Scores
    private void score() {
        score++;

        if(!isTitle)
            scoreView.setText(Integer.toString(this.score));
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
        if (!AI)
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
        } else if (gameOver && !gameOverCalled) {
            gameOverCalled = true;
            new Thread(new Runnable() {
                public void run() {
                    parent.postDelayed(new Runnable() {
                        public void run() {
                            Log.i("hr.knezzz.snake.game.Game", "gameOver called");
                            if(!isTitle)
                                mActivity.gameOver();
                            else
                                tActivity.gameOver();
                        }
                    }, 500);
                }
            }).start();
        }
    }

    public void changeOptions(boolean walls, int speed, boolean glitch){
        this.wall = walls;
        this.frameRate = (((speed+1) * 15)/2)+1;
        this.isGlitch = glitch;
    }

    // Setup View
    public void setup() {
        gameOver = false;
        gameOverCalled = false;

        // Calculate Width of View in Inches
        int pxWidth = getWidth();
        int pxHeight = getHeight();
       // int dpi = (int) (0.4 * getResources().getDisplayMetrics().densityDpi);
   //     float cmWidth = ((float) pxWidth) / dpi;
   //     float cmHeight = ((float) pxHeight) / dpi;

    //    double incresWidth = ((cmWidth * 6) - (cmWidth*2)) / 100;
   //     double incresHeight = ((cmHeight * 6) - (cmHeight*2)) / 100;

      //  squaresWidth = 22;//(int) ((incresWidth*boardSize)+(cmWidth*2));
      //  squaresHeight = 39;//(int) ((incresHeight*boardSize)+(cmHeight*2));

        squaresWidth = 18;//(int) ((incresWidth*boardSize)+(cmWidth*2));
        squaresHeight = 32;//(int) ((incresHeight*boardSize)+(cmHeight*2));

/*
                squaresWidth = pxWidth / 2;// Double pixel mode
                squaresHeight = pxHeight / 2;

                // Pixel game. (PIXel Gamer). <<< First pixel game.
                squaresWidth = pxWidth;
                squaresHeight = pxHeight;
*/
        // Calculate Size of Squares
        int pxSquareWidth = pxWidth / squaresWidth;
        int pxSquareHeight = pxHeight / squaresHeight;

        if (pxSquareWidth > pxSquareHeight)
            pxSquare = pxSquareHeight; // Extra Space on Sides
        else
            pxSquare = pxSquareWidth; // Extra Space on Top

        // Calculate Padding Around & Between Squares
        padding = squaresWidth / 20;

        //This makes snake padding on blocks
        sqBorder = pxSquare / 22; //Original 25.

        // Build List of Wall Objects
        walls = new ArrayList<Block>();
        for (int j = 0; j < squaresWidth + 1; j++) {
            walls.add(new Block(j, -1, 0)); // Top Walls
            walls.add(new Block(j - 1, squaresHeight, 0)); // Bottom Walls
        }
        for (int j = 0; j < (squaresHeight + 1); j++) { // Left Walls
            walls.add(new Block(-1, j - 1, 0)); // Left Walls
            walls.add(new Block(squaresWidth, j, 0)); // Right Walls
        }

        if(isTitle){
            titleWalls = new ArrayList<Block>();
            for(int x = 2; x < 16; x++) {
                titleWalls.add(new Block(x, 11, 5)); //Top walls
                titleWalls.add(new Block(x, 20, 5)); //Bottom walls
                for (int y = 12; y < 20; y++) {
                    if(x>2&&x<16)
                        titleWalls.add(new Block(x,y, 4));//Inside
                }
            }

            for (int y = 12; y < 20; y++) {
                titleWalls.add(new Block(2, y, 5)); //Left walls
                titleWalls.add(new Block(15, y, 5)); //Right walls
            }
        }

        // Create Snake
        snake = new Snake();

        // Create Food
        if(!isTitle)
            food = new Food(snake, walls);
        else
            food = new Food(snake, walls, titleWalls);

        // Reset Score
        size = squaresHeight * squaresWidth;
        score = -1;
        score();

        setupComplete = true;
    }

    // Snake Object contains a list of blocks, knows if it is moving and
    // which direction it is moving
    public class Snake {

        public ArrayList<Block> blocks;
        private int direction, length;
        public boolean stopped = false;
      //  private int size = 0;

        // Create Snake
        public Snake() {

            // Create Leading Block
            blocks = new ArrayList<Block>();
            if(!isTitle)
                blocks.add(new Block(squaresWidth / 2, squaresHeight / 2, 1));
            else {
                double random = Math.random();
                if(random < 0.5)
                    blocks.add(new Block(squaresWidth / 2, squaresHeight / 4, 1));
                else
                    blocks.add(new Block(squaresWidth / 2, (squaresHeight / 6)* 5, 1));
            }

            length = snakeLength;

            // Calculate Random Initial Direction and Add 2 Remaining Blocks
            direction = random.nextInt(4);
        /*    for (int i = 1; i < length; i++) {
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
            }*/

            if(!isTitle) {
                LinearLayout layout = (LinearLayout) mActivity
                        .findViewById(R.id.controls);
                if (direction == 2 || direction == 0)
                    layout.setOrientation(LinearLayout.VERTICAL);
                else
                    layout.setOrientation(LinearLayout.HORIZONTAL);
            }
        }

        // Move & Draw Snake
        public void draw(Canvas canvas) {
            if (!stopped)
                move();
            for (Block block : blocks)
                block.draw(canvas);
        }

        //Testing for AI--
        public Block getHead() {
            return blocks.get(0);
        }
/*
        public int getDirection() {
            return direction;
        }*/

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
            if ((newBlock.collides(walls) && !wall) && !newBlock.collides(titleWalls)) {// Going though walls.
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
            if (this.collides(newBlock) || (newBlock.collides(walls) && wall) || (newBlock.collides(titleWalls) && isTitle)) {
                stopped = true;
                for (Block block : blocks) {
                    //Painting snake when it dies (Food remains black, head and tail stay unchanged, everything else is red)
                    if (block.getType() != Color.BLACK && block != getHead() && block != blocks.get(blocks.size() - 1))
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
                    food.getType();
                    length = length + 2;
                    newBlock.setType(2);
                    score();
                    // If No Collision with Food, Remove Last Block
                } else if (blocks.size() > length)
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

            if(isGlitch) {
                int glitch = (int) (Math.random() * 20);

                shape = new ShapeDrawable(new RectShape());
                shape.setBounds(glitch + x * pxSquare + sqBorder, glitch + y
                        * pxSquare + sqBorder, glitch + (x + 1) * pxSquare
                        - sqBorder, glitch + (y + 1) * pxSquare - sqBorder);
            }else{
                shape = new ShapeDrawable(new RectShape());
                shape.setBounds(padding + x * pxSquare + sqBorder, padding + y
                        * pxSquare + sqBorder, padding + (x + 1) * pxSquare
                        - sqBorder, padding + (y + 1) * pxSquare - sqBorder);
            }

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

        public void setType(int type) {
            switch (type) {
                case 0: // If Wall
                    if (wall)
                        shape.getPaint().setColor(Color.argb(200, 200, 200, 200));
                    else
                        shape.getPaint().setColor(Color.argb(40, 150, 150, 150));
                    break;
                case 1: // If Snake
                        shape.getPaint().setColor(color);
                    break;
                case 2: // If Food
                    shape.getPaint().setColor(Color.BLACK);
                    break;
                case 3: // If Collision
                    shape.getPaint().setColor(Color.argb(150, 255, 20, 20));
                    break;
                case 4:
                    shape.getPaint().setColor(Color.TRANSPARENT);
                    break;
                case 5:
                    shape.getPaint().setColor(Color.argb(255, 143, 143, 143));
            }
        }

        //Testing XXX
        public int getType() {
            return shape.getPaint().getColor();
        }
    }

    class Food extends Block {

        public Food(Snake snake, ArrayList<Block> blocks) {
            shape = new ShapeDrawable(new RectShape());
            this.setType(2);
            this.move(snake, blocks);
        }

        public Food(Snake snake, ArrayList<Block> blocks, ArrayList<Block> titleWalls) {
            shape = new ShapeDrawable(new RectShape());
            this.setType(2);
            blocks.addAll(titleWalls);
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

        public int getFoodX() {
            return x;
        }

        public int getFoodY() {
            return y;
        }
    }

    @SuppressLint("InlinedApi")
    public void hideSystemUi() {
        if(!isTitle)
            mActivity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            |View.SYSTEM_UI_FLAG_FULLSCREEN
                            |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        else
            tActivity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            |View.SYSTEM_UI_FLAG_FULLSCREEN
                            |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    Handler mHideHandler = new Handler();

    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideSystemUi();
        }
    };

    //Works.
    //TODO:Figure out how to not to box self in. Start from beginning?.
    public void AI() {
        if(!isTitle) {
            mActivity.findViewById(R.id.buttonLeftUp).setVisibility(View.GONE);
            mActivity.findViewById(R.id.buttonRightDown).setVisibility(View.GONE);
        }

        int fx = food.getFoodX();
        int fy = food.getFoodY();

        int sx = snake.getHead().x;
        int sy = snake.getHead().y;

        int newDirection = snake.direction;

        Block frontBlock = snake.blocks.get(0);
        Block newBlock;

        //Main function GOAL: Find food and position snake towards food.
        switch (newDirection) {
            case 0://Right
                if (fx > sx && !snake.collides(new Block(frontBlock.x + 1, frontBlock.y, 1))&& !new Block(frontBlock.x+1, frontBlock.y, 1).collides(walls) && !(isTitle && new Block(frontBlock.x+1, frontBlock.y, 1).collides(titleWalls)));
                else if (fx > sx && snake.collides(new Block(frontBlock.x + 1, frontBlock.y, 1))&& !new Block(frontBlock.x+1, frontBlock.y, 1).collides(walls) && !(isTitle && new Block(frontBlock.x+1, frontBlock.y, 1).collides(titleWalls)))
                    newDirection = betterDirection(snake.direction, frontBlock);
                else if (fx > sx && (snake.collides(new Block(frontBlock.x + 1, frontBlock.y + 1, 1)) || snake.collides(new Block(frontBlock.x + 1, frontBlock.y - 1, 1))))
                    newDirection = betterDirection(snake.direction, frontBlock);
                else {
                    if (fy == sy && (!snake.collides(new Block(frontBlock.x + 1, frontBlock.y + 1, 1)) || !snake.collides(new Block(frontBlock.x + 1, frontBlock.y - 1, 1))))
                        newDirection = betterDirection(snake.direction, frontBlock);
                    else if (fy > sy && !snake.collides(new Block(frontBlock.x + 1, frontBlock.y, 1))&& !new Block(frontBlock.x+1, frontBlock.y, 1).collides(walls) && !(isTitle && new Block(frontBlock.x+1, frontBlock.y, 1).collides(titleWalls)))
                        newDirection = 1;
                    else if (fy < sy && !snake.collides(new Block(frontBlock.x + 1, frontBlock.y, 1))&& !new Block(frontBlock.x+1, frontBlock.y, 1).collides(walls) && !(isTitle && new Block(frontBlock.x+1, frontBlock.y, 1).collides(titleWalls)))
                        newDirection = 3;
                    else {
                        if (lastTurn.size() > 0)
                            newDirection = lastTurn.get(lastTurn.size() - 1);
                        else
                            betterDirection(snake.direction, frontBlock);
                    }
                }
                break;
            case 1://Down
                if (fy > sy && !snake.collides(new Block(frontBlock.x, frontBlock.y + 1, 1))&& !new Block(frontBlock.x, frontBlock.y+1, 1).collides(walls) && !(isTitle && new Block(frontBlock.x, frontBlock.y+1, 1).collides(titleWalls)));
                else if (fy > sy && snake.collides(new Block(frontBlock.x, frontBlock.y + 1, 1))&& !new Block(frontBlock.x + 1, frontBlock.y, 1).collides(walls) && !(isTitle && new Block(frontBlock.x + 1, frontBlock.y, 1).collides(titleWalls)))
                    newDirection = betterDirection(snake.direction, frontBlock);
                else if (fy > sy && (snake.collides(new Block(frontBlock.x + 1, frontBlock.y + 1, 1)) || snake.collides(new Block(frontBlock.x - 1, frontBlock.y + 1, 1))))
                    newDirection = betterDirection(snake.direction, frontBlock);
                else {
                    if (fx == sx && (!snake.collides(new Block(frontBlock.x + 1, frontBlock.y + 1, 1)) || !snake.collides(new Block(frontBlock.x - 1, frontBlock.y + 1, 1))))
                        newDirection = betterDirection(snake.direction, frontBlock);
                    else if (fx > sx && !snake.collides(new Block(frontBlock.x, frontBlock.y + 1, 1)) && !new Block(frontBlock.x + 1, frontBlock.y, 1).collides(walls) && !(isTitle && new Block(frontBlock.x + 1, frontBlock.y, 1).collides(titleWalls)))
                        newDirection = 0;
                    else if (fx < sx && !snake.collides(new Block(frontBlock.x, frontBlock.y + 1, 1)) && !new Block(frontBlock.x + 1, frontBlock.y, 1).collides(walls) && !(isTitle && new Block(frontBlock.x + 1, frontBlock.y, 1).collides(titleWalls)))
                        newDirection = 2;
                    else {
                        if (lastTurn.size() > 0)
                            newDirection = lastTurn.get(lastTurn.size() - 1);
                        else
                            betterDirection(snake.direction, frontBlock);
                    }
                }
                break;
            case 2://Left
                if (fx < sx && !snake.collides(new Block(frontBlock.x - 1, frontBlock.y, 1))&& !new Block(frontBlock.x-1, frontBlock.y, 1).collides(walls) && !(isTitle && new Block(frontBlock.x-1, frontBlock.y, 1).collides(titleWalls)));
                else if (fx < sx && snake.collides(new Block(frontBlock.x - 1, frontBlock.y, 1))&& !new Block(frontBlock.x-1, frontBlock.y, 1).collides(walls) && !(isTitle && new Block(frontBlock.x-1, frontBlock.y, 1).collides(titleWalls)))
                    newDirection = betterDirection(snake.direction, frontBlock);
                else if (fx < sx && (snake.collides(new Block(frontBlock.x - 1, frontBlock.y + 1, 1)) || snake.collides(new Block(frontBlock.x - 1, frontBlock.y - 1, 1))))
                    newDirection = betterDirection(snake.direction, frontBlock);
                else {
                    if (fy == sy && (!snake.collides(new Block(frontBlock.x - 1, frontBlock.y + 1, 1)) || !snake.collides(new Block(frontBlock.x - 1, frontBlock.y - 1, 1))))
                        newDirection = betterDirection(snake.direction, frontBlock);
                    else if (fy > sy && !snake.collides(new Block(frontBlock.x - 1, frontBlock.y, 1))&& !new Block(frontBlock.x-1, frontBlock.y, 1).collides(walls) && !(isTitle && new Block(frontBlock.x-1, frontBlock.y, 1).collides(titleWalls)))
                        newDirection = 1;
                    else if (fy < sy && !snake.collides(new Block(frontBlock.x - 1, frontBlock.y, 1))&& !new Block(frontBlock.x-1, frontBlock.y, 1).collides(walls) && !(isTitle && new Block(frontBlock.x-1, frontBlock.y, 1).collides(titleWalls)))
                        newDirection = 3;
                    else {
                        if (lastTurn.size() > 0)
                            newDirection = lastTurn.get(lastTurn.size() - 1);
                        else
                            betterDirection(snake.direction, frontBlock);
                    }
                }
                break;
            case 3://Up
                if (fy < sy && !snake.collides(new Block(frontBlock.x, frontBlock.y - 1, 1))&& !new Block(frontBlock.x, frontBlock.y-1, 1).collides(walls) && !(isTitle && new Block(frontBlock.x, frontBlock.y-1, 1).collides(titleWalls)));
                else if (fy < sy && snake.collides(new Block(frontBlock.x, frontBlock.y - 1, 1))&& !new Block(frontBlock.x, frontBlock.y-1, 1).collides(walls) && !(isTitle && new Block(frontBlock.x, frontBlock.y-1, 1).collides(titleWalls)))
                    newDirection = betterDirection(snake.direction, frontBlock);
                else if (fy < sy && (snake.collides(new Block(frontBlock.x + 1, frontBlock.y - 1, 1)) || snake.collides(new Block(frontBlock.x - 1, frontBlock.y - 1, 1))))
                    newDirection = betterDirection(snake.direction, frontBlock);
                else {
                    if (fx == sx && (!snake.collides(new Block(frontBlock.x + 1, frontBlock.y - 1, 1)) || !snake.collides(new Block(frontBlock.x - 1, frontBlock.y - 1, 1))))
                        newDirection = betterDirection(snake.direction, frontBlock);
                    else if (fx > sx && !snake.collides(new Block(frontBlock.x, frontBlock.y - 1, 1)) && !new Block(frontBlock.x, frontBlock.y-1, 1).collides(walls) && !(isTitle && new Block(frontBlock.x, frontBlock.y-1, 1).collides(titleWalls)))
                        newDirection = 0;
                    else if (fx < sx && !snake.collides(new Block(frontBlock.x, frontBlock.y - 1, 1)) && !new Block(frontBlock.x, frontBlock.y-1, 1).collides(walls) && !(isTitle && new Block(frontBlock.x, frontBlock.y-1, 1).collides(titleWalls)))
                        newDirection = 2;
                    else {
                        if (lastTurn.size() > 0)
                            newDirection = lastTurn.get(lastTurn.size() - 1);
                        else
                            betterDirection(snake.direction, frontBlock);
                    }
                }
                break;
        }
        boolean gotDirection = false;

        for (int i = 0; i < 5; i++) {
            switch (newDirection) {
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

            if ((newBlock.collides(walls) && !wall) && (newBlock.collides(titleWalls) && !isTitle)) {
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

            if (!snake.collides(newBlock)) {
                if ((newBlock.collides(walls) && wall) || (newBlock.collides(titleWalls) && isTitle)) ;
                else {
                    int oldDirection = snake.direction;
                    if (oldDirection != newDirection) {
                        switch (newDirection) {
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

                        if (lastTurn.size() <= 1) {
                            lastTurn.add(newDirection);
                        } else if (oldDirection != newDirection) {
                            lastTurn.remove(0);
                            lastTurn.add(newDirection);
                        }
                    }
                    break;
                }
            } else if (!gotDirection) {
                switch (newDirection) {
                    case 0:
                        if (snake.collides(new Block(frontBlock.x, frontBlock.y + 1, 1)) || snake.collides(new Block(frontBlock.x, frontBlock.y - 1, 1)))
                            newDirection = 0;
                        if (snake.collides(new Block(frontBlock.x + 1, frontBlock.y, 1)))
                            newDirection = betterDirection(snake.direction, frontBlock);
                        break;
                    case 1:
                        if (snake.collides(new Block(frontBlock.x + 1, frontBlock.y, 1)) || snake.collides(new Block(frontBlock.x - 1, frontBlock.y, 1)))
                            newDirection = 1;
                        if (snake.collides(new Block(frontBlock.x, frontBlock.y + 1, 1)))
                            newDirection = betterDirection(snake.direction, frontBlock);
                        break;
                    case 2:
                        if (snake.collides(new Block(frontBlock.x, frontBlock.y + 1, 1)) || snake.collides(new Block(frontBlock.x, frontBlock.y - 1, 1)))
                            newDirection = 2;
                        if (snake.collides(new Block(frontBlock.x - 1, frontBlock.y, 1)))
                            newDirection = betterDirection(snake.direction, frontBlock);
                        break;
                    case 3:
                        if (snake.collides(new Block(frontBlock.x + 1, frontBlock.y, 1)) || snake.collides(new Block(frontBlock.x - 1, frontBlock.y, 1)))
                            newDirection = 3;
                        if (snake.collides(new Block(frontBlock.x, frontBlock.y - 1, 1)))
                            newDirection = betterDirection(snake.direction, frontBlock);
                        break;
                }

                gotDirection = true;
            } else {
                newDirection += 1;//TODO:Remove this with algorithm posted below. possibly with timer.
                if (newDirection > 3) newDirection = 0;
            }

        }
    }

    /*
     * TODO: make if inside switch to "see" trough walls, and adding switch from above to calculate where it has more space.
     * */
    public int betterDirection(int direction, Block frontBlock) {
        int dir = direction;
        int sonar = sonar(direction, frontBlock);

        switch (direction) {
            case 0:
            case 2:
                int crashesDown = 0, crashesUp = 0;

                if (snake.collides(new Block(frontBlock.x + 1, frontBlock.y + 1, 1)))
                    crashesDown++;
                if (snake.collides(new Block(frontBlock.x, frontBlock.y + 1, 1)))
                    crashesDown++;
                if (snake.collides(new Block(frontBlock.x - 1, frontBlock.y + 1, 1)))
                    crashesDown++;

                if (snake.collides(new Block(frontBlock.x + 1, frontBlock.y - 1, 1)))
                    crashesUp++;
                if (snake.collides(new Block(frontBlock.x, frontBlock.y - 1, 1)))
                    crashesUp++;
                if (snake.collides(new Block(frontBlock.x - 1, frontBlock.y - 1, 1)))
                    crashesUp++;

                if (crashesDown < crashesUp)
                    dir = 1;
                else if (crashesDown > crashesUp)
                    dir = 3;
                else
                    dir = direction;
                break;
            case 1:
            case 3:
                int crashesLeft = 0, crashesRight = 0;

                if (snake.collides(new Block(frontBlock.x + 1, frontBlock.y + 1, 1)))
                    crashesRight++;
                if (snake.collides(new Block(frontBlock.x + 1, frontBlock.y, 1)))
                    crashesRight++;
                if (snake.collides(new Block(frontBlock.x + 1, frontBlock.y - 1, 1)))
                    crashesRight++;

                if (snake.collides(new Block(frontBlock.x - 1, frontBlock.y + 1, 1)))
                    crashesLeft++;
                if (snake.collides(new Block(frontBlock.x - 1, frontBlock.y, 1)))
                    crashesLeft++;
                if (snake.collides(new Block(frontBlock.x - 1, frontBlock.y - 1, 1)))
                    crashesLeft++;

                if (crashesRight < crashesLeft)
                    dir = 0;
                else if (crashesRight > crashesLeft)
                    dir = 2;
                else
                    dir = direction;

                break;
            default:
                Log.e("hr.knezzz.snake.Game", "Something went wrong in betterDirection switch loop");
        }

        if (sonar == direction || sonar == dir)
            return dir;
        else if (dir == direction && sonar != direction)
            return sonar;
        else if (dir == direction && !lastTurn.isEmpty())
            return lastTurn.get(lastTurn.size() - 1);
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

    public int sonar(int direction, Block frontBlock) {
        int up = 0, down = 0, left = 0, right = 0;
        boolean Bup = false, Bdown = false, Bleft = false, Bright = false;
        int dir = direction;

        switch (direction) {
            case 0:
            case 2://right
                for (int c = 1; c < snake.length / 2; c++) {
                    //Check down
                    if (snake.collides(new Block(frontBlock.x, frontBlock.y + c, 1))) {
                        down = c;
                        break;
                    }
                    //Check up
                    if (snake.collides(new Block(frontBlock.x, frontBlock.y - c, 1))) {
                        up = c;
                        break;
                    }
                }

                if (up > down && down != 0)
                    return 3;
                else if (down > up && up != 0)
                    return 1;
                else if (down == 0)
                    return 1;
                else if (up == 0)
                    return 3;
                else if (lastTurn.size() > 0)
                    return lastTurn.get(lastTurn.size() - 1);
                else
                    return dir;

            case 1:
            case 3://down
                for (int c = 1; c < snake.length / 2; c++) {
                    if (snake.collides(new Block(frontBlock.x + c, frontBlock.y, 1))) {
                        right = c;
                        break;
                    }
                    if (snake.collides(new Block(frontBlock.x - c, frontBlock.y, 1))) {
                        left = c;
                        break;
                    }
                }

                if (right > left && left != 0)
                    return 0;
                else if (left > right && right != 0)
                    return 2;
                else if (left == 0)
                    return 2;
                else if (right == 0)
                    return 0;
                else if (lastTurn.size() > 0)
                    return lastTurn.get(lastTurn.size() - 1);
                else
                    return dir;
            default:
                Log.e("hr.knezzz.snake.Game.sonar", "Something went wrong in sonar (Direction: " + direction + ")");
                return dir;
        }
    }
}
