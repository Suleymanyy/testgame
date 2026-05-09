package com.mecola.testproject;

import com.badlogic.gdx.math.Polygon;

/**
 * Інтерфейс для перевірки колізій.
 * EntityKnight знає ЛИШЕ про цей інтерфейс — не про CollisionMap, не про Tiled.
 */
public interface CollisionProvider {
    boolean canMoveTo(Polygon hitbox);
}
