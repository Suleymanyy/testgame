package com.mecola.testproject;

import MapsClasses.Logo1Screen;
import MapsClasses.Map001Testmap;
import MapsClasses.Map002Testmap2;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;

import com.badlogic.gdx.Game;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    ///private final class Map<Class<? extends Screen>, Screen> screenCatche = new HashMap<>();
    TiledMap map;
    OrthogonalTiledMapRenderer renderer;
    OrthographicCamera camera;
    private float timer = 0f;
    private boolean switched = false;
    private SpriteBatch batch;
    private Texture image;

    @Override
    public void create() {


        setScreen(new Logo1Screen(this));





       /* batch = new SpriteBatch();
        image = new Texture("libgdx.png");
        map = new TmxMapLoader().load("Maps/map1.tmx");

        renderer = new OrthogonalTiledMapRenderer(map);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);*/
    }


    @Override
    public void render() {
        super.render();

        if (!switched) {

            timer += Gdx.graphics.getDeltaTime();
            System.out.println("timer = " + timer);
            if (timer >= 2f) {
                System.out.println("Switching screen");
                setScreen(new Map002Testmap2());

                switched = true;
            }
        }
    }
}

