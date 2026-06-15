package PlayersClasses;

/**
 * Пасивна поведінка: ворог стоїть на місці і нікого не чіпає.
 * Корисна для NPC-торговців, мирних жителів тощо.
 *
 * Щоб додати нову поведінку — реалізуйте AIBehavior і зареєструйте
 * у BehaviorRegistry під потрібним рядком-ключем.
 */
public class PassiveBehavior implements AIBehavior {

    @Override
    public void update(AbstractEnemy enemy,
                       AbstractPlayer player,
                       float delta)
    {
        // Нічого не робимо — ворог стоїть нерухомо
    }
}
