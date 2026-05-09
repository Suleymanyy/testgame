package PlayersClasses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

public class TestPlayerEntity {
    ///Змінні

    public enum Direction {
        N, NE, E, SE, S, SW, W, NW
    }

    private float waypointStep = 64f;

    private Vector2 position;
    private Vector2 target;

    /// теперь храним точку + фиксированное направление + вектор движения
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

    private List<PathNode> path = new ArrayList<>();
    private int currentWaypoint = 0;

    private float speed;
    private Direction direction;

    private Texture[] directionSprites;

    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    public TestPlayerEntity() {

        position = new Vector2(2200, 400);
        target = null;

        speed = 200f;
        direction = Direction.S;

        /// Текстури спрайта в стані спокою в різних напрямках у масиві
        directionSprites = new Texture[8];

        directionSprites[0] = new Texture("Characters/256x256/TestPlayerEntity/TestPlayerEntity1.png");
        directionSprites[1] = new Texture("Characters/256x256/TestPlayerEntity/TestPlayerEntity2.png");
        directionSprites[2] = new Texture("Characters/256x256/TestPlayerEntity/TestPlayerEntity3.png");
        directionSprites[3] = new Texture("Characters/256x256/TestPlayerEntity/TestPlayerEntity4.png");
        directionSprites[4] = new Texture("Characters/256x256/TestPlayerEntity/TestPlayerEntity5.png");
        directionSprites[5] = new Texture("Characters/256x256/TestPlayerEntity/TestPlayerEntity6.png");
        directionSprites[6] = new Texture("Characters/256x256/TestPlayerEntity/TestPlayerEntity7.png");
        directionSprites[7] = new Texture("Characters/256x256/TestPlayerEntity/TestPlayerEntity8.png");
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

        /// діагональний рух
        if (diagonalSteps > 0) {
            Vector2 diagonalEnd = new Vector2(
                current.x + signX * diagonalSteps,
                current.y + signY * diagonalSteps
            );

            Direction dir = getDirection(signX, signY);
            Vector2 dirVec = new Vector2(signX, signY).nor();

            path.add(new PathNode(diagonalEnd, dir, dirVec));
            current.set(diagonalEnd);
        }

        /// прямий рух
        if (straightSteps > 0) {

            Vector2 straightEnd;
            Direction dir;
            Vector2 dirVec;

            if (absDx > absDy) {
                straightEnd = new Vector2(
                    current.x + signX * straightSteps,
                    current.y
                );
                dir = getDirection(signX, 0);
                dirVec = new Vector2(signX, 0);
            } else {
                straightEnd = new Vector2(
                    current.x,
                    current.y + signY * straightSteps
                );
                dir = getDirection(0, signY);
                dirVec = new Vector2(0, signY);
            }

            dirVec.nor();
            path.add(new PathNode(straightEnd, dir, dirVec));
        }
    }

    public void update(float delta) {


        if (currentWaypoint < path.size()) {

            PathNode node = path.get(currentWaypoint);
            Vector2 diff = new Vector2(node.point).sub(position);

            if (diff.len() < 5f) {
                currentWaypoint++;
            } else {


                direction = node.direction;

                /// без diff
                Vector2 movement = new Vector2(node.dirVector);

                position.mulAdd(movement, speed * delta);
            }
        }

        Vector2 movement = new Vector2();

        if (Gdx.input.isKeyPressed(Input.Keys.W)) movement.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) movement.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) movement.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) movement.x += 1;

        if (movement.len() > 0) {
            path.clear(); // сброс pathfinding

            int dx = (int) Math.signum(movement.x);
            int dy = (int) Math.signum(movement.y);
            direction = getDirection(dx, dy);

            movement.nor();
            position.mulAdd(movement, speed * delta);
        }
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

    public void render(SpriteBatch batch) {
        batch.draw(getCurrentFrame(), position.x, position.y);
    }

    private Texture getCurrentFrame() {
        return directionSprites[directionToIndex(direction)];
    }

    private int directionToIndex(Direction dir) {
        switch (dir) {
            case N: return 0;
            case NE: return 1;
            case E: return 2;
            case SE: return 3;
            case S: return 4;
            case SW: return 5;
            case W: return 6;
            case NW: return 7;
        }
        return 0;
    }

    public void setTarget(Vector2 target) {
        this.target = target;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void dispose() {
        for (Texture tex : directionSprites) {
            tex.dispose();
        }
    }
}
