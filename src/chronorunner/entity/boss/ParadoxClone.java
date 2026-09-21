package chronorunner.entity.boss;

import chronorunner.core.Physics;
import chronorunner.entity.Particle;
import chronorunner.entity.Player;
import chronorunner.entity.enemy.Enemy;
import chronorunner.util.Collision;
import chronorunner.world.Level;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Bản sao của The Paradox ở phase 2.
 *
 * <p>Trong khi còn bản sao, cơ thể chính của trùm được bảo vệ.
 * Người chơi buộc phải tiêu diệt hết bản sao trước.</p>
 */
public class ParadoxClone extends Enemy {

    private static final Color BODY = new Color(0x8A3FBF);
    private static final Color GLOW = new Color(0xFF5FD2);

    private final TheParadox master;
    private double flicker;

    public ParadoxClone(double x, double y, TheParadox master) {
        super(x, y, 26, 38, 3);
        this.master = master;
        this.contactDamage = 1;
        this.coinReward = 5;
        this.friction = 2400;
    }

    @Override
    protected void think(Level level, double dt) {
        flicker += dt * 6;
        applyGravity(level, dt);

        Player p = level.player();
        if (p != null && p.isAlive()) {
            int dir = p.centerX() > centerX() ? 1 : -1;
            velX = Collision.approach(velX, dir * Physics.GUARDIAN_CHASE_SPEED * 0.85, 900 * dt);
            faceTowards(p.centerX());

            // Bản sao nhảy khi người chơi ở trên cao
            if (onGround && p.centerY() < centerY() - 40) {
                velY = Physics.JUMP_VELOCITY * 0.85;
            }
        }

        moveWithCollision(level, dt);
        checkOutOfBounds(level);
    }

    @Override
    protected void onDeath() {
        super.onDeath();
        if (master != null) {
            master.onCloneDefeated();
        }
    }

    @Override
    protected void dropLoot(Level level) {
        // Bản sao tan biến chứ không rơi tiền
        Particle.burst(level, centerX(), centerY(), 18, 170, GLOW, 0.55);
    }

    @Override
    public void render(Graphics2D g) {
        int rx = (int) Math.round(x);
        int ry = (int) Math.round(y);
        int rw = (int) Math.round(width);
        int rh = (int) Math.round(height);

        int alpha = (int) (150 + 70 * Math.sin(flicker));

        g.setColor(new Color(BODY.getRed(), BODY.getGreen(), BODY.getBlue(), alpha));
        g.fillRoundRect(rx, ry, rw, rh, 8, 8);
        g.setColor(new Color(GLOW.getRed(), GLOW.getGreen(), GLOW.getBlue(), alpha));
        g.drawRoundRect(rx, ry, rw, rh, 8, 8);

        // Mắt
        g.setColor(new Color(255, 255, 255, alpha));
        g.fillRect(rx + 5, ry + 9, 6, 3);
        g.fillRect(rx + rw - 11, ry + 9, 6, 3);

        // Vệt nhiễu
        g.setColor(new Color(GLOW.getRed(), GLOW.getGreen(), GLOW.getBlue(), 70));
        for (int i = 0; i < 3; i++) {
            int off = (int) (Math.sin(flicker + i) * 6);
            g.drawLine(rx + off, ry + 14 + i * 7, rx + rw + off, ry + 14 + i * 7);
        }
    }
}
