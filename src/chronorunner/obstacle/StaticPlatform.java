package chronorunner.obstacle;

import chronorunner.world.Level;

import java.awt.Color;

/** Bệ đứng yên, chặn từ mọi phía. */
public class StaticPlatform extends Platform {

    public StaticPlatform(double x, double y, double w, double h) {
        super(x, y, w, h);
        this.oneWay = false;
        this.bodyColor = new Color(0x4C5666);
        this.edgeColor = new Color(0x8FA6BC);
    }

    /** Bệ đứng yên nhưng chỉ tồn tại ở một số thời đại. */
    public StaticPlatform(double x, double y, double w, double h, int timeMask) {
        this(x, y, w, h);
        this.timeMask = timeMask;
        this.temporalActive = true;
    }

    @Override
    protected void move(Level level, double dt) {
        // đứng yên
    }
}
