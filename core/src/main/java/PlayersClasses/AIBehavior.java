package PlayersClasses;

/**
 * Інтерфейс, що описує один вид поведінки ворога.
 * Реалізуйте його щоб створити новий тип ШІ.
 *
 * Приклади:
 *   "Aggressive"  → AggressiveBehavior
 *   "Patrol"      → PatrolBehavior
 *   "Passive"     → PassiveBehavior
 */
public interface AIBehavior {

    /**
     * Виклики кожен кадр для ворога, якому призначена ця поведінка.
     *
     * @param enemy  ворог, яким керує цей ШІ
     * @param player гравець (ціль)
     * @param delta  час з попереднього кадру, секунди
     */
    void update(AbstractEnemy enemy, AbstractPlayer player, float delta);
}
