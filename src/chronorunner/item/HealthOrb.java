package chronorunner.item;

import chronorunner.entity.FloatingText;
import chronorunner.entity.Player;
import chronorunner.world.Level;

import java.awt.Color;
import java.awt.Graphics2D;

/** Quả cầu sinh mệnh: hồi máu. */
public class HealthOrb extends Item {

    private static final Color RED = new Color(0xFF6B6B);
    /** Số máu hồi lại. */
    public static final int HEAL_AMOUNT = 1;

    private final int healAmount;

    public HealthOrb(double x, double y) {
        this(x, y, HEAL_AMOUNT);
    }

    public HealthOrb(double x, double y, int healAmount) {
        super(x, y, 16);
        this.healAmount = healAmount;
    }

    @Override
    public void apply(Player player, Level level) {
        if (player.health() >= player.maxHealth()) {
            // Đã đầy máu thì quy đổi thành năng lượng, tránh vật phẩm vô dụng.
            player.addTimeEnergy(15);
            level.spawn(new FloatingText(centerX() - 10, top() - 6, "+NĂNG LƯỢNG", RED, 0.8));
        } else {
            player.heal(healAmount);
            level.spawn(new FloatingText(centerX() - 12, top() - 6, "+" + healAmount + " HP", RED, 0.9));
        }
    }

    @Override
    public Color color() {
        return RED;
    }

    @Override
    protected void renderShape(Graphics2D g, int cx, int cy, int size) {
        int r = size / 2;
        g.setColor(new Color(0x5C1B1B));
        g.fillOval(cx - r, cy - r, r * 2, r * 2);
        g.setColor(RED);
        g.drawOval(cx - r, cy - r, r * 2, r * 2);

        g.setColor(new Color(255, 255, 255, 220));
        g.fillRect(cx - 2, cy - r + 3, 4, r * 2 - 6);
        g.fillRect(cx - r + 3, cy - 2, r * 2 - 6, 4);
    }
}
