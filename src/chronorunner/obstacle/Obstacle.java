package chronorunner.obstacle;

import chronorunner.entity.GameObject;
import chronorunner.world.Level;

/**
 * Nhóm vật thể tĩnh / cơ chế của màn chơi: bệ, bẫy, cổng thời gian, cổng ra.
 *
 * <p>Lớp này chỉ bổ sung tuổi thọ vật thể; các hành vi cụ thể do
 * {@link Platform} và {@link Trap} định nghĩa.</p>
 */
public abstract class Obstacle extends GameObject {

    protected Obstacle(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    @Override
    public void update(Level level, double dt) {
        age += dt;
    }
}
