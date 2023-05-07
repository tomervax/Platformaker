package com.tomer.platformaker;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public enum Tile {
    AIR             (1, 0, null, -1, 1, 1, -1),

    STONE_BRICKS    (0, 1, TilePattern.GRASS_PATTERNS, 0, 3, 5, 0),
    WOOD            (0, 1, TilePattern.GRASS_PATTERNS, 0, 3, 5, 88),
    CYAN_STONE      (0, 1, TilePattern.GRASS_PATTERNS, 0, 3, 5, 176),
    GREEN_GRASS     (0, 1, TilePattern.GRASS_PATTERNS, 1, 3, 5, 6),
    ORANGE_GRASS    (0, 5, TilePattern.GRASS_PATTERNS, 1, 3, 5, 94),
    PINK_GRASS      (0, 6, TilePattern.GRASS_PATTERNS, 1, 3, 5, 182),
    BRICKS          (0, 1, TilePattern.GRASS_PATTERNS, 6, 3, 5, 105),

    BRONZE_BOX      (0, 1, TilePattern.BOX_PATTERNS, 4, 1, 4, 12),
    IRON_BOX        (0, 1, TilePattern.BOX_PATTERNS, 4, 1, 4, 100),
    COPPER_BOX      (0, 1, TilePattern.BOX_PATTERNS, 4, 1, 4, 188),
    GOLD_BOX        (0, 1, TilePattern.BOX_PATTERNS, 4, 1, 4, 193),

    GOLD_PLATFORM   (2, 0, TilePattern.PLATFORM_PATTERNS, 1, 1, 3, 17),
    WOOD_PLATFORM   (2, 0, TilePattern.PLATFORM_PATTERNS, 1, 1, 3, 39),
    IRON_PLATFORM   (2, 0, TilePattern.PLATFORM_PATTERNS, 1, 1, 3, 61),

    SPIKES          (1, 0, null, 0, 1,1, 5),

    PLAYER_SPAWN    (1, 0, TilePattern.SPAWN_GOAL_PATTERNS, 0, 2, 2, 47),
    PLAYER_GOAL     (1, 0, TilePattern.SPAWN_GOAL_PATTERNS, 0, 2, 2, 135);

    public final int solid;
    private final int precedence;
    private final TilePattern[] patterns;
    public final int icon, iconSize;
    private final int blockWidth, pos;
    Tile(int solid, int precedence, TilePattern[] patterns, int icon, int iconSize, int blockWidth, int pos) {
        this.solid = solid;
        this.precedence = precedence;
        this.patterns = patterns;
        this.icon = icon;
        this.iconSize = iconSize;
        this.blockWidth = blockWidth;
        this.pos = pos;
    }

    public static final int SOLID = 0, PLATFORM = 2;

    public static final int TILESET_ROWS = 11, TILESET_COLUMNS = 22, TILE_SIZE = 16;
    public static final Bitmap[] TILESET = new Bitmap[TILESET_ROWS * TILESET_COLUMNS];
    public static final Drawable[] ICONS = new Drawable[values().length];
    private static int IX(int x, int y) {
        return y * TILESET_COLUMNS + x;
    }
    public int IX(int pos) {
        int x = pos % blockWidth, y = pos / blockWidth;
        int bx = this.pos % TILESET_COLUMNS, by = this.pos / TILESET_COLUMNS;
        return IX(x+bx,y+by);
    }

    public static final float SPIKE_HEIGHT_FACTOR = 0.35f, SPIKE_WIDTH_FACTOR = 0.4f;

    public static void loadImages(Resources res){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap tileset = BitmapFactory.decodeResource(res, R.drawable.tileset, options);
        Bitmap tilesetImg = replaceAlpha(tileset).copy(Bitmap.Config.RGB_565, false);
        for (int i = 0; i < TILESET_ROWS; i++) {
            for (int j = 0; j < TILESET_COLUMNS; j++) {
                TILESET[IX(j, i)] = Bitmap.createBitmap(tilesetImg, j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
        //Set spikes to empty tile at 5,0
        Bitmap spikes = BitmapFactory.decodeResource(res, R.drawable.spikes, options);
        TILESET[IX(5, 0)] = replaceAlpha(spikes).copy(Bitmap.Config.RGB_565, false);

        int s = 32;
        //Set spawn to 4 empty tiles at 3,2
        Bitmap spawn = BitmapFactory.decodeResource(res, R.drawable.player_spawn, options);
        Bitmap SPAWN = replaceAlpha(spawn).copy(Bitmap.Config.RGB_565, false);
        TILESET[IX(3, 2)] = Bitmap.createBitmap(SPAWN, 0, 0, s, s);
        TILESET[IX(4, 2)] = Bitmap.createBitmap(SPAWN, s, 0, s, s);
        TILESET[IX(3, 3)] = Bitmap.createBitmap(SPAWN, 0, s, s, s);
        TILESET[IX(4, 3)] = Bitmap.createBitmap(SPAWN, s, s, s, s);
        //Set goal to 4 empty tiles at 3,6
        Bitmap goal = BitmapFactory.decodeResource(res, R.drawable.player_goal, options);
        Bitmap GOAL = replaceAlpha(goal).copy(Bitmap.Config.RGB_565, false);
        TILESET[IX(3, 6)] = Bitmap.createBitmap(GOAL, 0, 0, s, s);
        TILESET[IX(4, 6)] = Bitmap.createBitmap(GOAL, s, 0, s, s);
        TILESET[IX(3, 7)] = Bitmap.createBitmap(GOAL, 0, s, s, s);
        TILESET[IX(4, 7)] = Bitmap.createBitmap(GOAL, s, s, s, s);

        Tile[] values = values();
        for (int i = 1; i < values.length; i++){
            Tile tile = values[i];
            Bitmap icon;
            if (tile.patterns == TilePattern.GRASS_PATTERNS){
                icon = Bitmap.createBitmap(TILE_SIZE*2, TILE_SIZE*2, Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(icon);
                canvas.drawBitmap(TILESET[tile.IX(0)], 0, 0, null);
                canvas.drawBitmap(TILESET[tile.IX(2)], TILE_SIZE, 0, null);
                canvas.drawBitmap(TILESET[tile.IX(10)], 0, TILE_SIZE, null);
                canvas.drawBitmap(TILESET[tile.IX(12)], TILE_SIZE, TILE_SIZE, null);
            } else if (tile.patterns == TilePattern.BOX_PATTERNS){
                icon = TILESET[tile.IX(4)];
            } else if (tile.patterns == TilePattern.PLATFORM_PATTERNS) {
                int x = tile.pos % TILESET_COLUMNS, y = tile.pos / TILESET_COLUMNS;
                icon = Bitmap.createBitmap(tileset, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE*3, TILE_SIZE);
            } else if (tile == PLAYER_SPAWN) {
                icon = spawn;
            } else if (tile == PLAYER_GOAL) {
                icon = goal;
            } else if (tile == SPIKES) {
                icon = Bitmap.createBitmap(TILE_SIZE*2, TILE_SIZE, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(icon);
                canvas.drawBitmap(spikes,0,0,null);
                canvas.drawBitmap(spikes,TILE_SIZE,0,null);
                icon = spikes;
            } else {
                icon = TILESET[tile.IX(0)];
            }
            ICONS[i] = new BitmapDrawable(res, icon);
            ICONS[i].setFilterBitmap(false);
        }
    }

    private static Bitmap replaceAlpha(Bitmap src) {
        if(src == null) {
            return null;
        }
        // Source image size
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pixels = new int[width * height];
        //get pixels
        src.getPixels(pixels, 0, width, 0, 0, width, height);

        for(int x = 0; x < pixels.length; ++x) {
            pixels[x] = (Color.alpha(pixels[x]) == 0) ? DisplayView.BACKGROUND_COLOR : pixels[x];
        }
        // create result bitmap output
        Bitmap result = Bitmap.createBitmap(width, height, src.getConfig());
        //set pixels
        result.setPixels(pixels, 0, width, 0, 0, width, height);

        return result;
    }

    public static class TilePattern {
        public static final int PATTERN_SIZE = 9;
        public static final int ANY = 0, NONE = 1, TILE = 2;

        private final int[] pattern;
        public final int image;
        TilePattern(int[] pattern, int image){
            this.pattern = pattern;
            this.image = image;
        }

        private int match(int[] tiles){
            int strength = 0;
            for (int i = 0; i < PATTERN_SIZE; i++){
                if (pattern[i] != ANY) {
                    if (pattern[i] != tiles[i]) {
                        return 0;
                    }
                    strength++;
                } else if (tiles[i] == ANY){
                    strength++;
                }
            }
            return strength;
        }

        public static int getTileImage(Tile[][] map, int x, int y){
            if (map[y][x].patterns == null){
                return map[y][x].pos;
            }
            int[] tiles = new int[PATTERN_SIZE];
            int index = 0;
            for (int i = y-1; i <= y+1; i++){
                for (int j = x-1; j <= x+1; j++){
                    if (i < 0 || j < 0 || i >= map.length || j >= map[0].length){
                        tiles[index] = ANY;
                    } else {
                        if (map[i][j] == map[y][x]){
                            tiles[index] = TILE;
                        } else if (map[i][j].precedence > map[y][x].precedence){
                            tiles[index] = ANY;
                        } else {
                            tiles[index] = NONE;
                        }
                    }
                    index++;
                }
            }
            TilePattern bestPattern = null;
            int maxStrength = 0;
            for (TilePattern pattern: map[y][x].patterns) {
                int strength = pattern.match(tiles);
                if (strength > maxStrength) {
                    bestPattern = pattern;
                    maxStrength = strength;
                }
            }
            return bestPattern != null ? map[y][x].IX(bestPattern.image) : -1;
        }

        private static final TilePattern[] GRASS_PATTERNS = new TilePattern[]{
                new TilePattern(new int[]{
                        ANY, NONE, ANY,
                        NONE, TILE, ANY,
                        ANY, ANY, ANY,
                }, 0),
                new TilePattern(new int[]{
                        ANY, NONE, ANY,
                        NONE, TILE, TILE,
                        ANY, TILE, ANY,
                }, 0),
                new TilePattern(new int[]{
                        ANY, NONE, ANY,
                        ANY, TILE, ANY,
                        ANY, ANY, ANY,
                }, 1),
                new TilePattern(new int[]{
                        ANY, NONE, ANY,
                        NONE, TILE, NONE,
                        ANY, ANY, ANY,
                }, 1),
                new TilePattern(new int[]{
                        ANY, NONE, ANY,
                        TILE, TILE, TILE,
                        ANY, ANY, ANY,
                }, 1),
                new TilePattern(new int[]{
                        ANY, NONE, ANY,
                        ANY, TILE, NONE,
                        ANY, ANY, ANY,
                }, 2),
                new TilePattern(new int[]{
                        ANY, NONE, ANY,
                        TILE, TILE, NONE,
                        ANY, TILE, ANY,
                }, 2),
                new TilePattern(new int[]{
                        ANY, ANY, ANY,
                        ANY, TILE, TILE,
                        ANY, TILE, NONE,
                }, 3),
                new TilePattern(new int[]{
                        ANY, ANY, ANY,
                        TILE, TILE, ANY,
                        NONE, TILE, ANY,
                }, 4),
                new TilePattern(new int[]{
                        ANY, ANY, ANY,
                        NONE, TILE, ANY,
                        ANY, ANY, ANY,
                }, 5),
                new TilePattern(new int[]{
                        ANY, ANY, ANY,
                        ANY, TILE, ANY,
                        ANY, ANY, ANY,
                }, 6),
                new TilePattern(new int[]{
                        ANY, TILE, ANY,
                        NONE, TILE, NONE,
                        ANY, ANY, ANY,
                }, 6),
                new TilePattern(new int[]{
                        NONE, TILE, NONE,
                        TILE, TILE, TILE,
                        ANY, TILE, ANY,
                }, 6),
                new TilePattern(new int[]{
                        ANY, TILE, ANY,
                        TILE, TILE, TILE,
                        NONE, TILE, NONE,
                }, 6),
                new TilePattern(new int[]{
                        NONE, TILE, NONE,
                        TILE, TILE, TILE,
                        NONE, TILE, NONE,
                }, 6),
                new TilePattern(new int[]{
                        ANY, ANY, ANY,
                        ANY, TILE, NONE,
                        ANY, ANY, ANY,
                }, 7),
                new TilePattern(new int[]{
                        ANY, TILE, NONE,
                        ANY, TILE, TILE,
                        ANY, ANY, ANY,
                }, 8),
                new TilePattern(new int[]{
                        ANY, TILE, NONE,
                        ANY, TILE, TILE,
                        ANY, TILE, ANY,
                }, 8),
                new TilePattern(new int[]{
                        NONE, TILE, ANY,
                        TILE, TILE, ANY,
                        ANY, TILE, ANY,
                }, 9),
                new TilePattern(new int[]{
                        NONE, TILE, ANY,
                        TILE, TILE, ANY,
                        ANY, ANY, ANY,
                }, 9),
                new TilePattern(new int[]{
                        ANY, ANY, ANY,
                        NONE, TILE, ANY,
                        ANY, NONE, ANY,
                }, 10),
                new TilePattern(new int[]{
                        ANY, ANY, ANY,
                        ANY, TILE, ANY,
                        ANY, NONE, ANY,
                }, 11),
                new TilePattern(new int[]{
                        ANY, TILE, ANY,
                        NONE, TILE, NONE,
                        ANY, NONE, ANY,
                }, 11),
                new TilePattern(new int[]{
                        ANY, ANY, ANY,
                        ANY, TILE, NONE,
                        ANY, NONE, ANY,
                }, 12),
        };

        private static final TilePattern[] BOX_PATTERNS = new TilePattern[]{
                new TilePattern(new int[]{
                        ANY, ANY, ANY,
                        NONE, TILE, TILE,
                        ANY, ANY, ANY,
                }, 0),
                new TilePattern(new int[]{
                        ANY, ANY, ANY,
                        TILE, TILE, TILE,
                        ANY, ANY, ANY,
                }, 1),
                new TilePattern(new int[]{
                        ANY, ANY, ANY,
                        TILE, TILE, NONE,
                        ANY, ANY, ANY,
                }, 2),
                new TilePattern(new int[]{
                        ANY, ANY, ANY,
                        ANY, TILE, ANY,
                        ANY, ANY, ANY,
                }, 4),
                new TilePattern(new int[]{
                        ANY, ANY, ANY,
                        ANY, TILE, ANY,
                        ANY, TILE, ANY,
                }, 3),
                new TilePattern(new int[]{
                        TILE, TILE, TILE,
                        NONE, TILE, NONE,
                        NONE, TILE, NONE,
                }, 3),
                new TilePattern(new int[]{
                        ANY, TILE, ANY,
                        ANY, TILE, ANY,
                        ANY, TILE, ANY,
                }, 7),
                new TilePattern(new int[]{
                        ANY, TILE, ANY,
                        ANY, TILE, ANY,
                        ANY, ANY, ANY,
                }, 11),
                new TilePattern(new int[]{
                        NONE, TILE, NONE,
                        NONE, TILE, NONE,
                        TILE, TILE, ANY,
                }, 11),
        };

        private static final TilePattern[] PLATFORM_PATTERNS = new TilePattern[]{
                new TilePattern(new int[]{
                        ANY, ANY, ANY,
                        NONE, TILE, TILE,
                        ANY, ANY, ANY,
                }, 0),

                new TilePattern(new int[]{
                        ANY, ANY, ANY,
                        ANY, TILE, ANY,
                        ANY, ANY, ANY,
                }, 1),

                new TilePattern(new int[]{
                        ANY, ANY, ANY,
                        TILE, TILE, NONE,
                        ANY, ANY, ANY,
                }, 2),
        };

        private static final TilePattern[] SPAWN_GOAL_PATTERNS = new TilePattern[]{
                new TilePattern(new int[]{
                        ANY, ANY, ANY,
                        ANY, TILE, TILE,
                        ANY, TILE, TILE,
                }, 0),

                new TilePattern(new int[]{
                        ANY, ANY, ANY,
                        TILE, TILE, ANY,
                        TILE, TILE, ANY,
                }, 1),

                new TilePattern(new int[]{
                        ANY, TILE, TILE,
                        ANY, TILE, TILE,
                        ANY, ANY, ANY,
                }, 2),

                new TilePattern(new int[]{
                        TILE, TILE, ANY,
                        TILE, TILE, ANY,
                        ANY, ANY, ANY,
                }, 3),
        };
    }
}