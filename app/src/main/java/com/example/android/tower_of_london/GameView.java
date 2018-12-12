package com.example.android.tower_of_london;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.android.tower_of_london.MainActivity.currentLevel;

public class GameView extends View {
    protected int moveCount;

    private class Item {
        float x, y;
        int color;
        boolean isFrame;

        Item(float x, float y, int color, boolean isFrame) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.isFrame = isFrame;
        }
    }

    private class Animation {
        Item item;
        float startX, startY;
        float endX, endY;
        long numFrames, count;
        GameView view;
        Runnable finishCallback;

        Animation(GameView view, Item item) {
            this.view = view;
            this.item = item;
            this.startX = item.x;
            this.startY = item.y;
            this.endX = item.x;
            this.endY = item.y;
            this.numFrames = 80;
            this.finishCallback = null;
            this.count = 0;
        }

        Animation setStart(float x, float y) {
            startX = x;
            startY = y;
            return this;
        }

        Animation setEnd(float x, float y) {
            endX = x;
            endY = y;
            return this;
        }

        Animation onFinish(Runnable finishCallback) {
            this.finishCallback = finishCallback;
            return this;
        }

        void start() {
            step();
        }

        private void step() {
            if (count != numFrames) {
                float t = (float) count / numFrames;
                float s = 3*t*t - 2*t*t*t;
                item.x = startX + s * (endX - startX);
                item.y = startY + s * (endY - startY);
                count += 1;
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        step();
                        view.invalidate();
                    }
                }, 3);
            } else {
                if (finishCallback != null) finishCallback.run();
            }
        }
    }

    private class AnimationManager {


    }


    Game game;
    ArrayList<Item> items = new ArrayList<>();
    int[] colorMap;
    Item[] blockMap;
    int selectedTower = -1; // -1 not selected; larger than 0 : choose which column

    AlertDialog.Builder dialogBuilder;
    Timer timer = new Timer();


    public GameView(final Context context, AttributeSet attrs) {
        super(context, attrs);

        game = new Game(currentLevel);
        colorMap = new int[game.getBlockNum()];
        blockMap = new Item[game.getBlockNum()];
        Random rand = new Random();
        for (int i = 0; i < colorMap.length; i++) {
            colorMap[i] = Color.argb(255, rand.nextInt(32)*8, rand.nextInt(32)*8, rand.nextInt(32)*8);
        }
        for (int i = 0; i < Game.TOWER_NUM; i++) {
            ArrayList<Integer> targets = game.getTargetsAt(i);
            for (int j = 0; j < targets.size(); j++) {
                items.add(createItem(targets.get(j), i, j,true));
            }
        }
        for (int i = 0; i < Game.TOWER_NUM; i++) {
            ArrayList<Integer> blocks = game.getBlocksAt(i);
            for (int j = 0; j < blocks.size(); j++) {
                Item temp = createItem(blocks.get(j), i, j,false);
                items.add(temp);
                blockMap[blocks.get(j)] = temp;
            }
        }
        dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setMessage("You winned! Moves: " + moveCount)
                .setPositiveButton("Restart", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(context, GameActivity.class);
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton("Return", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);
                    }
                });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawColumn(canvas);
        for (Item item : items) {
            drawItem(canvas, item);
        }
    }

    public void drawItem(Canvas canvas, Item item) {
        Paint p = new Paint();
        p.setColor(item.color);
        if (item.isFrame) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(10);
        } else {
            p.setAlpha(150);
        }
        float width = 0.1f;
        float height = 0.1f;
        canvas.drawRect(
                scaleX(item.x-width/2), scaleY(item.y-height/2),
                scaleX(item.x+width/2), scaleY(item.y+height/2),
                p
        );
    }

    public void drawColumn(Canvas canvas) {
        Paint p = new Paint();
        p.setStrokeWidth(20);
        canvas.drawLine(scaleX(0.25f), scaleY(0.8f), scaleX(0.25f), scaleY(0.2f), p);
        canvas.drawLine(scaleX(0.50f), scaleY(0.8f), scaleX(0.50f), scaleY(0.2f), p);
        canvas.drawLine(scaleX(0.75f), scaleY(0.8f), scaleX(0.75f), scaleY(0.2f), p);
        canvas.drawRect(scaleX(0.20f), scaleY(0.80f), scaleX(0.80f), scaleY(0.90f), p);
    }

    private float scaleX(float x) {
        return x * getWidth();
    }

    private float scaleY(float y) {
        return y * getHeight();
    }

    private float invScaleX(float x) {
        return (float) x / getWidth();
    }

    private float invScaleY(float y) { return (float) y / getHeight(); }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (0.2 < invScaleX(event.getX()) && invScaleX(event.getX()) < 0.3 ) {
                handleTower(0);
            } else if (0.45 < invScaleX(event.getX()) && invScaleX(event.getX()) < 0.55) {
                handleTower(1);
            } else if (0.7 <invScaleX(event.getX()) && invScaleX(event.getX()) < 0.8  ) {
                handleTower(2);
            }
            invalidate();
            return true;
        } else {
            return false;
        }
    }

    public void handleTower(int index) {
        if (selectedTower == -1 && !game.isTowerEmpty(index)) { // select tower
            Item item = blockMap[game.getTopBlockAt(index)];
            float midX = 0.25f * (index + 1), midY = 0.1f;
            float endX = 0.5f, endY = 0.1f;
            if (index == 1) {
                startOneStepAnimation(item, endX, endY);
            } else {
                startTwoStepsAnimation(item, midX, midY, endX, endY);
            }
            selectedTower = index;
        } else if (selectedTower == index) { //put back
            Item item = blockMap[game.getTopBlockAt(index)];
            float endX = 0.25f * (index + 1);
            float endY = 0.75f - (game.getBlocksAt(index).size() - 1) * 0.1f;
            startOneStepAnimation(item, endX, endY);
            selectedTower = -1;
        } else if (selectedTower != -1 && !game.isTowerFull(index)) {  // successful move
            Item item = blockMap[game.getTopBlockAt(selectedTower)];
            float midX = 0.25f * (index + 1);
            float midY = 0.1f;
            float endX = 0.25f * (index + 1);
            float endY = 0.75f - (game.getBlocksAt(index).size()) * 0.1f;
            startTwoStepsAnimation(item, midX, midY, endX, endY);
            game.move(selectedTower, index);
            selectedTower = -1;
            moveCount++;
            if (game.hasEnded()) {
                dialogBuilder.setMessage("You winned! Moves: " + moveCount);
                dialogBuilder.create().show();
            }

        }
    }

    private Item createItem(int id, int tower, int height, boolean isFrame) {
        return new Item(0.25f * (tower + 1), 0.75f - height * 0.1f, colorMap[id], isFrame);
    }

    private void startOneStepAnimation(Item item, float endX, float endY) {
        new Animation(this, item).setEnd(endX, endY).start();
    }

    private void startTwoStepsAnimation(final Item item, final float midX, final float midY, final float endX, final float endY) {
        final GameView thisView = this;
        new Animation(this, item).setEnd(midX, midY).onFinish(new Runnable() {
            @Override
            public void run() {
                new Animation(thisView, item).setStart(midX, midY).setEnd(endX, endY).start();
            }
        }).start();
    }
}
