package at.gadermaier.argon2.algorithm;

import at.gadermaier.argon2.model.Instance;
import at.gadermaier.argon2.model.Position;

public class FillSegmentRunnable implements Runnable {

    private Instance instance;
    private Position position;

    FillSegmentRunnable(Instance instance, Position position) {
        this.instance = instance;
        this.position = position;
    }

    @Override
    public void run() {
        FillSegment.fillSegment(instance, position);
    }
}
