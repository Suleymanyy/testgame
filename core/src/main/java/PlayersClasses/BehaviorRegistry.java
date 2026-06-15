package PlayersClasses;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Реєстр поведінок ворогів.
 *
 * Рядки-ключі збігаються зі значенням властивості "Behavior"
 * у точці об'єктного шару Tiled.
 *
 * ── Як додати нову поведінку ──────────────────────────────────────
 *  1. Створіть клас, що реалізує AIBehavior (наприклад PatrolBehavior).
 *  2. Зареєструйте його в блоці static нижче:
 *       register("Patrol", PatrolBehavior::new);
 *  3. У Tiled вкажіть у властивості Behavior точки ворога: "Patrol".
 * ─────────────────────────────────────────────────────────────────
 */
public class BehaviorRegistry {

    private static final Map<String, Supplier<AIBehavior>> registry = new HashMap<>();

    static {
        register("Aggressive", AggressiveBehavior::new);
        register("Passive",    PassiveBehavior::new);
        // register("Patrol",  PatrolBehavior::new);   ← приклад для майбутнього
    }

    /**
     * Реєструє поведінку під рядком-ключем.
     * Викликайте цей метод для реєстрації власних поведінок
     * до ініціалізації мапи.
     */
    public static void register(String key, Supplier<AIBehavior> factory) {
        registry.put(key, factory);
    }

    /**
     * Повертає нову інстанцію поведінки за ключем.
     * Якщо ключ не знайдено — повертає AggressiveBehavior за замовчуванням.
     */
    public static AIBehavior create(String behaviorKey) {
        Supplier<AIBehavior> factory = registry.get(behaviorKey);
        if (factory == null) {
            factory = registry.get("Aggressive");   // fallback
        }
        return factory.get();
    }
}
