package com.mecola.testproject;

import com.badlogic.gdx.Input;
import com.mecola.testproject.DebugConsole;

public class ConsoleInput implements com.badlogic.gdx.InputProcessor
{


    private DebugConsole console;


    public ConsoleInput(DebugConsole console){

        this.console = console;

    }



    @Override
    public boolean keyTyped(char character) {


        if(!console.isActive())
            return false;



        console.input(character);


        return true;

    }



    @Override
    public boolean keyDown(int keycode) {


        if(!console.isActive())
            return false;



        if(keycode == Input.Keys.ENTER){


            console.execute();


        }



        if(keycode == Input.Keys.BACKSPACE){


            console.backspace();


        }



        return true;

    }




    public boolean keyUp(int keycode){return false;}


    public boolean touchDown(int x,int y,int p,int b){return false;}

    public boolean touchUp(int x,int y,int p,int b){return false;}

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    public boolean touchDragged(int x,int y,int p){return false;}

    public boolean mouseMoved(int x,int y){return false;}

    public boolean scrolled(float a,float b){return false;}


}
