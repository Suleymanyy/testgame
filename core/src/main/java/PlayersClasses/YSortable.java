package PlayersClasses;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface YSortable {

    float getDepthY();


    boolean isYSorted();

    void render(SpriteBatch batch);
}
