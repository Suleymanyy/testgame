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

    // Назва шару в Tiled — одне місце, легко змінити
    private static final String LAYER_NAME = "Collisions";

    private final List<Polygon> polygons = new ArrayList<>();


    public CollisionMap(TiledMap map) {
        MapLayer layer = map.getLayers().get(LAYER_NAME);
        if (layer == null) {
            throw new RuntimeException("[CollisionMap] Шар '" + LAYER_NAME + "' не знайдено!");
        }

        // ── Виводимо у консоль початок координат шару колізій ─────────────
        debugLayerOrigin(map, layer);

        int loaded = 0;
        for (MapObject obj : layer.getObjects()) {
            try {
                Polygon poly = parseObject(obj);
                if (poly != null) {
                    polygons.add(poly);
                    loaded++;
                }
            } catch (Exception e) {
                Gdx.app.error(TAG, "Помилка: " + e.getMessage());
            }
        }
        Gdx.app.log(TAG, "Завантажено полігонів: " + loaded);
    }

   /* public CollisionMap(TiledMap map) {
        // ─── Отримуємо висоту мапи для конвертації координат ───────────────
        int mapHeightTiles = map.getProperties().get("height", Integer.class);
        int tileHeight     = map.getProperties().get("tileheight", Integer.class);
        float mapHeightPx  = -400;//mapHeightTiles * tileHeight;


        MapLayer layer = map.getLayers().get(LAYER_NAME);
        if (layer == null) {
            throw new RuntimeException("[CollisionMap] Шар '" + LAYER_NAME + "' не знайдено!");
        }

        int loaded = 0;
        for (MapObject obj : layer.getObjects()) {
            try {
                Polygon poly = parseObject(obj, mapHeightPx);
                if (poly != null) {
                    polygons.add(poly);
                    loaded++;
                }
            } catch (Exception e) {
                Gdx.app.error(TAG, "Не вдалося завантажити об'єкт: " + e.getMessage());
            }
        }
        Gdx.app.log(TAG, "Завантажено полігонів: " + loaded
            + " | mapHeightPx=" + mapHeightPx);
    }
*/

  /*  private Polygon parseObject(MapObject obj, float mapHeightPx) {
        if (obj instanceof PolygonMapObject) {
            // Беремо трансформовані вершини (з позицією вже застосованою)
            float[] src = ((PolygonMapObject) obj).getPolygon().getTransformedVertices();
            float[] verts = src.clone();
            for (int i = 0; i < verts.length; i += 2) {
                float x = verts[i];
                float y = verts[i + 1];



                // 2. Мануальне коригування (спробуйте змінити ці числа на великі)
                verts[i]     = x + 100f;        // Зсув по горизонталі
                verts[i + 1] = y - 2f; // Зсув по вертикалі
            }

            for (int i = 1; i < verts.length; i += 2) {
                verts[i] = mapHeightPx - verts[i];
            }


            return new Polygon(verts);
        }
        return null;
    }
*/

    /**
     * Виводить у консоль координату (0,0) шару колізій у просторі графіки мапи.
     * offsetX/offsetY — це зміщення шару задане у Tiled (Layer Properties → offsetx/offsety).
     * Якщо зміщення не задано — обидва будуть 0.
     */
    private void debugLayerOrigin(TiledMap map, MapLayer layer) {
        // Зміщення самого шару відносно мапи (задається у Tiled)
        float layerOffsetX = layer.getProperties().get("offsetx", 0f, Float.class);
        float layerOffsetY = layer.getProperties().get("offsety", 0f, Float.class);

        // Розміри мапи у пікселях
        int mapHeightTiles = map.getProperties().get("height", Integer.class);
        int tileHeight     = map.getProperties().get("tileheight", Integer.class);
        float mapHeightPx  = (float)(mapHeightTiles * tileHeight);

        // Точка (0,0) шару у координатах Tiled → координати графіки LibGDX
        // LibGDX OrthogonalTiledMapRenderer рендерить Y знизу вгору,
        // тому graphicsY = mapHeightPx - tiledY
        float tiledX    = 0f + layerOffsetX;
        float tiledY    = 0f + layerOffsetY;
        float graphicsX = tiledX;
        float graphicsY = mapHeightPx - tiledY;

        Gdx.app.log(TAG, "─────────────────────────────────────────────");
        Gdx.app.log(TAG, "Шар '" + LAYER_NAME + "' origin (0,0) у Tiled    → (" + tiledX + ", " + tiledY + ")");
        Gdx.app.log(TAG, "Шар '" + LAYER_NAME + "' origin (0,0) у графіці  → (" + graphicsX + ", " + graphicsY + ")");
        Gdx.app.log(TAG, "mapHeightPx=" + mapHeightPx + "  layerOffset=(" + layerOffsetX + ", " + layerOffsetY + ")");
        Gdx.app.log(TAG, "─────────────────────────────────────────────");
    }

    private Polygon parseObject(MapObject obj) {
        if (obj instanceof PolygonMapObject) {
            float[] src = ((PolygonMapObject) obj).getPolygon().getTransformedVertices();
            float[] verts = new float[src.length];
            System.arraycopy(src, 0, verts, 0, src.length); // явна копія у новий масив

            // ── Поворот на 40° за годинниковою стрілкою навколо глобального (0,0) ──
            // Оскільки pivot = (0,0), dx=x, dy=y — множення прямо без зсуву
            // Матриця: x' = x·cos + y·sin,  y' = -x·sin + y·cos
            //float cosA = 0.70710678118f; // cos(45°) = sin(45°) = √2/2
            //float sinA = 0.70710678118f;
            float cosA = (float) Math.cos(Math.toRadians(45));
            float sinA = (float) Math.sin(Math.toRadians(45));
            for (int i = 0; i < verts.length; i += 2) {
                float x = verts[i];
                float y = verts[i + 1];
                verts[i]     =  cosA * x + sinA * y;
                verts[i + 1] = -sinA * x + cosA * y;
            }

            // ── Відзеркалення по осі X: множимо глобальні Y-координати на -1 ──
            // Геометрично: відображення відносно прямої Y=0 у світовому просторі
            //for (int i = 1; i < verts.length; i += 2) {
            //    verts[i] = -verts[i];
            //}

            // ── Зсув по X у "брудних" координатах (після повороту, простір Tiled) ──
            // X збігається між Tiled і LibGDX, але вершини вже повернуті на 45°,
            // тому зсув діє вздовж оригінальної осі X, а не LibGDX-осі
            for (int i = 0; i < verts.length; i += 2) {
                verts[i] += 900f;
            }
/// по у
            for (int i = 1; i < verts.length; i += 2) {
                verts[i] -= 0f;
            }

            return new Polygon(verts);
        }
        return null;
    }

    /**
     * Реалізація CollisionProvider.
     * Перевіряє чи переданий хітбокс перетинається з будь-яким полігоном колізії.
     */
    @Override
    public boolean canMoveTo(Polygon hitbox) {
        for (Polygon wall : polygons) {
            if (Intersector.overlapConvexPolygons(hitbox, wall)) {
                return false;
            }
        }
        return true;
    }

    public List<Polygon> getPolygons() { return polygons; }
}
