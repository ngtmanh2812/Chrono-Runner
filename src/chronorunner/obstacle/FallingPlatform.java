package chronorunner.obstacle;

import chronorunner.core.Physics;
import chronorunner.entity.Character;
import chronorunner.world.Level;

import java.awt.Color;

/**
 * Bệ rơi: khi người chơi đặt chân lên, bệ rung lắc một nhịp rồi lao xuống.
 * Sau vài giây bệ hồi sinh về chỗ cũ để màn chơi không bị kẹt.
 */
public class FallingPlatform extends Platform {

    /** Các pha của bệ rơi. */
    private enum State {
        IDLE, SHAKING, FALLING, RESPAWNING
    }

    private static final double SHAKE_TIME = 0.55;
    private static final double RESPAWN_TIME = 2.6;

    private final double originX;
    private final double originY;

    private State state = State.IDLE;
    private double timer;
    private double shakeOffset;

    public FallingPlatform(double x, double y, double w, double h) {
        super(x, y, w, h);
        this.originX = x;
        this.originY = y;
        this.oneWay = true;
        this.bodyColor = new Color(0x7A5A3A);
        this.edgeColor = new Color(0xD9A05B);
    }

    @Override
    public void onStand(Character c) {
        if (state == State.IDLE) {
            state = State.SHAKING;
            timer = SHAKE_TIME;
        }
    }

    @Override
    protected void move(Level level, double dt) {
        switch (state) {
            case IDLE:
                shakeOffset = 0;
                return;
            case SHAKING:
                timer -= dt;
                shakeOffset = Math.sin(age * 70) * 2.2;
                if (timer <= 0) {
                    state = State.FALLING;
                    velY = 40;
                }
                return;
            case FALLING:
                velY += Physics.GRAVITY * 0.85 * dt;
                y += velY * dt;
                if (y > level.worldHeight() + 200) {
                    state = State.RESPAWNING;
                    timer = RESPAWN_TIME;
                    visible = false;
                }
                return;
            case RESPAWNING:
                timer -= dt;
                if (timer <= 0) {
                    moveTo(originX, originY);
                    velY = 0;
                    visible = true;
                    state = State.IDLE;
                }
                return;
            default:
        }
    }

    @Override
    public boolean blocks(Character c) {
        return super.blocks(c) && state != State.RESPAWNING;
    }

    @Override
    public void render(java.awt.Graphics2D g) {
        if (state == State.SHAKING || state == State.FALLING) {
            g.translate(shakeOffset, 0);
            super.render(g);
            g.translate(-shakeOffset, 0);
            return;
        }
        super.render(g);
    }
}
