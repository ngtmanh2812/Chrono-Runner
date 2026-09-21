package chronorunner.core;

import chronorunner.util.Collision;

import java.awt.Graphics2D;
import java.util.Random;

/**
 * Camera 2D bám theo người chơi, có giới hạn trong biên màn chơi
 * và hiệu ứng rung (screen shake) khi va chạm mạnh.
 */
public class Camera {

    private final int viewWidth;
    private final int viewHeight;

    private double x;
    private double y;

    private double shakeTime;
    private double shakeMagnitude;
    private double shakeOffsetX;
    private double shakeOffsetY;

    private final Random random = new Random(1337L);

    public Camera(int viewWidth, int viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

    /**
     * Bám theo mục tiêu với độ trễ mềm, sau đó kẹp vào biên thế giới.
     */
    public void follow(double targetX, double targetY, double worldW, double worldH, double dt) {
        double desiredX = targetX - viewWidth * 0.5;
        double desiredY = targetY - viewHeight * 0.55;

        double smooth = 1.0 - Math.pow(0.0015, dt);
        x = Collision.lerp(x, desiredX, smooth);
        y = Collision.lerp(y, desiredY, smooth);

        clampToWorld(worldW, worldH);
    }

    /** Đặt camera ngay lập tức (khi bắt đầu màn hoặc hồi sinh). */
    public void snapTo(double targetX, double targetY, double worldW, double worldH) {
        x = targetX - viewWidth * 0.5;
        y = targetY - viewHeight * 0.55;
        clampToWorld(worldW, worldH);
    }

    private void clampToWorld(double worldW, double worldH) {
        if (worldW <= viewWidth) {
            x = (worldW - viewWidth) * 0.5;
        } else {
            x = Collision.clamp(x, 0, worldW - viewWidth);
        }
        if (worldH <= viewHeight) {
            y = (worldH - viewHeight) * 0.5;
        } else {
            y = Collision.clamp(y, 0, worldH - viewHeight);
        }
    }

    public void update(double dt) {
        if (shakeTime > 0) {
            shakeTime -= dt;
            double falloff = Math.max(0, shakeTime);
            shakeOffsetX = (random.nextDouble() * 2 - 1) * shakeMagnitude * falloff;
            shakeOffsetY = (random.nextDouble() * 2 - 1) * shakeMagnitude * falloff;
            if (shakeTime <= 0) {
                shakeOffsetX = 0;
                shakeOffsetY = 0;
            }
        }
    }

    /** Rung camera trong `time` giây với biên độ `magnitude` pixel. */
    public void shake(double magnitude, double time) {
        if (magnitude >= shakeMagnitude * Math.max(0.001, shakeTime)) {
            shakeMagnitude = magnitude;
            shakeTime = time;
        }
    }

    public void applyTo(Graphics2D g) {
        g.translate(-Math.round(getX()), -Math.round(getY()));
    }

    public double getX() {
        return x + shakeOffsetX;
    }

    public double getY() {
        return y + shakeOffsetY;
    }

    public int viewWidth() {
        return viewWidth;
    }

    public int viewHeight() {
        return viewHeight;
    }

    /** Chuyển toạ độ thế giới -> toạ độ màn hình. */
    public double toScreenX(double worldX) {
        return worldX - getX();
    }

    public double toScreenY(double worldY) {
        return worldY - getY();
    }

    /** Chuyển toạ độ màn hình -> toạ độ thế giới (dùng cho chuột). */
    public double toWorldX(double screenX) {
        return screenX + getX();
    }

    public double toWorldY(double screenY) {
        return screenY + getY();
    }
}
