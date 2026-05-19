package com.mecola.testproject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;

import java.util.ArrayList;
import java.util.List;

public class CollisionMap implements CollisionProvider {

    private static final String TAG = "CollisionMap";
    private static final String LAYER_NAME = "Collisions";

    private final List<Polygon> polygons = new ArrayList<>();

    public CollisionMap(TiledMap map) {

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

        Gdx.app.log(TAG, "Loaded polygons: " + loaded);
    }

    /**
     * RAW polygon +
     * старые простые трансформации
     * для проверки coordinate space.
     */
    private Polygon parseObject(MapObject obj) {

        if (!(obj instanceof PolygonMapObject)) {
            return null;
        }

        PolygonMapObject polyObj = (PolygonMapObject)obj;

        Polygon src = polyObj.getPolygon();

        float[] verts = src.getTransformedVertices().clone();

        // =========================================
        // SIMPLE MANUAL TRANSFORMS
        // =========================================

        float cosA = (float)Math.cos(Math.toRadians(45));
        float sinA = (float)Math.sin(Math.toRadians(45));

        for (int i = 0; i < verts.length; i += 2) {

            float x = verts[i];
            float y = verts[i + 1];

            // ROTATE 45°
            float rx = cosA * x + sinA * y;
            float ry = -sinA * x + cosA * y;

            verts[i] = rx;
            verts[i + 1] = ry;
        }

        // OFFSET X
        for (int i = 0; i < verts.length; i += 2) {
            verts[i] += 900f;
        }

        // OFFSET Y
        for (int i = 1; i < verts.length; i += 2) {
            verts[i] += 0f;
        }

        // OPTIONAL MIRROR
        /*
        for (int i = 1; i < verts.length; i += 2) {
            verts[i] = -verts[i];
        }
        */

        // =========================================

        return new Polygon(verts);
    }

    @Override
    public boolean canMoveTo(Polygon hitbox) {

        for (Polygon wall : polygons) {

            if (Intersector.overlapConvexPolygons(hitbox, wall)) {
                return false;
            }
        }

        return true;
    }

    public List<Polygon> getPolygons() {
        return polygons;
    }
}
