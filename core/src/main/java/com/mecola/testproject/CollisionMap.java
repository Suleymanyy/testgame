package com.mecola.testproject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;

import java.util.ArrayList;
import java.util.List;

public class CollisionMap implements CollisionProvider {

    private static final String TAG = "CollisionMap";

    // polygon object layer
    private static final String LAYER_NAME = "Collisions";

    // tile layer with environment objects
    private static final String TILE_LAYER_NAME = "Environment";

    private final List<Polygon> polygons = new ArrayList<>();

    public CollisionMap(TiledMap map) {

        loadPolygonLayer(map);
        loadTileCollisions(map);

        Gdx.app.log(TAG, "Total polygons loaded: " + polygons.size());
    }

    /**
     * LOAD OBJECT LAYER POLYGONS
     */
    private void loadPolygonLayer(TiledMap map) {

        MapLayer layer = map.getLayers().get(LAYER_NAME);

        if (layer == null) {
            throw new RuntimeException(
                "[CollisionMap] Layer '" + LAYER_NAME + "' not found!"
            );
        }

        int loaded = 0;

        for (MapObject obj : layer.getObjects()) {

            Polygon polygon = parseObject(obj);

            if (polygon != null) {
                polygons.add(polygon);
                loaded++;
            }
        }

        Gdx.app.log(TAG,
            "Loaded object layer polygons: " + loaded);
    }

    /**
     * LOAD TILESET COLLISIONS
     */
    private void loadTileCollisions(TiledMap map) {

        MapLayer rawLayer =
            map.getLayers().get(TILE_LAYER_NAME);

        if (!(rawLayer instanceof TiledMapTileLayer)) {

            Gdx.app.log(TAG,
                "Tile collision layer not found: "
                    + TILE_LAYER_NAME);

            return;
        }

        TiledMapTileLayer tileLayer =
            (TiledMapTileLayer) rawLayer;

        int loaded = 0;

        for (int x = 0; x < tileLayer.getWidth(); x++) {

            for (int y = 0; y < tileLayer.getHeight(); y++) {

                TiledMapTileLayer.Cell cell =
                    tileLayer.getCell(x, y);

                if (cell == null || cell.getTile() == null) {
                    continue;
                }

                MapObjects objects =
                    cell.getTile().getObjects();

                for (MapObject obj : objects) {

                    if (!(obj instanceof PolygonMapObject)) {
                        continue;
                    }

                    Polygon polygon =
                        parseTileObject(
                            (PolygonMapObject)obj,
                            x * tileLayer.getTileWidth(),
                            y * tileLayer.getTileHeight()
                        );

                    if (polygon != null) {

                        polygons.add(polygon);
                        loaded++;
                    }
                }
            }
        }

        Gdx.app.log(TAG,
            "Loaded tile collision polygons: " + loaded);
    }

    /**
     * PARSE OBJECT LAYER POLYGON
     */
    private Polygon parseObject(MapObject obj) {

        if (!(obj instanceof PolygonMapObject)) {
            return null;
        }

        PolygonMapObject polyObj =
            (PolygonMapObject)obj;

        Polygon src = polyObj.getPolygon();

        float[] verts =
            src.getTransformedVertices().clone();

        return new Polygon(verts);
    }

    /**
     * PARSE TILE COLLISION POLYGON
     */
    private Polygon parseTileObject(
        PolygonMapObject obj,
        float offsetX,
        float offsetY
    ) {

        Polygon src = obj.getPolygon();

        float[] verts =
            src.getTransformedVertices().clone();

        // convert local tile coords
        // to world coords
        for (int i = 0; i < verts.length; i += 2) {

            verts[i] += offsetX;
            verts[i + 1] += offsetY;
        }

        return new Polygon(verts);
    }

    @Override
    public boolean canMoveTo(Polygon hitbox) {

        for (Polygon wall : polygons) {

            if (Intersector.overlapConvexPolygons(
                hitbox,
                wall
            )) {
                return false;
            }
        }

        return true;
    }

    public List<Polygon> getPolygons() {
        return polygons;
    }
}

