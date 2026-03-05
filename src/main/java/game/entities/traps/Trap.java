package game.entities.traps;

import game.entities.GameEntity;

public abstract class Trap extends GameEntity {

    protected Trap(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    @Override
    public void update(double deltaSeconds) {

    }


    public abstract double getExplosionForceMultiplier();
}
