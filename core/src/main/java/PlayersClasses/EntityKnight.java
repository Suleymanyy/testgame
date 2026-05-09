package PlayersClasses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.mecola.testproject.CollisionProvider;

import java.util.ArrayList;
import java.util.List;

public class EntityKnight {

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
    private EntityTestEnemy targetEnemy;

    private static final String TAG = "EntityKnight";

    private static final float SPRITE_WIDTH  = 256f;
    private static final float SPRITE_HEIGHT = 256f;

    private static final float HITBOX_OFFSET_X = 0f;
    private static final float HITBOX_OFFSET_Y = SPRITE_HEIGHT * 0.06f;

    private static final float HITBOX_HALF_W = 30f;
    private static final float HITBOX_HALF_H = 15f;

    private CollisionProvider collisionProvider = null;
    private final Polygon hitbox;

    private KnightAnimController animationController;

    public enum Direction { N, NE, E, SE, S, SW, W, NW }

    private Vector2 position;
    private Vector2 target;
    private List<PathNode> path = new ArrayList<>();
    private int currentWaypoint = 0;
    private float speed;
    private Direction direction;

    private static class PathNode {
        Vector2 point;
        Direction direction;
        Vector2 dirVector;

        PathNode(Vector2 point, Direction direction, Vector2 dirVector) {
            this.point = point;
            this.direction = direction;
            this.dirVector = dirVector;
        }
    }

    public EntityKnight() {
        position = new Vector2(2200, 400);
        target = null;
        speed = 200f;
        direction = Direction.S;

        animationController = new KnightAnimController();

        hitbox = new Polygon(new float[]{
            -HITBOX_HALF_W,  0,
            0,             -HITBOX_HALF_H,
            HITBOX_HALF_W,  0,
            0,              HITBOX_HALF_H
        });

        updateHitbox(position.x, position.y);
    }

    public int getHP() { return HP; }
    public int getMaxHP() { return MaxHP; }

    public void setTargetEnemy(EntityTestEnemy enemy) {
        this.targetEnemy = enemy;
    }

    private void die() {
        isDead = true;
        state = State.DEAD;
        stateTimer = 0f;
    }

    public void receiveDamage(int incomingDamage) {
        if (isDead) return;

        int roll = (int)(Math.random() * 100);
        if (roll < BlockChance) return;

        int finalDamage = incomingDamage - Armor;
        if (finalDamage < 0) finalDamage = 0;

        HP -= finalDamage;

        if (HP <= 0) die();
    }

    public void tryAttack(EntityTestEnemy enemy) {
        if (isDead) return;
        if (state == State.ATTACK) return;

        float dist = position.dst(enemy.getPosition());

        if (dist <= attackRange) {
            state = State.ATTACK;
            stateTimer = 0f;
            attackApplied = false;

            Vector2 dir = new Vector2(enemy.getPosition()).sub(position).nor();
            direction = getDirection(dir.x, dir.y);

            path.clear();
        }
    }

    public void setCollisionProvider(CollisionProvider provider) {
        this.collisionProvider = provider;
    }

    private void updateHitbox(float x, float y) {
        hitbox.setPosition(x + HITBOX_OFFSET_X, y + HITBOX_OFFSET_Y);
    }

    private boolean canMoveTo(float newX, float newY) {
        if (collisionProvider == null) return true;

        try {
            updateHitbox(newX, newY);
            return collisionProvider.canMoveTo(hitbox);
        } catch (Exception e) {
            Gdx.app.error(TAG, e.getMessage());
            return true;
        }
    }

    private void move(Vector2 movement, float delta) {
        float newX = position.x + movement.x * speed * delta;
        float newY = position.y + movement.y * speed * delta;

        boolean canX = canMoveTo(newX, position.y);
        boolean canY = canMoveTo(position.x, newY);

        if (canX) position.x = newX;
        if (canY) position.y = newY;

        updateHitbox(position.x, position.y);
    }

