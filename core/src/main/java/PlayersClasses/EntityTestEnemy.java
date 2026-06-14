package PlayersClasses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;

public class EntityTestEnemy
    extends AbstractEnemy {

    public enum State {
        IDLE,
        WALK,
        ATTACK,
        DEAD
    }

    private State state = State.IDLE;

    private float stateTimer = 0f;

    private boolean attackApplied = false;

    private Polygon clickHitbox;

    public EntityTestEnemy() {

        super();

        HP = 75;

        Damage = 10;

        Armor = 5;

        BlockChance = 10;

        animationController =
            new KnightAnimController();

        clickHitbox = new Polygon(
            new float[]{

                -40, 0,
                -40, 200,
                40, 200,
                40, 0
            }
        );

        setPosition(1200, 400);

        updateClickHitbox();
    }

    public Polygon getClickHitbox() {
        return clickHitbox;
    }

    private void updateClickHitbox() {

        clickHitbox.setPosition(
            position.x,
            position.y
        );
    }

    @Override
    public void receiveDamage(
        int incomingDamage)
    {
        if (isDead) return;

        int roll =
            (int)(Math.random() * 100);

        if (roll < BlockChance) {

            Gdx.app.log(
                "Combat Enemy",
                "BLOCK!"
            );

            return;
        }

        int finalDamage =
            incomingDamage - Armor;

        if (finalDamage < 0)
            finalDamage = 0;

        HP -= finalDamage;

        Gdx.app.log(
            "Combat Enemy",
            "Damage: " +
                finalDamage +
                " | HP: " + HP
        );

        if (HP <= 0)
            die();
    }

    private void die() {

        isDead = true;

        state = State.DEAD;

        stateTimer = 0f;
    }

    @Override
    public void updateAI(
        AbstractPlayer player,
        float delta)
    {
        if (isDead) return;

        targetPlayer = player;

        Vector2 playerPos =
            player.getPosition();

        float distance =
            position.dst(playerPos);

        if (distance <= aggroRange) {

            pathTimer += delta;

            if (pathTimer >=
                pathUpdateInterval)
            {
                pathTimer = 0f;

                buildPath(
                    position,
                    playerPos
                );
            }

        } else {

            path.clear();

            currentWaypoint = 0;
        }

        if (distance <= attackRange) {

            state = State.ATTACK;

            stateTimer = 0f;

            attackApplied = false;

            Vector2 dir =
                new Vector2(playerPos)
                    .sub(position)
                    .nor();

            direction =
                getDirection(
                    dir.x,
                    dir.y
                );

            path.clear();

            currentWaypoint = 0;
        }
    }

    @Override
    public void update(float delta) {

        KnightAnimController.State
            animState = getAnimState();

        animationController.update(
            delta,
            animState
        );

        if (isDead) return;

        if (state == State.ATTACK) {

            stateTimer += delta;

            if (!attackApplied &&
                stateTimer >=
                    attackCooldown * 0.5f)
            {
                attackApplied = true;

                if (targetPlayer != null) {

                    ((EntityKnight)
                        targetPlayer)
                        .receiveDamage(
                            Damage
                        );
                }
            }

            if (stateTimer >=
                attackCooldown)
            {
                state = State.IDLE;
            }

            return;
        }

        if (currentWaypoint < path.size()) {

            PathNode node =
                path.get(currentWaypoint);

            Vector2 diff =
                new Vector2(node.point)
                    .sub(position);

            if (diff.len() < 5f) {

                currentWaypoint++;

            } else {

                direction =
                    node.direction;

                move(
                    node.dirVector,
                    delta
                );

                updateClickHitbox();
            }
        }
    }

    private KnightAnimController.State
    getAnimState()
    {
        if (state == State.ATTACK)
            return KnightAnimController
                .State.ATTACK;

        if (state == State.DEAD)
            return KnightAnimController
                .State.DEAD;

        return currentWaypoint <
            path.size()

            ? KnightAnimController
            .State.WALK

            : KnightAnimController
            .State.IDLE;
    }

    @Override
    public void render(SpriteBatch batch) {

        int index =
            directionToIndex(direction);

        batch.draw(
            animationController.getFrame(
                index,
                getAnimState()
            ),

            position.x -
                SPRITE_WIDTH / 2f,

            position.y
        );
    }

    @Override
    public void dispose() {

        animationController.dispose();
    }
}
