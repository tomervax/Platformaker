package com.tomer.platformaker.game;


import android.graphics.RectF;

import androidx.core.math.MathUtils;

import com.tomer.platformaker.Level;
import com.tomer.platformaker.Tile;

public class CollisionSolver {
    private final static float PADDING = 0.001f;
    public static boolean HORIZONTAL = true;
    public static boolean VERTICAL = false;

    public static boolean resolvePlayerTileMapCollisions(Player player, Level level, boolean orientation){
        int leftTile = MathUtils.clamp((int) ((player.x + PADDING) / level.TILE_SIZE), 0, level.WIDTH - 1);
        int rightTile = MathUtils.clamp((int) ((player.x + player.width - PADDING) / level.TILE_SIZE), 0, level.WIDTH - 1);
        int topTile = MathUtils.clamp((int) ((player.y + PADDING) / level.TILE_SIZE), 0, level.HEIGHT - 1);
        int bottomTile = MathUtils.clamp((int) ((player.y + player.height - PADDING) / level.TILE_SIZE), 0, level.HEIGHT - 1);

        if (orientation == VERTICAL) {
            int xTile = player.xVel < 0 ? leftTile : rightTile;
            for (int i = topTile; i <= bottomTile; i++) {
                if (level.tiles[i][xTile].solid == Tile.SOLID) {
                    player.x =  player.xVel < 0 ? (xTile+1) * level.TILE_SIZE : xTile * level.TILE_SIZE - player.width;
                    return true;
                }
            }
        } else {
            int yTile = player.yVel < 0 ? topTile : bottomTile;
            for (int j = leftTile; j <= rightTile; j++) {
                if (level.tiles[yTile][j].solid == Tile.SOLID) {
                    player.y = player.yVel < 0 ? (yTile+1) * level.TILE_SIZE : yTile * level.TILE_SIZE - player.height;
                    return true;
                } else if (level.tiles[yTile][j].solid == Tile.PLATFORM && player.yVel > 0 && player.y + player.height <= yTile * level.TILE_SIZE + player.yVel + PADDING) {
                    player.y = yTile * level.TILE_SIZE - player.height;
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean checkPlayerSpikeCollisions(Player player, Level level) {
        int leftTile = MathUtils.clamp((int) ((player.x + PADDING) / level.TILE_SIZE), 0, level.WIDTH - 1);
        int rightTile = MathUtils.clamp((int) ((player.x + player.width - PADDING) / level.TILE_SIZE), 0, level.WIDTH - 1);
        int topTile = MathUtils.clamp((int) ((player.y + PADDING) / level.TILE_SIZE), 0, level.HEIGHT - 1);
        int bottomTile = MathUtils.clamp((int) ((player.y + player.height - PADDING) / level.TILE_SIZE), 0, level.HEIGHT - 1);
        RectF playerRect = new RectF(player.x, player.y, player.x + player.width, player.y + player.height);
        for (int i = topTile; i <= bottomTile; i++) {
            for (int j = leftTile; j <= rightTile; j++) {
                if (level.tiles[i][j] == Tile.SPIKES &&
                        new RectF((j+(1-Tile.SPIKE_WIDTH_FACTOR)/2) * level.TILE_SIZE, (i+1-Tile.SPIKE_HEIGHT_FACTOR) * level.TILE_SIZE,
                                (j+1 - (1-Tile.SPIKE_WIDTH_FACTOR)/2) * level.TILE_SIZE, (i+1) * level.TILE_SIZE).intersect(playerRect)){
                    return true;
                }
            }
        }
        return false;
    }
}
