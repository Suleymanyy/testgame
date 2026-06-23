package PlayersClasses;

import com.badlogic.gdx.math.Polygon;

public abstract class AbstractEnemy
    extends AbstractPlayer {

    protected int HP;
    protected int Damage;
    protected int Armor;
    protected int BlockChance;

    protected boolean isDead = false;

    protected float attackRange = 70f;

    protected float attackCooldown = 1.2f;

    protected float attackTimer = 0f;

    protected float aggroRange = 400f;

    protected float pathUpdateInterval = 0.5f;

    protected float pathTimer = 0f;

    protected AbstractPlayer targetPlayer;

    private Polygon clickHitbox;

    public AbstractEnemy() {


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

    public abstract void updateAI(
        AbstractPlayer player,
        float delta);

    public abstract void receiveDamage(
        int damage);
}
