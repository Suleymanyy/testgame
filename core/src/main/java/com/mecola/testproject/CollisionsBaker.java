package com.mecola.testproject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

public class CollisionsBaker {

    private HashMap<TiledMapTile, Pixmap> stampCache = new HashMap<>();
    private final int TILE_SIZE = 256; // Фіксований розмір твоїх 2D тайлів

    public void bake(String tmxPath) {
        try {
            TmxMapLoader loader = new TmxMapLoader();
            TiledMap tempMap = loader.load(tmxPath);

            int mapW = tempMap.getProperties().get("width", Integer.class);
            int mapH = tempMap.getProperties().get("height", Integer.class);
            int tileW = tempMap.getProperties().get("tilewidth", Integer.class);
            int tileH = tempMap.getProperties().get("tileheight", Integer.class);

            buildStampCache(tempMap);

            // Розмір фінальної картинки
            int pixWidth = (mapW + mapH) * (tileW / 2);
            int pixHeight = (mapW + mapH) * (tileH / 2);

            Pixmap mainPixmap = new Pixmap(pixWidth, pixHeight, Pixmap.Format.RGBA8888);
            mainPixmap.setColor(Color.DARK_GRAY);
            mainPixmap.fill();

            for (MapLayer layer : tempMap.getLayers()) {
                if (!(layer instanceof TiledMapTileLayer)) continue;
                TiledMapTileLayer tl = (TiledMapTileLayer) layer;

                for (int y = 0; y < tl.getHeight(); y++) {
                    for (int x = 0; x < tl.getWidth(); x++) {
                        TiledMapTileLayer.Cell cell = tl.getCell(x, y);
                        if (cell == null) continue;


                        int nx = x;
                        int ny = y;

                        // Стандартна ізометрична формула проекції
                        float screenX = (nx - ny) * (tileW / 2f) + (mapH - 1) * (tileW / 2f);
                        float screenY = (nx + ny) * (tileH / 2f);

                        //Малюємо білий ромб
                        mainPixmap.setColor(Color.WHITE);
                        drawTileRhombus(mainPixmap, screenX, screenY, tileW, tileH, pixHeight);

                        // 2. Малюємо штамп колізії поверх
                        TiledMapTile tile = cell.getTile();
                        if (tile != null && stampCache.containsKey(tile)) {
                            int targetX = (int) screenX;
                            // Перехід з Y-up (математика мапи) в Y-down (пікселі Pixmap)
                            int targetY = pixHeight - (int) (screenY + TILE_SIZE);

                            mainPixmap.setBlending(Pixmap.Blending.SourceOver);
                            mainPixmap.drawPixmap(stampCache.get(tile), targetX, targetY);
                        }
                    }
                }
            }

            PixmapIO.writePNG(Gdx.files.local("assets/collision_test.png"), mainPixmap);


            mainPixmap.dispose();
            for (Pixmap p : stampCache.values()) p.dispose();
            tempMap.dispose();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildStampCache(TiledMap map) {
        for (TiledMapTileSet tileSet : map.getTileSets()) {
            for (TiledMapTile tile : tileSet) {
                if (tile.getObjects().getCount() == 0) continue;

                Pixmap stamp = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
                stamp.setBlending(Pixmap.Blending.None);
                stamp.setColor(0, 0, 0, 0);
                stamp.fill();

                stamp.setColor(Color.RED);

                for (MapObject obj : tile.getObjects()) {
                    // Використовуємо локальні координати як вони є в Tiled
                    if (obj instanceof PolygonMapObject) {
                        drawLocalPolygon(stamp, (PolygonMapObject) obj);
                    } else if (obj instanceof RectangleMapObject) {
                        Rectangle r = ((RectangleMapObject) obj).getRectangle();
                        stamp.fillRectangle((int) r.x, (int) r.y, (int) r.width, (int) r.height);
                    }
                }
                stampCache.put(tile, stamp);
            }
        }
    }

    private void drawLocalPolygon(Pixmap stamp, PolygonMapObject polyObj) {
        Polygon poly = polyObj.getPolygon();
        float[] v = poly.getVertices();
        float ox = poly.getX();
        float oy = poly.getY();

        int n = v.length / 2;
        int[] x = new int[n];
        int[] y = new int[n];

        int minY = TILE_SIZE, maxY = 0;

        for (int i = 0; i < n; i++) {
            x[i] = (int) (v[i * 2] + ox);
            y[i] = (int) (v[i * 2 + 1] + oy);

            if (y[i] < minY) minY = y[i];
            if (y[i] > maxY) maxY = y[i];
        }
        fillPolygon(stamp, x, y, minY, maxY);
    }

    private void drawTileRhombus(Pixmap pixmap, float sx, float sy, int tw, int th, int ph) {
        int[] x = new int[4];
        int[] y = new int[4];

        // Точки ромба в системі координат, де Y росте вгору
        x[0] = (int) (sx + tw / 2f); y[0] = ph - (int) (sy + th);
        x[1] = (int) (sx + tw);      y[1] = ph - (int) (sy + th / 2f);
        x[2] = (int) (sx + tw / 2f); y[2] = ph - (int) (sy);
        x[3] = (int) (sx);           y[3] = ph - (int) (sy + th / 2f);

        int minY = y[0], maxY = y[0];
        for (int i = 1; i < 4; i++) {
            if (y[i] < minY) minY = y[i];
            if (y[i] > maxY) maxY = y[i];
        }
        fillPolygon(pixmap, x, y, minY, maxY);
    }

    private void fillPolygon(Pixmap pixmap, int[] x, int[] y, int minY, int maxY) {
        int n = x.length;
        for (int scanY = minY; scanY <= maxY; scanY++) {
            if (scanY < 0 || scanY >= pixmap.getHeight()) continue;
            Array<Integer> nodes = new Array<>();
            for (int i = 0; i < n; i++) {
                int next = (i + 1) % n;
                if ((y[i] <= scanY && y[next] > scanY) || (y[next] <= scanY && y[i] > scanY)) {
                    if (y[next] - y[i] != 0) {
                        nodes.add((int) (x[i] + (float) (scanY - y[i]) / (y[next] - y[i]) * (x[next] - x[i])));
                    }
                }
            }
            nodes.sort();
            for (int i = 0; i < nodes.size; i += 2) {
                if (i + 1 < nodes.size) {
                    int startX = Math.max(0, nodes.get(i));
                    int endX = Math.min(pixmap.getWidth() - 1, nodes.get(i + 1));
                    pixmap.drawLine(startX, scanY, endX, scanY);
                }
            }
        }
    }
}
