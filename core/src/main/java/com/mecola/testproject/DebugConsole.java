package com.mecola.testproject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;


public class DebugConsole {


    private boolean active = false;


    private BitmapFont font;

    private List<String> messages = new ArrayList<>();

    private String input = "";


    public DebugConsole() {

        font = new BitmapFont();

        log("Console initialized");
        log("Type 'quit' to exit");


    }



    public void toggle(){

        active = !active;

    }



    public boolean isActive(){

        return active;

    }



    public void log(String text){

        messages.add(text);


        // ограничиваем историю

        if(messages.size() > 15){

            messages.remove(0);

        }

    }



    public void render(SpriteBatch batch){


        if(!active)
            return;



        font.setColor(Color.GREEN);


        int y = Gdx.graphics.getHeight() - 30;



        for(String s : messages){


            font.draw(
                batch,
                s,
                20,
                y
            );


            y -= 25;

        }



        font.draw(
            batch,
            "> " + input,
            20,
            30
        );

    }



    public void input(char c){

        input += c;

    }



    public void backspace(){


        if(input.length()>0)

            input =
                input.substring(
                    0,
                    input.length()-1
                );

    }



    public void execute(){


        String command = input.trim();


        log("> " + command);



        if(command.equalsIgnoreCase("quit")){


            Gdx.app.exit();


        }



        input="";


    }


}
