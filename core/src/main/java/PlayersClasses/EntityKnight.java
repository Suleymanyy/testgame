package PlayersClasses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class EntityKnight extends AbstractPlayer {

    private int HP = 100;
    private int MaxHP = 100;

    private int Damage = 15;
    private int Armor = 5;
    private int BlockChance = 10;

    public enum State {
        IDLE,
        WALK,
        ATTACK,
        DEAD
    }

    private State state = State.IDLE;

    private float stateTimer = 0f;

    private float attackDuration = 0.4f;

    private boolean attackApplied = false;

    private float attackRange = 80f;

    private boolean isDead = false;

    private AbstractEnemy targetEnemy;

    public EntityKnight() {

        super();

        animationController =
            new KnightAnimController();

        setPosition(2200, 400);
    }

    public int getHP() {
        return HP;
    }

    public int getMaxHP() {
        return MaxHP;
    }

    public void setTargetEnemy(
        AbstractEnemy enemy)
    {
        this.targetEnemy = enemy;
    }

    public void receiveDamage(
        int incomingDamage)
    {
        if (isDead) return;

        int roll =
            (int)(Math.random() * 100);

        if (roll < BlockChance) return;

        int finalDamage =
            incomingDamage - Armor;

        if (finalDamage < 0)
            finalDamage = 0;

        HP -= finalDamage;

        if (HP <= 0) {
            die();
        }
    }

    private void die() {

        isDead = true;

        state = State.DEAD;

        stateTimer = 0f;
    }

    public void tryAttack(
        AbstractEnemy enemy)
    {
        if (isDead) return;

        if (state == State.ATTACK)
            return;

        float dist =
            position.dst(enemy.getPosition());

        if (dist <= attackRange) {

            state = State.ATTACK;

            stateTimer = 0f;

            attackApplied = false;

            Vector2 dir =
                new Vector2(
                    enemy.getPosition()
                )
                    .sub(position)
                    .nor();

            direction =
                getDirection(dir.x, dir.y);

            path.clear();
        }
    }

    @Override
    public void update(float delta) {

        KnightAnimController.State animState =
            getAnimState();

        animationController.update(
            delta,
            animState
        );

        if (isDead) return;

        if (state == State.ATTACK) {

            stateTimer += delta;

            if (!attackApplied &&
                stateTimer >=
                    attackDuration * 0.5f)
            {
                attackApplied = true;

                if (targetEnemy != null) {

                    targetEnemy.receiveDamage(
                        Damage
                    );
                }
            }

            if (animationController
                .isAnimationFinished(
                    KnightAnimController
                        .State.ATTACK))
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

                direction = node.direction;

                move(node.dirVector, delta);
            }
        }

        Vector2 movement =
            new Vector2();

        if (Gdx.input.isKeyPressed(
            Input.Keys.W))
            movement.y += 1;

        if (Gdx.input.isKeyPressed(
            Input.Keys.S))
            movement.y -= 1;

        if (Gdx.input.isKeyPressed(
            Input.Keys.A))
            movement.x -= 1;

        if (Gdx.input.isKeyPressed(
            Input.Keys.D))
            movement.x += 1;

        if (movement.len() > 0) {

            path.clear();

            direction =
                getDirection(
                    Math.signum(movement.x),
                    Math.signum(movement.y)
                );

            move(movement.nor(), delta);
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

        boolean moving =
            currentWaypoint < path.size() ||

                Gdx.input.isKeyPressed(
                    Input.Keys.W) ||

                Gdx.input.isKeyPressed(
                    Input.Keys.A) ||

                Gdx.input.isKeyPressed(
                    Input.Keys.S) ||

                Gdx.input.isKeyPressed(
                    Input.Keys.D);

        return moving
            ? KnightAnimController.State.WALK
            : KnightAnimController.State.IDLE;
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
