package PlayersClasses;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Реєстр типів ворогів.
 *
 * Рядки-ключі збігаються зі значенням властивості "EnemyType"
 * у точці об'єктного шару Tiled.
 *
 * ── Як додати нового ворога ───────────────────────────────────────
 *  1. Створіть клас, що наслідує AbstractEnemy (наприклад EntityOrc).
 *  2. Зареєструйте його в блоці static нижче:
 *       register("Orc", EntityOrc::new);
 *  3. У Tiled вкажіть у властивості EnemyType точки ворога: "Orc".
 * ─────────────────────────────────────────────────────────────────
 */
public class EntityRegistry {

    private static final Map<String, Supplier<AbstractEnemy>> registry = new HashMap<>();

    static {
        register("TestEnemy", EntityTestEnemy::new);
        // register("Orc",    EntityOrc::new);   ← приклад для майбутнього
        // register("Mage",   EntityMage::new);
    }

    /**
     * Реєструє фабрику ворога під рядком-ключем.
     */
    public static void register(String key, Supplier<AbstractEnemy> factory) {
        registry.put(key, factory);
    }

    /**
     * Створює нового ворога за ключем типу.
     * Якщо ключ не знайдено — повертає null і логує помилку.
     */
    public static AbstractEnemy create(String enemyTypeKey) {
        Supplier<AbstractEnemy> factory = registry.get(enemyTypeKey);
        if (factory == null) {
            com.badlogic.gdx.Gdx.app.error(
                "EntityRegistry",
                "Unknown EnemyType: '" + enemyTypeKey +
                    "'. Did you forget to register it?"
            );
            return null;
        }
        return factory.get();
    }
}
