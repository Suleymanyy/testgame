package PlayersClasses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.mecola.testproject.CollisionProvider;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlayer

    implements YSortable
    {

    protected static final float SPRITE_WIDTH  = 256f;
    protected static final float SPRITE_HEIGHT = 256f;

    protected static final float HITBOX_OFFSET_X = 0f;
    protected static final float HITBOX_OFFSET_Y =
        SPRITE_HEIGHT * 0.06f;

    protected static final float HITBOX_HALF_W = 30f;
    protected static final float HITBOX_HALF_H = 15f;

    protected Vector2 position;
    protected float speed;

    protected Direction direction;

    protected CollisionProvider collisionProvider;

    protected Polygon hitbox;

    protected List<PathNode> path =
        new ArrayList<>();

    protected int currentWaypoint = 0;

    protected KnightAnimController animationController;

    protected static final String TAG =
        "AbstractPlayer";

    public AbstractPlayer() {

        position = new Vector2();

        direction = Direction.S;

        speed = 200f;

        hitbox = new Polygon(new float[]{
            -HITBOX_HALF_W,  0,
            0,              -HITBOX_HALF_H,
            HITBOX_HALF_W,   0,
            0,               HITBOX_HALF_H
        });
    }

    public void setPosition(float x, float y) {

        position.set(x, y);

        updateHitbox(position.x, position.y);
    }

    public Vector2 getPosition() {
        return position;
    }

    public Polygon getHitbox() {
        return hitbox;
    }

    public void setCollisionProvider(
        CollisionProvider provider)
    {
        this.collisionProvider = provider;
    }

    protected void updateHitbox(float x, float y) {

        hitbox.setPosition(
            x + HITBOX_OFFSET_X,
            y + HITBOX_OFFSET_Y
        );
    }

    protected boolean canMoveTo(
        float newX,
        float newY)
    {
        if (collisionProvider == null)
            return true;

        try {

            updateHitbox(newX, newY);

            return collisionProvider
                .canMoveTo(hitbox);

        } catch (Exception e) {

            Gdx.app.error(TAG, e.getMessage());

            return true;
        }
    }

    protected void move(
        Vector2 movement,
        float delta)
    {
        float newX =
            position.x +
                movement.x * speed * delta;

        float newY =
            position.y +
                movement.y * speed * delta;

        boolean canX =
            canMoveTo(newX, position.y);

        boolean canY =
            canMoveTo(position.x, newY);

        if (canX) position.x = newX;
        if (canY) position.y = newY;

        updateHitbox(position.x, position.y);
    }

    public void buildPath(
        Vector2 start,
        Vector2 target)
    {
        path.clear();

        currentWaypoint = 0;

        float dx = target.x - start.x;
        float dy = target.y - start.y;

        float absDx = Math.abs(dx);
        float absDy = Math.abs(dy);

        float signX = Math.signum(dx);
        float signY = Math.signum(dy);

        float diagonalSteps =
            Math.min(absDx, absDy);

        float straightSteps =
            Math.abs(absDx - absDy);

        Vector2 current =
            new Vector2(start);

        if (diagonalSteps > 0) {

            Vector2 diagonalEnd =
                new Vector2(
                    current.x +
                        signX * diagonalSteps,

                    current.y +
                        signY * diagonalSteps
                );

            path.add(
                new PathNode(
                    diagonalEnd,

                    getDirection(
                        signX,
                        signY
                    ),

                    new Vector2(
                        signX,
                        signY
                    ).nor()
                )
            );

            current.set(diagonalEnd);
        }

        if (straightSteps > 0) {

            Vector2 straightEnd;

            Direction dir;

            Vector2 dirVec;

            if (absDx > absDy) {

                straightEnd =
                    new Vector2(
                        current.x +
                            signX * straightSteps,

                        current.y
                    );

                dir =
                    getDirection(signX, 0);

                dirVec =
                    new Vector2(signX, 0);

            } else {

                straightEnd =
                    new Vector2(
                        current.x,

                        current.y +
                            signY * straightSteps
                    );

                dir =
                    getDirection(0, signY);

                dirVec =
                    new Vector2(0, signY);
            }

            path.add(
                new PathNode(
                    straightEnd,
                    dir,
                    dirVec.nor()
                )
            );
        }
    }

    protected Direction getDirection(
        float x,
        float y)
    {
        int dx = (int)x;
        int dy = (int)y;

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

    protected int directionToIndex(Direction dir)
    {
        switch (dir) {

            case N:  return 0;
            case NE: return 1;
            case E:  return 2;
            case SE: return 3;
            case S:  return 4;
            case SW: return 5;
            case W:  return 6;
            case NW: return 7;
        }

        return 0;
    }

    @Override
    public float getDepthY() {
        return position.y;
    }

    @Override
    public boolean isYSorted() {
        return true;
    }

    public abstract void update(float delta);

    public abstract void render(
        SpriteBatch batch);

    public abstract void dispose();
}
