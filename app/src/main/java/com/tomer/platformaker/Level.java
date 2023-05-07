package com.tomer.platformaker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.Toast;

import androidx.core.math.MathUtils;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Level extends GameObject{
    public int WIDTH, HEIGHT;
    public final float TILE_SIZE = 80;

    public Tile[][] tiles;
    private int[][] images;

    private int playerSpawnX = -1, playerSpawnY = -1, playerGoalX = -1, playerGoalY = -1;

//    private int userRating = -1;
//    public int ratingCount = 0;
//    public float avgRating = 0;

    public String id, title, author;

    public Level(DisplayView displayView, int width, int height) {
        super(displayView,0,0);

        this.WIDTH = width;
        this.HEIGHT = height;

        this.width = WIDTH * TILE_SIZE;
        this.height = HEIGHT * TILE_SIZE;

        this.tiles = new Tile[height][width];
        this.images = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                tiles[i][j] = Tile.AIR;
                images[i][j] = -1;
            }
        }
    }

    public boolean hasGoal() {
        return playerGoalX != -1 && playerGoalY != -1;
    }

    public RectF getGoalRect() {
        return new RectF(playerGoalX * TILE_SIZE, (playerGoalY-1) * TILE_SIZE, (playerGoalX+2) * TILE_SIZE, (playerGoalY+1) * TILE_SIZE);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect visibleTiles = new Rect(
                MathUtils.clamp((int) (displayView.visibleArea.left / TILE_SIZE), 0, WIDTH - 1),
                MathUtils.clamp((int) (displayView.visibleArea.top / TILE_SIZE), 0, HEIGHT - 1),
                MathUtils.clamp((int) (displayView.visibleArea.right / TILE_SIZE), 0, WIDTH - 1),
                MathUtils.clamp((int) (displayView.visibleArea.bottom / TILE_SIZE), 0, HEIGHT - 1)
        );
        for (int i = visibleTiles.top; i <= visibleTiles.bottom; i++){
            float y = i * TILE_SIZE;
            for (int j = visibleTiles.left; j <= visibleTiles.right; j++){
                float x = j * TILE_SIZE;
                if (images[i][j] != -1){
                    canvas.drawBitmap(Tile.TILESET[images[i][j]], null, displayView.toScreenCoors(x, y, x + TILE_SIZE, y + TILE_SIZE), null);
                }
            }
        }
    }

    public Bitmap drawLevelImage() {
        int dim = Math.min(WIDTH, HEIGHT);
        Bitmap image = Bitmap.createBitmap(dim * Tile.TILE_SIZE, dim * Tile.TILE_SIZE, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(image);
        canvas.drawColor(DisplayView.BACKGROUND_COLOR);
        for (int i = 0; i < dim; i++){
            float y = i * Tile.TILE_SIZE;
            for (int j = 0; j < dim; j++){
                float x = j * Tile.TILE_SIZE;
                if (images[i][j] != -1){
                    canvas.drawBitmap(Tile.TILESET[images[i][j]], null, new RectF(x, y, x+ Tile.TILE_SIZE, y + Tile.TILE_SIZE), null);
                }
            }
        }
        return image;
    }

    public float getPlayerSpawnX(){
        return playerSpawnX * TILE_SIZE;
    }

    public float getPlayerSpawnY(){
        return playerSpawnY * TILE_SIZE;
    }

    public boolean setTile(int x, int y, Tile tile, boolean override) {
        if (y < 0 || x < 0 || y >= HEIGHT || x >= WIDTH){
            return false;
        }
        if (tiles[y][x] != tile) {
            boolean removeSpawnTiles = false, removeGoalTiles = false;
            if (tiles[y][x] == Tile.PLAYER_SPAWN && override){
                playerSpawnX = -1;
                playerSpawnY = -1;
                removeSpawnTiles = true;
            }

            if (tiles[y][x] == Tile.PLAYER_GOAL && override){
                playerGoalX = -1;
                playerGoalY = -1;
                removeGoalTiles = true;
            }
            tiles[y][x] = tile;
            for (int i = y - 1; i <= y + 1; i++) {
                for (int j = x - 1; j <= x + 1; j++) {
                    if (i >= 0 && j >= 0 && i < HEIGHT && j < WIDTH) {
                        if (removeSpawnTiles && tiles[i][j] == Tile.PLAYER_SPAWN) {
                            tiles[i][j] = Tile.AIR;
                        } else if (removeGoalTiles && tiles[i][j] == Tile.PLAYER_GOAL) {
                            tiles[i][j] = Tile.AIR;
                        }

                        if (tiles[i][j] == Tile.AIR) {
                            images[i][j] = -1;
                        } else {
                            images[i][j] = Tile.TilePattern.getTileImage(tiles, j, i);
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void solveTileImages(){
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (tiles[i][j] == Tile.AIR){
                    images[i][j] = -1;
                } else {
                    images[i][j] = Tile.TilePattern.getTileImage(tiles, j, i);
                }
            }
        }
    }

    public void resize(int width, int height){
        Tile[][] old_tiles = this.tiles;
        this.tiles = new Tile[height][width];
        this.images = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (j < this.WIDTH && i < this.HEIGHT){
                    tiles[i][j] = old_tiles[i][j];
                } else {
                    tiles[i][j] = Tile.AIR;
                }
                images[i][j] = -1;
            }
        }
        this.WIDTH = width;
        this.HEIGHT = height;
        this.width = WIDTH * TILE_SIZE;
        this.height = HEIGHT * TILE_SIZE;
        solveTileImages();
    }

    public boolean setSpawn(int x, int y){
        if (y < 1 || x < 0 || y >= HEIGHT || x >= WIDTH -1){
            Toast.makeText(displayView.getContext(),"Invalid player spawn location",Toast.LENGTH_SHORT).show();
            return false;
        }
        if (playerSpawnX != -1 && playerSpawnY != -1) {
            setTile(playerSpawnX, playerSpawnY, Tile.AIR, false);
            setTile(playerSpawnX+1, playerSpawnY, Tile.AIR, false);
            setTile(playerSpawnX, playerSpawnY-1, Tile.AIR, false);
            setTile(playerSpawnX+1, playerSpawnY-1, Tile.AIR, false);
        }
        setTile(x, y, Tile.PLAYER_SPAWN, false);
        setTile(x+1, y, Tile.PLAYER_SPAWN, false);
        setTile(x, y-1, Tile.PLAYER_SPAWN, false);
        setTile(x+1, y-1, Tile.PLAYER_SPAWN, false);
        playerSpawnX = x;
        playerSpawnY = y;
        return true;
    }

    public boolean setGoal(int x, int y){
        if (y < 1 || x < 0 || y >= HEIGHT || x >= WIDTH -1 && displayView != null){
            Toast.makeText(displayView.getContext(),"Invalid player goal location",Toast.LENGTH_SHORT).show();
            return false;
        }
        if (playerGoalX != -1 && playerGoalY != -1) {
            setTile(playerGoalX, playerGoalY, Tile.AIR, false);
            setTile(playerGoalX+1, playerGoalY, Tile.AIR, false);
            setTile(playerGoalX, playerGoalY-1, Tile.AIR, false);
            setTile(playerGoalX+1, playerGoalY-1, Tile.AIR, false);
        }
        setTile(x, y, Tile.PLAYER_GOAL, false);
        setTile(x+1, y, Tile.PLAYER_GOAL, false);
        setTile(x, y-1, Tile.PLAYER_GOAL, false);
        setTile(x+1, y-1, Tile.PLAYER_GOAL, false);
        playerGoalX = x;
        playerGoalY = y;
        return true;
    }

    public HashMap<String, Object> serialize(){
        HashMap<String, Object> document = new HashMap<String,Object>();
        document.put("title", title);
        document.put("width", WIDTH);
        document.put("height", HEIGHT);
        document.put("author", MainActivity.username);
        document.put("spawn", playerSpawnX + ", " + playerSpawnY);
        document.put("goal", playerGoalX + ", " + playerGoalY);
        List<Long> tilemap = new ArrayList<>();
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                tilemap.add((long) tiles[i][j].ordinal());
            }
        }
        document.put("tiles", tilemap);
        return document;
    }

    public static Level deserialize(Map<String, Object> document, DisplayView displayView){
        Level level = new Level(displayView, ((Number)document.get("width")).intValue(), ((Number)document.get("height")).intValue());
        level.title = (String) document.get("title");
        level.author = (String) document.get("author");
        level.id = (String) document.get("id");
//        level.ratingCount = ((Number) document.get("ratingCount")).intValue();
//        level.avgRating = ((Number) document.get("avgRating")).floatValue();
//        level.userRating = ((Number) document.get("userRating")).intValue();
        Tile[] TILES = Tile.values();
        List<Number> tilemap = (List<Number>) document.get("tiles");
        for (int i = 0; i < level.HEIGHT; i++) {
            for (int j = 0; j < level.WIDTH; j++) {
                level.tiles[i][j] = TILES[tilemap.get(i * level.WIDTH + j).intValue()];
            }
        }
        String[] spawn = ((String) document.get("spawn")).split(", ");
        level.setSpawn(Integer.parseInt(spawn[0]), Integer.parseInt(spawn[1]));
        String[] goal = ((String) document.get("goal")).split(", ");
        if (Integer.parseInt(goal[0]) != -1) {
            level.setGoal(Integer.parseInt(goal[0]), Integer.parseInt(goal[1]));
        }
        level.solveTileImages();
        return level;
    }


    @Override
    public void update() {}
}
