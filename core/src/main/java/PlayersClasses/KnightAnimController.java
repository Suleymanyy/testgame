package PlayersClasses;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class KnightAnimController {

    public enum State {
        IDLE,
        WALK,
        ATTACK,
        DEAD
    }

    private Animation<TextureRegion>[] walkAnimations;
    private Animation<TextureRegion>[] attackAnimations;
    private TextureRegion[] idleFrames;
    private Animation<TextureRegion> deathAnimation;

    private Texture walkSheet;
    private Texture attackSheet;

    private Texture death1, death2, death3, death4;
    private Texture[] idleTextures;

    private float stateTime = 0f;
    private State currentState = State.IDLE;

    private float attackDuration = 0.4f;

    public void setAttackDuration(float duration) {
        this.attackDuration = duration;
    }

    public KnightAnimController() {

        walkSheet = new Texture("Characters/256x256/Knight/Walk/KnightWalkAtlasV2.png");
        attackSheet = new Texture("Characters/256x256/Knight/Attack/KnightAttackAtlas.png");

        TextureRegion[][] walkTmp = TextureRegion.split(walkSheet, 256, 256);
        TextureRegion[][] attackTmp = TextureRegion.split(attackSheet, 256, 256);

        walkAnimations = new Animation[8];
        attackAnimations = new Animation[8];
        idleFrames = new TextureRegion[8];
        idleTextures = new Texture[8];

        for (int i = 0; i < 8; i++) {
            walkAnimations[i] = new Animation<>(0.1f, walkTmp[i]);
            attackAnimations[i] = new Animation<>(attackDuration / 4f, attackTmp[i]);
        }

        idleTextures[0] = new Texture("Characters/256x256/Knight/Idle/TestknightIdleU.png");
        idleTextures[1] = new Texture("Characters/256x256/Knight/Idle/TestknightIdleUR.png");
        idleTextures[2] = new Texture("Characters/256x256/Knight/Idle/TestknightIdleL.png");
        idleTextures[3] = new Texture("Characters/256x256/Knight/Idle/TestknightIdleDR.png");
        idleTextures[4] = new Texture("Characters/256x256/Knight/Idle/TestknightIdleD.png");
        idleTextures[5] = new Texture("Characters/256x256/Knight/Idle/TestknightIdleDL.png");
        idleTextures[6] = new Texture("Characters/256x256/Knight/Idle/TestknightIdleR.png");
        idleTextures[7] = new Texture("Characters/256x256/Knight/Idle/TestknightIdleUL.png");

        for (int i = 0; i < 8; i++) {
            idleFrames[i] = new TextureRegion(idleTextures[i]);
        }

        death1 = new Texture("Characters/256x256/Knight/Death/D1.png");
        death2 = new Texture("Characters/256x256/Knight/Death/D2.png");
        death3 = new Texture("Characters/256x256/Knight/Death/D3.png");
        death4 = new Texture("Characters/256x256/Knight/Death/D4.png");

        deathAnimation = new Animation<>(0.12f,
            new TextureRegion(death1),
            new TextureRegion(death2),
            new TextureRegion(death3),
            new TextureRegion(death4)
        );
    }

    public void update(float delta, State newState) {
        if (newState != currentState) {
            currentState = newState;
            stateTime = 0f;
        } else {
            stateTime += delta;
        }
    }

    public TextureRegion getFrame(int dir, State state) {

        switch (state) {
            case WALK:
                return walkAnimations[dir].getKeyFrame(stateTime, true);

            case ATTACK:
                return attackAnimations[dir].getKeyFrame(stateTime, false);

            case DEAD:
                return deathAnimation.getKeyFrame(stateTime, false);

            default:
                return idleFrames[dir];
        }
    }

    public boolean isAnimationFinished(State state) {
        if (state == State.ATTACK) {
            return attackAnimations[0].isAnimationFinished(stateTime);
        }
        if (state == State.DEAD) {
            return deathAnimation.isAnimationFinished(stateTime);
        }
        return false;
    }

    public void dispose() {
        walkSheet.dispose();
        attackSheet.dispose();

        for (Texture t : idleTextures) t.dispose();

        death1.dispose();
        death2.dispose();
        death3.dispose();
        death4.dispose();
    }
}
