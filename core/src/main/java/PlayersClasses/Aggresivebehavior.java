package PlayersClasses;

import com.badlogic.gdx.math.Vector2;

/**
 * Агресивна поведінка: ворог переслідує гравця у радіусі aggro
 * та атакує, коли гравець у радіусі атаки.
 *
 * Щоб додати нову поведінку — скопіюйте цей клас, змініть логіку
 * і зареєструйте у BehaviorRegistry під новим рядком-ключем.
 */
public class AggressiveBehavior implements AIBehavior {

    @Override
    public void update(AbstractEnemy enemy,
                       AbstractPlayer player,
                       float delta)
    {
        Vector2 playerPos = player.getPosition();
        float distance    = enemy.getPosition().dst(playerPos);

        if (distance <= enemy.getAggroRange()) {
            enemy.pathTimer += delta;
            if (enemy.pathTimer >= enemy.pathUpdateInterval) {
                enemy.pathTimer = 0f;
                enemy.buildPath(enemy.getPosition(), playerPos);
            }
        } else {
            enemy.path.clear();
            enemy.currentWaypoint = 0;
        }

        if (distance <= enemy.getAttackRange()) {
            enemy.triggerAttack(player);
        }
    }
}
