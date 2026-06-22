package MapsClasses;

import PlayersClasses.EntityKnight;
import PlayersClasses.EntityTestEnemy;
import PlayersClasses.YSortable;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PointMapObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mecola.testproject.CollisionMap;

public class Map002Testmap2 implements Screen {

    // ─────────────────────────────────────────────────────────────────────
    // CONFIG
    // ─────────────────────────────────────────────────────────────────────

    private static final int ENEMY_COUNT = 5;

    private static final float ENEMY_START_X = 400;
    private static final float ENEMY_START_Y = 500;
    private static final float ENEMY_SPACING = 200;

    // ─────────────────────────────────────────────────────────────────────
    // MAP
    // ─────────────────────────────────────────────────────────────────────

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private TiledMapTileLayer environmentLayer;

    // ─────────────────────────────────────────────────────────────────────
    // CAMERA
    // ─────────────────────────────────────────────────────────────────────

    private OrthographicCamera camera;
    private OrthographicCamera UIcamera;

    // ─────────────────────────────────────────────────────────────────────
    // RENDER
    // ─────────────────────────────────────────────────────────────────────

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    // ─────────────────────────────────────────────────────────────────────
    // ENTITIES
    // ─────────────────────────────────────────────────────────────────────

    private EntityKnight player;

    private Array<EntityTestEnemy> enemies;
    private Array<Vector2> enemyPositions;



    // ── Все объекты участвующие в Y-sort ────────────────────────────────
    private Array<YSortable> ySortedEntities;

    // ─────────────────────────────────────────────────────────────────────
    // COLLISION
    // ─────────────────────────────────────────────────────────────────────

    private CollisionMap collisionMap;

    // ─────────────────────────────────────────────────────────────────────
    // DEBUG
    // ─────────────────────────────────────────────────────────────────────

    private boolean debug = true;

    // ─────────────────────────────────────────────────────────────────────
    // SHOW
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public void show() {

        loadMap();
        createCameras();
        createRenderers();
        createEntities();
        setupCollision();
        registerYSortEntities();
    }

    // ─────────────────────────────────────────────────────────────────────
    // INITIALIZATION
    // ─────────────────────────────────────────────────────────────────────

    private void loadMap() {

        map = new TmxMapLoader().load("Maps/map2.tmx");

        renderer = new OrthogonalTiledMapRenderer(map);

        environmentLayer =
            (TiledMapTileLayer) map.getLayers().get("Environment");
    }

    private void createCameras() {

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1920, 1080);
        camera.zoom = 0.75f;

