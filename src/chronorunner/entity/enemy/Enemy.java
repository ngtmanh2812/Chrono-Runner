package chronorunner.entity.enemy;

import chronorunner.core.Physics;
import chronorunner.core.TimeState;
import chronorunner.entity.Character;
import chronorunner.entity.Particle;
import chronorunner.entity.Player;
import chronorunner.item.ChronoCoin;
import chronorunner.world.Level;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Lớp cơ sở cho mọi kẻ địch.
 *
 * <p>Quy ước chung:</p>
 * <ul>
 *   <li>Kẻ địch chỉ hoạt động khi tồn tại ở thời đại hiện tại
 *       (một số loài chỉ sống ở QUÁ KHỨ hoặc TƯƠNG LAI).</li>
 *   <li>Khi thời gian bị đóng băng, kẻ địch đứng yên hoàn toàn.</li>
 *   <li>Chết thì rơi Chrono Coin và phát hiệu ứng vỡ.</li>
 * </ul>
 */
public abstract class Enemy extends Character {

    protected int contactDamage = 1;
    protected int coinReward = 3;
    protected boolean damageable = true;
    protected boolean stompable = true;
    protected boolean freezable = true;

    protected double hitFlash;
    protected double attackTimer;
    protected double frozenPulse;

    protected Enemy(double x, double y, double w, double h, int maxHealth) {
        super(x, y, w, h, maxHealth);
        this.timeMask = TimeState.ALL;
    }

    @Override
    public void update(Level level, double dt) {
        age += dt;

        if (hitFlash > 0) {
            hitFlash -= dt;
        }
        if (invulnTimer > 0) {
            invulnTimer -= dt;
        }
        if (attackTimer > 0) {
            attackTimer -= dt;
        }

        if (!alive) {
            return;
        }

        // Không tồn tại ở thời đại này -> "ngủ đông".
        if (!existsIn(level.timeState())) {
            return;
        }

        // Bị đóng băng thời gian.
        if (level.isTimeFrozen() && freezable) {
            frozenPulse += dt;
            onFrozenTick(level, dt);
            return;
        }

        think(level, dt);
        contactDamageToPlayer(level);
    }

    /** Hành vi riêng của từng loài. */
    protected abstract void think(Level level, double dt);

    /**
     * Hook được gọi mỗi bước khi kẻ địch đang bị đóng băng.
     * Trùm dùng hook này để phá giải đóng băng của người chơi.
     */
    protected void onFrozenTick(Level level, double dt) {
    }

    /** Hình dáng riêng, vẽ trong hệ toạ độ thế giới. */
    @Override
    public abstract void render(Graphics2D g);

    // ---- Sát thương --------------------------------------------------------
    @Override
    public void takeDamage(int amount, double sourceX) {
        if (!damageable || !alive) {
            return;
        }
        super.damage(amount, sourceX);
        hitFlash = 0.16;
        if (alive) {
            // Kẻ địch hồi phục nhanh hơn người chơi để tránh "tank" đòn.
            invulnTimer = Physics.ENEMY_COUNTDOWN_AFTER_HIT;
            onHit(sourceX);
        }
    }

    protected void onHit(double sourceX) {
    }

    @Override
    protected void applyKnockback(double sourceX) {
        int dir = centerX() < sourceX ? -1 : 1;
        velX = dir * 110;
        velY = -140;
    }

    @Override
    protected void onDeath() {
        velX = 0;
        velY = 0;
    }

    /** Rơi tiền và tạo hiệu ứng khi bị tiêu diệt. */
    protected void dropLoot(Level level) {
        int coins = Math.max(1, coinReward / Physics.COIN_VALUE);
        for (int i = 0; i < coins; i++) {
            double ox = (Math.random() - 0.5) * width;
            level.spawn(new ChronoCoin(centerX() + ox - 7, centerY()));
        }
        Particle.burst(level, centerX(), centerY(), 14, 150, new Color(0xFFD166), 0.5);
    }

    protected void contactDamageToPlayer(Level level) {
        Player p = level.player();
        if (p == null || !p.isAlive()) {
            return;
        }
        if (intersects(p.getX(), p.getY(), p.getWidth(), p.getHeight())) {
            p.onEnemyContact(this, contactDamage);
        }
    }

    /** Kẻ địch có đang bị đóng băng bởi Time Freeze hay không. */
    public boolean isFrozenNow(Level level) {
        return level.isTimeFrozen() && freezable;
    }

    /** Kẻ địch này có phải trùm (được hiển thị thanh máu lớn) hay không. */
    public boolean isBoss() {
        return false;
    }

    /** Tên hiển thị trên thanh máu trùm. */
    public String displayName() {
        return getClass().getSimpleName();
    }

    public boolean isDamageable() {
        return damageable;
    }

    public boolean isStompable() {
        return stompable;
    }

    public int coinReward() {
        return coinReward;
    }

    public int contactDamage() {
        return contactDamage;
    }

    /** Vẽ lớp phủ khi trúng đòn / bị đóng băng. */
    protected void renderStatusOverlay(Graphics2D g, Level level) {
        if (hitFlash > 0) {
            g.setColor(new Color(255, 255, 255, (int) (200 * (hitFlash / 0.16))));
            g.fillRoundRect((int) Math.round(x) - 2, (int) Math.round(y) - 2,
                    (int) Math.round(width) + 4, (int) Math.round(height) + 4, 8, 8);
        }
        if (isFrozenNow(level)) {
            g.setColor(new Color(150, 220, 255, 90));
            g.fillRoundRect((int) Math.round(x) - 3, (int) Math.round(y) - 3,
                    (int) Math.round(width) + 6, (int) Math.round(height) + 6, 8, 8);
            g.setColor(new Color(220, 245, 255, 200));
            g.drawLine((int) Math.round(x) - 3, (int) Math.round(centerY()),
                    (int) Math.round(x + width) + 3, (int) Math.round(centerY()));
        }
    }

    /** Kẻ địch có rơi xuống dưới màn chơi thì tự huỷ. */
    protected void checkOutOfBounds(Level level) {
        if (y > level.worldHeight() + 200) {
            alive = false;
        }
    }

    protected boolean solidBelow(Level level, double px, double py) {
        return level.solidInRect(px - 2, py, 4, 6);
    }
}