    public void buildPath(Vector2 start, Vector2 target) {
        path.clear();
        currentWaypoint = 0;

        float dx = target.x - start.x;
        float dy = target.y - start.y;

        float absDx = Math.abs(dx);
        float absDy = Math.abs(dy);

        float signX = Math.signum(dx);
        float signY = Math.signum(dy);

        float diagonalSteps = Math.min(absDx, absDy);
        float straightSteps = Math.abs(absDx - absDy);

        Vector2 current = new Vector2(start);

        if (diagonalSteps > 0) {
            Vector2 diagonalEnd = new Vector2(
                current.x + signX * diagonalSteps,
                current.y + signY * diagonalSteps
            );

            path.add(new PathNode(
                diagonalEnd,
                getDirection(signX, signY),
                new Vector2(signX, signY).nor()
            ));

            current.set(diagonalEnd);
        }

        if (straightSteps > 0) {
            Vector2 straightEnd;
            Direction dir;
            Vector2 dirVec;

            if (absDx > absDy) {
                straightEnd = new Vector2(current.x + signX * straightSteps, current.y);
                dir = getDirection(signX, 0);
                dirVec = new Vector2(signX, 0);
            } else {
                straightEnd = new Vector2(current.x, current.y + signY * straightSteps);
                dir = getDirection(0, signY);
                dirVec = new Vector2(0, signY);
            }

            path.add(new PathNode(straightEnd, dir, dirVec.nor()));
        }
    }

    private KnightAnimController.State getAnimState() {
        if (state == State.ATTACK) return KnightAnimController.State.ATTACK;
        if (state == State.DEAD) return KnightAnimController.State.DEAD;

        boolean moving =
            currentWaypoint < path.size() ||
                Gdx.input.isKeyPressed(Input.Keys.W) ||
                Gdx.input.isKeyPressed(Input.Keys.A) ||
                Gdx.input.isKeyPressed(Input.Keys.S) ||
                Gdx.input.isKeyPressed(Input.Keys.D);

        return moving ? KnightAnimController.State.WALK : KnightAnimController.State.IDLE;
    }

    public void update(float delta) {

        KnightAnimController.State animState = getAnimState();
        animationController.update(delta, animState);

        if (isDead) return;

        if (state == State.ATTACK) {

            stateTimer += delta;

            if (!attackApplied && stateTimer >= attackDuration * 0.5f) {
                attackApplied = true;
                if (targetEnemy != null) {
                    targetEnemy.receiveDamage(Damage);
                }
            }

            if (animationController.isAnimationFinished(KnightAnimController.State.ATTACK)) {
                state = State.IDLE;
            }

            return;
        }

        if (currentWaypoint < path.size()) {
            PathNode node = path.get(currentWaypoint);
            Vector2 diff = new Vector2(node.point).sub(position);

            if (diff.len() < 5f) {
                currentWaypoint++;
            } else {
                direction = node.direction;
                move(node.dirVector, delta);
            }
        }

        Vector2 movement = new Vector2();

        if (Gdx.input.isKeyPressed(Input.Keys.W)) movement.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) movement.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) movement.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) movement.x += 1;

        if (movement.len() > 0) {
            path.clear();
            direction = getDirection(Math.signum(movement.x), Math.signum(movement.y));
            move(movement.nor(), delta);
        }
    }

    public void render(SpriteBatch batch) {
        int index = directionToIndex(direction);

        batch.draw(
            animationController.getFrame(index, getAnimState()),
            position.x - SPRITE_WIDTH / 2f,
            position.y
        );
    }

    public Polygon getHitbox() { return hitbox; }

    private int directionToIndex(Direction dir) {
        switch (dir) {
            case N: return 0; case NE: return 1; case E: return 2; case SE: return 3;
            case S: return 4; case SW: return 5; case W: return 6; case NW: return 7;
        }
        return 0;
    }

    private Direction getDirection(float x, float y) {
        int dx = (int) x;
        int dy = (int) y;

        if (dx == 0 && dy > 0) return Direction.N;
        if (dx > 0 && dy > 0) return Direction.NE;
        if (dx > 0 && dy == 0) return Direction.E;
        if (dx > 0 && dy < 0) return Direction.SE;
        if (dx == 0 && dy < 0) return Direction.S;
        if (dx < 0 && dy < 0) return Direction.SW;
        if (dx < 0 && dy == 0) return Direction.W;
        if (dx < 0 && dy > 0) return Direction.NW;

        return Direction.S;
    }

    public void setTarget(Vector2 target) { this.target = target; }
    public Vector2 getPosition() { return position; }

    public void dispose() {
        animationController.dispose();
    }
}