        UIcamera = new OrthographicCamera();
        UIcamera.setToOrtho(false, 1920, 1080);
        UIcamera.zoom = 0.75f;
    }

    private void createRenderers() {

        batch = new SpriteBatch();

        shapeRenderer = new ShapeRenderer();
    }

    private void createEntities() {

        player = new EntityKnight();

        enemies = new Array<>();
        enemyPositions = new Array<>();

        MapLayer enemyLayer = map.getLayers().get("EnemyPositions");

        if (enemyLayer == null) {
            Gdx.app.error("Map", "Layer EnemyPositions not found");
            return;
        }

        // ── 1. Собираем ВСЕ позиции отдельно ─────────────────────────────
        for (MapObject object : enemyLayer.getObjects()) {

            if (!(object instanceof PointMapObject)) {
                continue;
            }

            Integer count = object.getProperties().get("Count", Integer.class);

            if (count == null) {
                continue;
            }

            float x = object.getProperties().get("x", Float.class);
            float y = object.getProperties().get("y", Float.class);

            enemyPositions.add(new Vector2(x, y));
        }

        // ── 2. Создаём врагов РОВНО по количеству позиций ────────────────
        for (int i = 0; i < enemyPositions.size; i++) {

            EntityTestEnemy enemy = new EntityTestEnemy();

            enemies.add(enemy);
        }

        // ── 3. Привязываем врагов к позициям ПО ИНДЕКСУ ──────────────────
        for (int i = 0; i < enemies.size; i++) {

            Vector2 pos = enemyPositions.get(i);

            enemies.get(i).getPosition().set(pos.x, pos.y);
        }

        Gdx.app.log(
            "Map",
            "Spawned enemies: " + enemies.size
        );
    }
    private void setupCollision() {

        try {

            collisionMap = new CollisionMap(map);

            player.setCollisionProvider(collisionMap);

            for (EntityTestEnemy enemy : enemies) {
                enemy.setCollisionProvider(collisionMap);
            }

        } catch (Exception e) {

            Gdx.app.error(
                "Map",
                "Collision error: " + e.getMessage()
            );
        }
    }

    private void registerYSortEntities() {

        ySortedEntities = new Array<>();

        ySortedEntities.add(player);

        for (EntityTestEnemy enemy : enemies) {
            ySortedEntities.add(enemy);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // RENDER
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public void render(float delta) {

        updateEntities(delta);

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        updateCameras();

        renderer.setView(camera);

        // ── Нижние слои ──────────────────────────────────────────────────
        renderer.render(new int[]{0, 1});

        // ── Y-sort render ────────────────────────────────────────────────
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        drawEnvironmentYSorted();

        batch.end();

        // ── Верхние слои ─────────────────────────────────────────────────
        renderer.render(new int[]{3});

        handleInput();

        drawDebug();
    }

    // ─────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────

    private void updateEntities(float delta) {

        for (EntityTestEnemy enemy : enemies) {

            enemy.updateAI(player, delta);

            enemy.update(delta);
        }

        player.update(delta);
    }

    private void updateCameras() {

        camera.position.set(
            player.getPosition().x,
            player.getPosition().y,
            0
        );

        UIcamera.position.set(
            player.getPosition().x,
            player.getPosition().y,
            0
        );

        camera.update();

        UIcamera.update();
    }

    // ─────────────────────────────────────────────────────────────────────
    // INPUT
    // ─────────────────────────────────────────────────────────────────────

    private void handleInput() {

        if (!Gdx.input.justTouched()) {
            return;
        }

        Vector3 pos = new Vector3(
            Gdx.input.getX(),
            Gdx.input.getY(),
            0
        );

        camera.unproject(pos);

        // ── Проверка клика по врагам ────────────────────────────────────
        for (EntityTestEnemy enemy : enemies) {

            if (enemy.getClickHitbox() != null &&
                enemy.getClickHitbox().contains(pos.x, pos.y)) {

                player.setTargetEnemy(enemy);

                player.tryAttack(enemy);

                return;
            }
        }

        // ── Иначе движение ──────────────────────────────────────────────
        player.buildPath(
            player.getPosition(),
            new Vector2(pos.x, pos.y)
        );
    }

    // ─────────────────────────────────────────────────────────────────────
    // Y SORT
    // ─────────────────────────────────────────────────────────────────────

    private static class RenderItem {

        // ── Tile ────────────────────────────────────────────────────────
        com.badlogic.gdx.graphics.g2d.TextureRegion tile;

        float tileX;
        float tileY;
        float tileW;
        float tileH;

        // ── Entity ──────────────────────────────────────────────────────
        YSortable entity;

        // ── Sort key ────────────────────────────────────────────────────
        float depthY;

        static RenderItem ofTile(
            com.badlogic.gdx.graphics.g2d.TextureRegion tex,
            float wx,
            float wy,
            float tw,
            float th
        ) {

            RenderItem item = new RenderItem();

            item.tile = tex;

            item.tileX = wx;
            item.tileY = wy;

            item.tileW = tw;
            item.tileH = th;

            item.depthY = wy + th * 0.5f;

            return item;
        }

        static RenderItem ofEntity(YSortable e) {

            RenderItem item = new RenderItem();

            item.entity = e;

            item.depthY = e.getDepthY();

            return item;
        }
    }

    private final Array<RenderItem> renderQueue =
        new Array<>(256);

    private void drawEnvironmentYSorted() {

        renderQueue.clear();

        float tileW = environmentLayer.getTileWidth();
        float tileH = environmentLayer.getTileHeight();

        int maxX = environmentLayer.getWidth();
        int maxY = environmentLayer.getHeight();

        // ── Tiles ───────────────────────────────────────────────────────
        for (int x = 0; x < maxX; x++) {

            for (int y = 0; y < maxY; y++) {

                TiledMapTileLayer.Cell cell =
                    environmentLayer.getCell(x, y);

                if (cell == null || cell.getTile() == null) {
                    continue;
                }

                renderQueue.add(
                    RenderItem.ofTile(
                        cell.getTile().getTextureRegion(),
                        x * tileW,
                        y * tileH,
                        tileW,
                        tileH
                    )
                );
            }
        }

        // ── Entities ────────────────────────────────────────────────────
        for (YSortable entity : ySortedEntities) {

            if (entity.isYSorted()) {
                renderQueue.add(
                    RenderItem.ofEntity(entity)
                );
            }
        }

        // ── Sort ────────────────────────────────────────────────────────
        renderQueue.sort(
            (a, b) -> Float.compare(b.depthY, a.depthY)
        );

        // ── Draw ────────────────────────────────────────────────────────
        for (RenderItem item : renderQueue) {

            if (item.tile != null) {

                batch.draw(
                    item.tile,
                    item.tileX,
                    item.tileY,
                    item.tileW,
                    item.tileH
                );

            } else {

                item.entity.render(batch);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // DEBUG
    // ─────────────────────────────────────────────────────────────────────

    private void drawDebug() {

        if (!debug || collisionMap == null) {
            return;
        }

        shapeRenderer.setProjectionMatrix(
            camera.combined
        );

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        );

        // ── Collision polygons ──────────────────────────────────────────
        shapeRenderer.setColor(Color.BLUE);

        for (Polygon p : collisionMap.getPolygons()) {

            shapeRenderer.polygon(
                p.getTransformedVertices()
            );
        }

        // ── Player ──────────────────────────────────────────────────────
        shapeRenderer.setColor(Color.RED);

        shapeRenderer.polygon(
            player.getHitbox().getTransformedVertices()
        );

        // ── Enemies ─────────────────────────────────────────────────────
        shapeRenderer.setColor(Color.GREEN);

        for (EntityTestEnemy enemy : enemies) {

            shapeRenderer.polygon(
                enemy.getHitbox().getTransformedVertices()
            );
        }

        shapeRenderer.end();
    }

    // ─────────────────────────────────────────────────────────────────────
    // SCREEN METHODS
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    // ─────────────────────────────────────────────────────────────────────
    // DISPOSE
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public void dispose() {

        map.dispose();

        renderer.dispose();

        batch.dispose();

        shapeRenderer.dispose();
    }
}
