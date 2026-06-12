package PlayersClasses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.mecola.testproject.CollisionProvider;


import java.util.ArrayList;
import java.util.List;

public class EntityTestEnemy implements PlayersClasses.YSortable {

    private int HP = 75;
    private int Damage = 10;
    private int Armor = 5;
    private int BlockChance = 10;

    public enum State {
        IDLE,
        WALK,
        ATTACK,
        DEAD
    }

    private float attackRange = 70f;
    private float attackCooldown = 1.2f;
    private float attackTimer = 0f;
    private boolean attackApplied = false;

    private State state = State.IDLE;
    private float stateTimer = 0f;

    private boolean isDead = false;
    private EntityKnight targetPlayer;

    private Polygon clickHitbox;

    private static final String TAG = "EntityEnemy";

    private static final float SPRITE_WIDTH  = 256f;
    private static final float SPRITE_HEIGHT = 256f;

    private static final float HITBOX_OFFSET_X = 0f;
    private static final float HITBOX_OFFSET_Y = SPRITE_HEIGHT * 0.06f;

    private static final float HITBOX_HALF_W = 30f;
    private static final float HITBOX_HALF_H = 15f;

    private float aggroRange = 400f;

    private float pathUpdateInterval = 0.5f;
    private float pathTimer = 0f;

    private CollisionProvider collisionProvider = null;
    private final Polygon hitbox;

    private Vector2 position;
    private float speed;
    private Direction direction;

    private List<PathNode> path = new ArrayList<>();
    private int currentWaypoint = 0;

    private KnightAnimController animationController;

    public enum Direction { N, NE, E, SE, S, SW, W, NW }

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

    public EntityTestEnemy() {

        position = new Vector2(1200, 400);
        speed = 200f;
        direction = Direction.S;

        animationController = new KnightAnimController();

        hitbox = new Polygon(new float[]{
            -HITBOX_HALF_W,  0,
            0,             -HITBOX_HALF_H,
            HITBOX_HALF_W,  0,
            0,              HITBOX_HALF_H
        });

        clickHitbox = new Polygon(new float[]{
            -40, 0,
            -40, 200,
            40, 200,
            40, 0
        });

        updateHitbox(position.x, position.y);
        updateClickHitbox();
    }

    public Polygon getClickHitbox() {
        return clickHitbox;
    }

    private void updateClickHitbox() {
        clickHitbox.setPosition(position.x, position.y);
    }

    public void receiveDamage(int incomingDamage) {
        if (isDead) return;

        int roll = (int)(Math.random() * 100);
        if (roll < BlockChance) {
            Gdx.app.log("Combat Enemy", "BLOCK!");
            return;
        }

        int finalDamage = incomingDamage - Armor;
        if (finalDamage < 0) finalDamage = 0;

        HP -= finalDamage;
        Gdx.app.log("Combat Enemy", "Damage: " + finalDamage + " | HP: " + HP);

        if (HP <= 0) die();
    }

    private void die() {
        isDead = true;
        state = State.DEAD;
        stateTimer = 0f;
    }

    public void updateAI(EntityKnight player, float delta) {

        if (isDead) return;

        Vector2 playerPos = player.getPosition();
        float distance = position.dst(playerPos);

        if (distance <= aggroRange) {

            pathTimer += delta;

            if (pathTimer >= pathUpdateInterval) {
                pathTimer = 0f;
                buildPath(position, playerPos);
            }

        } else {
            path.clear();
            currentWaypoint = 0;
        }

        if (distance <= attackRange) {

            state = State.ATTACK;
            stateTimer = 0f;
            attackApplied = false;
            targetPlayer = player;

            Vector2 dir = new Vector2(playerPos).sub(position).nor();
            direction = getDirection(dir.x, dir.y);

            path.clear();
            currentWaypoint = 0;
        }
    }

    public void update(float delta) {

        KnightAnimController.State animState = getAnimState();
        animationController.update(delta, animState);

        if (isDead) return;

        if (state == State.ATTACK) {
            stateTimer += delta;

            if (!attackApplied && stateTimer >= attackCooldown * 0.5f) {
                attackApplied = true;
                if (targetPlayer != null) {
                    targetPlayer.receiveDamage(Damage);
                }
            }

            if (stateTimer >= attackCooldown) {
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
    }

    private KnightAnimController.State getAnimState() {
        if (state == State.ATTACK) return KnightAnimController.State.ATTACK;
        if (state == State.DEAD) return KnightAnimController.State.DEAD;

        return currentWaypoint < path.size()
            ? KnightAnimController.State.WALK
            : KnightAnimController.State.IDLE;
    }

    private void move(Vector2 movement, float delta) {

        float newX = position.x + movement.x * speed * delta;
        float newY = position.y + movement.y * speed * delta;

        boolean canX = canMoveTo(newX, position.y);
        boolean canY = canMoveTo(position.x, newY);

        if (canX) position.x = newX;
        if (canY) position.y = newY;

        updateHitbox(position.x, position.y);
        updateClickHitbox();
    }

    private boolean canMoveTo(float newX, float newY) {

        if (collisionProvider == null) return true;

        try {
            updateHitbox(newX, newY);
            return collisionProvider.canMoveTo(hitbox);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Collision error: " + e.getMessage());
            return true;
        }
    }

    private void updateHitbox(float x, float y) {
        hitbox.setPosition(
            x + HITBOX_OFFSET_X,
            y + HITBOX_OFFSET_Y
        );
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
            Vector2 end = new Vector2(
                current.x + signX * diagonalSteps,
                current.y + signY * diagonalSteps
            );

            path.add(new PathNode(
                end,
                getDirection(signX, signY),
                new Vector2(signX, signY).nor()
            ));

            current.set(end);
        }

        if (straightSteps > 0) {

            Vector2 end;
            Direction dir;
            Vector2 dirVec;

            if (absDx > absDy) {
                end = new Vector2(current.x + signX * straightSteps, current.y);
                dir = getDirection(signX, 0);
                dirVec = new Vector2(signX, 0);
            } else {
                end = new Vector2(current.x, current.y + signY * straightSteps);
                dir = getDirection(0, signY);
                dirVec = new Vector2(0, signY);
            }

            path.add(new PathNode(end, dir, dirVec.nor()));
        }
    }

    @Override
    public float getDepthY() {
        return getPosition().y;
    }

    @Override
    public boolean isYSorted() {
        return true;
    }

    public void render(SpriteBatch batch) {

        int index = directionToIndex(direction);

        batch.draw(
            animationController.getFrame(index, getAnimState()),
            position.x - SPRITE_WIDTH / 2f,
            position.y
        );
    }

    public void setCollisionProvider(CollisionProvider provider) {
        this.collisionProvider = provider;
    }

    public Polygon getHitbox() { return hitbox; }

    public Vector2 getPosition() { return position; }

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

    public void dispose() {
        animationController.dispose();
    }
}
