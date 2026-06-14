package PlayersClasses;

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

    public abstract void updateAI(
        AbstractPlayer player,
        float delta);

    public abstract void receiveDamage(
        int damage);
}
