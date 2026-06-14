package PlayersClasses;

import com.badlogic.gdx.math.Vector2;

public class PathNode {

    public Vector2 point;
    public Direction direction;
    public Vector2 dirVector;

    public PathNode(Vector2 point,
                    Direction direction,
                    Vector2 dirVector)
    {
        this.point = point;
        this.direction = direction;
        this.dirVector = dirVector;
    }
}
