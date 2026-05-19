package com.mecola.testproject;

public interface CollidableEntity {

    float getX();
    float getY();

    float getPreviousX();
    float getPreviousY();

    void setPosition(float x, float y);
}
