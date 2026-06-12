package MapsClasses;

import PlayersClasses.EntityKnight;
import PlayersClasses.EntityTestEnemy;
import PlayersClasses.YSortable;

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

    // ── Список усіх сутностей, що беруть участь у Y-sort ──
    private Array<YSortable> ySortedEntities;

    @Override
    public void show() {
        map = new TmxMapLoader().load("Maps/map2.tmx");
        renderer = new OrthogonalTiledMapRenderer(map);
        environmentLayer = (TiledMapTileLayer) map.getLayers().get("Environment");

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1920, 1080);
        camera.zoom = 0.75f;

        UIcamera = new OrthographicCamera();
        UIcamera.setToOrtho(false, 1920, 1080);
        UIcamera.zoom = 0.75f;

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        player = new EntityKnight();
        enemy  = new EntityTestEnemy();

        try {
            collisionMap = new CollisionMap(map);
            player.setCollisionProvider(collisionMap);
            enemy.setCollisionProvider(collisionMap);
        } catch (Exception e) {
            Gdx.app.error("Map", "Collision error: " + e.getMessage());
        }

        // ── Реєструємо всі сутності ──────────────────────────────────────
        ySortedEntities = new Array<>();
        ySortedEntities.add(player);
        ySortedEntities.add(enemy);
        // Щоб додати ще одного: ySortedEntities.add(newEntity);
    }

    @Override
    public void render(float delta) {
        enemy.updateAI(player, delta);
        enemy.update(delta);
        player.update(delta);

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        camera.position.set(player.getPosition().x, player.getPosition().y, 0);
        UIcamera.position.set(player.getPosition().x, player.getPosition().y, 0);
        camera.update();
        UIcamera.update();

        renderer.setView(camera);
        renderer.render(new int[]{0, 1});   // фонові шари

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawEnvironmentYSorted();           // ← тут і тайли, і сутності
        batch.end();

        renderer.render(new int[]{3});      // верхні шари (дахи тощо)

        handleInput();
        drawDebug();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Y-SORT: тайли + усі зареєстровані сутності в одному проході
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Маленький контейнер, що описує один "об'єкт" черги рендеру.
     * Або це тайл (tile != null), або сутність (entity != null).
     */
    private static class RenderItem {
        // тайл
        com.badlogic.gdx.graphics.g2d.TextureRegion tile;
        float tileX, tileY, tileW, tileH;

        // сутність
        YSortable entity;

        // ключ сортування
        float depthY;

        static RenderItem ofTile(com.badlogic.gdx.graphics.g2d.TextureRegion tex,
                                 float wx, float wy, float tw, float th) {
            RenderItem item = new RenderItem();
            item.tile   = tex;
            item.tileX  = wx;  item.tileY = wy;
            item.tileW  = tw;  item.tileH = th;
            // порівнюємо по центру тайла, як було раніше
            item.depthY = wy + th * 0.5f;
            return item;
        }

        static RenderItem ofEntity(YSortable e) {
            RenderItem item = new RenderItem();
            item.entity  = e;
            item.depthY  = e.getDepthY();
            return item;
        }
    }

    private final Array<RenderItem> renderQueue = new Array<>(256);

    private void drawEnvironmentYSorted() {
        renderQueue.clear();

        float tileW = environmentLayer.getTileWidth();
        float tileH = environmentLayer.getTileHeight();
        int maxX = environmentLayer.getWidth();
        int maxY = environmentLayer.getHeight();

        // ── 1. Додаємо всі тайли ──────────────────────────────────────────
        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                TiledMapTileLayer.Cell cell = environmentLayer.getCell(x, y);
                if (cell == null || cell.getTile() == null) continue;

                renderQueue.add(RenderItem.ofTile(
                    cell.getTile().getTextureRegion(),
                    x * tileW, y * tileH, tileW, tileH
                ));
            }
        }

        // ── 2. Додаємо сутності (лише ті, що isYSorted == true) ───────────
        for (YSortable entity : ySortedEntities) {
            if (entity.isYSorted()) {
                renderQueue.add(RenderItem.ofEntity(entity));
            }
        }

        // ── 3. Сортуємо за depthY по спаданню (великий Y = далі = малюємо першим) ──
        renderQueue.sort((a, b) -> Float.compare(b.depthY, a.depthY));

        // ── 4. Рендеримо у відсортованому порядку ─────────────────────────
        for (RenderItem item : renderQueue) {
            if (item.tile != null) {
                batch.draw(item.tile, item.tileX, item.tileY, item.tileW, item.tileH);
            } else {
                item.entity.render(batch);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            Vector3 pos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(pos);

            if (enemy.getClickHitbox() != null &&
                enemy.getClickHitbox().contains(pos.x, pos.y)) {
                player.setTargetEnemy(enemy);
                player.tryAttack(enemy);
            } else {
                player.buildPath(player.getPosition(), new Vector2(pos.x, pos.y));
            }
        }
    }

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
