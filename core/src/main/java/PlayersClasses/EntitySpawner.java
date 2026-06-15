package MapsClasses;

import PlayersClasses.AIBehavior;
import PlayersClasses.AbstractEnemy;
import PlayersClasses.AbstractPlayer;
import PlayersClasses.BehaviorRegistry;
import PlayersClasses.EntityRegistry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Array;

/**
 * Читає шар об'єктів "EntityPositions" з тайлмапи і спавнить
 * гравців та ворогів у вказаних точках.
 *
 * ── Формат об'єктів у Tiled ──────────────────────────────────────
 *
 *  Клас (Class): "Player" | "Enemy"
 *
 *  Для Player:
 *    (немає додаткових властивостей)
 *
 *  Для Enemy:
 *    EnemyType  (string) — ключ у EntityRegistry,   напр. "TestEnemy"
 *    Behavior   (string) — ключ у BehaviorRegistry,  напр. "Aggressive"
 *
 * ─────────────────────────────────────────────────────────────────
 */
public class EntitySpawner {

    private static final String LAYER_NAME = "EntityPositions";

    private final TiledMap map;

    public EntitySpawner(TiledMap map) {
        this.map = map;
    }

    /**
     * Спавнить одного гравця (перша знайдена точка з класом "Player").
     * Позиція гравця встановлюється зі спавн-точки Tiled.
     * Якщо спавн-точка відсутня — позиція не змінюється.
     *
     * @param player  вже створений екземпляр гравця
     */
    public void spawnPlayer(AbstractPlayer player) {
        MapLayer layer = getLayer();
        if (layer == null) return;

        for (MapObject obj : layer.getObjects()) {
            if ("Player".equals(getClass(obj))) {
                float x = getFloat(obj, "x", 0f);
                float y = getFloat(obj, "y", 0f);
                player.setPosition(x, y);
                Gdx.app.log("EntitySpawner",
                    "Player spawned at (" + x + ", " + y + ")");
                return;   // беремо лише першу знайдену точку
            }
        }

        Gdx.app.error("EntitySpawner",
            "No 'Player' spawn point found in layer '" + LAYER_NAME + "'");
    }

    /**
     * Спавнить усіх ворогів, знайдених у шарі об'єктів.
     * Для кожного об'єкта з класом "Enemy":
     *   — створює екземпляр за EnemyType через EntityRegistry
     *   — призначає поведінку за Behavior через BehaviorRegistry
     *   — встановлює позицію зі спавн-точки Tiled
     *   — реєструє CollisionProvider якщо він вже встановлений у ворога
     *
     * @param collisionProvider  провайдер колізій (може бути null)
     * @return  масив заспавнених ворогів
     */
    public Array<AbstractEnemy> spawnEnemies(
        com.mecola.testproject.CollisionMap collisionProvider)
    {
        Array<AbstractEnemy> enemies = new Array<>();
        MapLayer layer = getLayer();
        if (layer == null) return enemies;

        for (MapObject obj : layer.getObjects()) {
            if (!"Enemy".equals(getClass(obj))) continue;

            MapProperties props = obj.getProperties();

            String enemyType   = props.get("EnemyType", "TestEnemy", String.class);
            String behaviorKey = props.get("Behavior",  "Aggressive", String.class);

            float x = getFloat(obj, "x", 0f);
            float y = getFloat(obj, "y", 0f);

            AbstractEnemy enemy = EntityRegistry.create(enemyType);
            if (enemy == null) continue;   // невідомий тип — пропускаємо

            // Призначаємо поведінку
            AIBehavior behavior = BehaviorRegistry.create(behaviorKey);
            enemy.setBehavior(behavior);

            // Позиція зі спавн-точки
            enemy.setPosition(x, y);

            // Колізії
            if (collisionProvider != null) {
                enemy.setCollisionProvider(collisionProvider);
            }

            enemies.add(enemy);

            Gdx.app.log("EntitySpawner",
                "Spawned " + enemyType + " [" + behaviorKey + "]" +
                    " at (" + x + ", " + y + ")");
        }

        return enemies;
    }

    // ── Допоміжні методи ────────────────────────────────────────────

    private MapLayer getLayer() {
        MapLayer layer = map.getLayers().get(LAYER_NAME);
        if (layer == null) {
            Gdx.app.error("EntitySpawner",
                "Object layer '" + LAYER_NAME + "' not found in map!");
        }
        return layer;
    }

    /**
     * Читає "class" або "type" об'єкта Tiled (API змінилось у різних версіях).
     */
    private static String getClass(MapObject obj) {
        // libGDX 1.12+: obj.getProperties().get("class", String.class)
        // Старіші версії: "type"
        MapProperties p = obj.getProperties();
        String cls = p.get("class", String.class);
        if (cls == null) cls = p.get("type", String.class);
        return cls;
    }

    private static float getFloat(MapObject obj, String key, float def) {
        Object val = obj.getProperties().get(key);
        if (val instanceof Number) return ((Number) val).floatValue();
        return def;
    }
}
