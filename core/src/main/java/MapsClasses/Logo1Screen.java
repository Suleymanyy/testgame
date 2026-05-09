package MapsClasses;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mecola.testproject.Main;

public class Logo1Screen implements Screen {

    private final Main game;
    private SpriteBatch batch;
    private Texture image;

    public Logo1Screen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0,0,0,1);

        batch.begin();
        batch.draw(image,140,210);
        batch.end();
    }

    @Override
    public void resize(int i, int i1) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
