package com.mecola.testproject;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class TileCollisionSystem {

    private final TiledMapTileLayer collisionLayer;

    private final int tileWidth;
    private final int tileHeight;

    public TileCollisionSystem(TiledMap map, String layerName) {

        this.collisionLayer =
            (TiledMapTileLayer) map.getLayers().get(layerName);

        this.tileWidth = (int) collisionLayer.getTileWidth();
        this.tileHeight = (int) collisionLayer.getTileHeight();
    }

    public void resolveCollision(CollidableEntity entity) {

        float worldX = entity.getX();
        float worldY = entity.getY();

        int tileX = (int)(worldX / tileWidth);
        int tileY = (int)(worldY / tileHeight);

        if (isBlocked(tileX, tileY)) {

            entity.setPosition(
                entity.getPreviousX(),
                entity.getPreviousY()
            );
        }
    }

    public boolean isBlocked(int tileX, int tileY) {

        TiledMapTileLayer.Cell cell =
            collisionLayer.getCell(tileX, tileY);

        if (cell == null) {
            return false;
        }

        if (cell.getTile() == null) {
            return false;
        }

        MapProperties props =
            cell.getTile().getProperties();

        if (!props.containsKey("NotWalkable")) {
            return false;
        }

        return props.get("NotWalkable", Boolean.class);
    }
}
