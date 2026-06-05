package MapsClasses;

import PlayersClasses.EntityKnight;
import PlayersClasses.EntityTestEnemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mecola.testproject.CollisionMap;
import com.badlogic.gdx.math.Polygon;

public class Map002Testmap2 implements Screen {

    private CollisionMap collisionMap;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private OrthographicCamera UIcamera;


    private SpriteBatch batch;
    private EntityKnight player;
    private EntityTestEnemy enemy; // ← ВОРОГ

    private ShapeRenderer shapeRenderer;
    private boolean debugCollisions = true;

    @Override
    public void show() {

        map = new TmxMapLoader().load("Maps/map2.tmx");
        renderer = new OrthogonalTiledMapRenderer(map);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1920, 1080);
        camera.position.set(1200, 200, 0);
        camera.zoom = 0.75f;
        camera.update();


        UIcamera = new OrthographicCamera();
        UIcamera.setToOrtho(false, 1920, 1080);
        UIcamera.position.set(1200, 200, 0);
        UIcamera.zoom = 0.75f;
        UIcamera.update();

        batch = new SpriteBatch();

        player = new EntityKnight();
        enemy = new EntityTestEnemy();

        try {
            collisionMap = new CollisionMap(map);

            player.setCollisionProvider(collisionMap);
            enemy.setCollisionProvider(collisionMap);

        } catch (Exception e) {
            Gdx.app.error("Map002Testmap2", "Колізії не завантажені: " + e.getMessage());

            player.setCollisionProvider(null);
            enemy.setCollisionProvider(null);
        }

        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render(float delta) {

        // Оновлено: передаємо самого гравця, щоб ворог міг його атакувати
        enemy.updateAI(player, delta);
        enemy.update(delta);

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        float PlayerX = player.getPosition().x;
        float PlayerY = player.getPosition().y;

        player.update(delta);

        camera.update();
        UIcamera.update();

        camera.position.set(
            PlayerX,
            PlayerY,
            0
        );
        UIcamera.position.set(PlayerX, PlayerY, 0);


        renderer.setView(camera);


        renderer.render(new int[]{0, 1});


        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        player.render(batch);
        enemy.render(batch);

        batch.end();


        ///renderer.render(new int[]{2, 3}); ///зараз в цій мапі тільки 2 шари

        if (Gdx.input.justTouched()) {

            Vector3 clickPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(clickPos);

            // Виправлено логіку: якщо клікнули на ворога - атакуємо, інакше - йдемо
            if (enemy.getClickHitbox() != null && enemy.getClickHitbox().contains(clickPos.x, clickPos.y)) {
                player.setTargetEnemy(enemy);
                player.tryAttack(enemy);
            } else {
                player.buildPath(
                    player.getPosition(),
                    new Vector2(clickPos.x, clickPos.y)
                );
            }
        }

        /// UI

        shapeRenderer.setProjectionMatrix(UIcamera.combined);


        float currentZoom = UIcamera.zoom;


        float barWidth = 300f * currentZoom;
        float barHeight = 25f * currentZoom;
        float padding = 40f * currentZoom;


        float screenRight = UIcamera.position.x + (UIcamera.viewportWidth / 2f) * currentZoom;
        float screenBottom = UIcamera.position.y - (UIcamera.viewportHeight / 2f) * currentZoom;


        float startX = screenRight - barWidth - padding;
        float startY = screenBottom + padding;


        float hpPercent = 0;
        if (player.getMaxHP() > 0) {
            hpPercent = Math.max(0, (float) player.getHP() / player.getMaxHP());
        }


        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.6f);
        shapeRenderer.rect(startX, startY, barWidth, barHeight);


        shapeRenderer.setColor(Color.FIREBRICK);
        shapeRenderer.rect(startX, startY, barWidth * hpPercent, barHeight);
        shapeRenderer.end();


        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
        shapeRenderer.rect(startX, startY, barWidth, barHeight);
        shapeRenderer.end();

        // ДЕБАГ
        if (debugCollisions && collisionMap != null) {

            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            // колізії карти
            shapeRenderer.setColor(Color.BLUE);
            for (Polygon poly : collisionMap.getPolygons()) {
                shapeRenderer.polygon(poly.getTransformedVertices());
            }

            // хітбокс гравця
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.polygon(player.getHitbox().getTransformedVertices());

            // хітбокс ворога
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.polygon(enemy.getHitbox().getTransformedVertices());

            // клік-хітбокс ворога
            shapeRenderer.setColor(Color.YELLOW);
            if (enemy.getClickHitbox() != null) {
                shapeRenderer.polygon(enemy.getClickHitbox().getTransformedVertices());
            }

            shapeRenderer.end();
        }
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
