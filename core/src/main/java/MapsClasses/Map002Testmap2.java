package MapsClasses;

import PlayersClasses.EntityKnight;
import PlayersClasses.EntityTestEnemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.ScreenUtils;

import com.mecola.testproject.CollisionMap;

public class Map002Testmap2 implements Screen {

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    private TiledMapTileLayer environmentLayer;

    private OrthographicCamera camera;
    private OrthographicCamera UIcamera;

    private SpriteBatch batch;

    private EntityKnight player;
    private EntityTestEnemy enemy;

    private CollisionMap collisionMap;

    private ShapeRenderer shapeRenderer;
    private boolean debug = true;

    @Override
    public void show() {

        map = new TmxMapLoader().load("Maps/map2.tmx");

        renderer = new OrthogonalTiledMapRenderer(map);

        environmentLayer =
            (TiledMapTileLayer) map.getLayers().get("Environment");

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1920, 1080);
        camera.zoom = 0.75f;

        UIcamera = new OrthographicCamera();
        UIcamera.setToOrtho(false, 1920, 1080);
        UIcamera.zoom = 0.75f;

        batch = new SpriteBatch();

        player = new EntityKnight();
        enemy = new EntityTestEnemy();

        try {
            collisionMap = new CollisionMap(map);

            player.setCollisionProvider(collisionMap);
            enemy.setCollisionProvider(collisionMap);

        } catch (Exception e) {
            Gdx.app.error("Map", "Collision error: " + e.getMessage());
        }

        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render(float delta) {

        enemy.updateAI(player, delta);
        enemy.update(delta);
        player.update(delta);

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        float playerX = player.getPosition().x;
        float playerY = player.getPosition().y;

        camera.position.set(playerX, playerY, 0);
        UIcamera.position.set(playerX, playerY, 0);

        camera.update();
        UIcamera.update();

        renderer.setView(camera);

        // =========================
        // BACKGROUND (GROUND + BELOW MAP LAYERS)
        // =========================
        renderer.render(new int[]{0, 1});

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        drawEnvironment(playerY);

        player.render(batch);
        enemy.render(batch);

        batch.end();

        // =========================
        // ABOVE LAYERS
        // =========================
        renderer.render(new int[]{3});

        // =========================
        // INPUT
        // =========================
        handleInput();

        // =========================
        // DEBUG
        // =========================
        drawDebug();
    }

    // =========================================================
    // CORE Y-SORT LOGIC (YOUR IDEA)
    // =========================================================

    private void drawEnvironment(float playerY) {

        float tileW = environmentLayer.getTileWidth();
        float tileH = environmentLayer.getTileHeight();

        int minX = 0;
        int minY = 0;
        int maxX = environmentLayer.getWidth();
        int maxY = environmentLayer.getHeight();

        // 1 PASS: BELOW PLAYER
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {

                TiledMapTileLayer.Cell cell = environmentLayer.getCell(x, y);
                if (cell == null || cell.getTile() == null) continue;

                float worldX = x * tileW;
                float worldY = y * tileH;

                float centerY = worldY + tileH * 0.5f;

                if (centerY < playerY) {
                    batch.draw(
                        cell.getTile().getTextureRegion(),
                        worldX,
                        worldY,
                        tileW,
                        tileH
                    );
                }
            }
        }

        // PLAYER is drawn OUTSIDE (in render)

        // 2 PASS: ABOVE PLAYER
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {

                TiledMapTileLayer.Cell cell = environmentLayer.getCell(x, y);
                if (cell == null || cell.getTile() == null) continue;

                float worldX = x * tileW;
                float worldY = y * tileH;

                float centerY = worldY + tileH * 0.5f;

                if (centerY >= playerY) {
                    batch.draw(
                        cell.getTile().getTextureRegion(),
                        worldX,
                        worldY,
                        tileW,
                        tileH
                    );
                }
            }
        }
    }

    // =========================================================
    // INPUT
    // =========================================================

    private void handleInput() {

        if (Gdx.input.justTouched()) {

            Vector3 pos = new Vector3(
                Gdx.input.getX(),
                Gdx.input.getY(),
                0
            );

            camera.unproject(pos);

            if (enemy.getClickHitbox() != null &&
                enemy.getClickHitbox().contains(pos.x, pos.y)) {

                player.setTargetEnemy(enemy);
                player.tryAttack(enemy);

            } else {

                player.buildPath(
                    player.getPosition(),
                    new Vector2(pos.x, pos.y)
                );
            }
        }
    }

    // =========================================================
    // DEBUG
    // =========================================================

    private void drawDebug() {

        if (!debug || collisionMap == null) return;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(Color.BLUE);

        for (Polygon p : collisionMap.getPolygons()) {
            shapeRenderer.polygon(p.getTransformedVertices());
        }

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.polygon(player.getHitbox().getTransformedVertices());

        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.polygon(enemy.getHitbox().getTransformedVertices());

        shapeRenderer.end();
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        batch.dispose();
        shapeRenderer.dispose();
    }
}
