package at.gadermaier.argon2.algorithm;

import at.gadermaier.argon2.model.Instance;
import at.gadermaier.argon2.model.Position;

import static at.gadermaier.argon2.Constants.ARGON2_SYNC_POINTS;

public class FillMemory {

    public static void fillMemoryBlocks(Instance instance) {

        if (instance.getLanes() == 1) {
            fillMemoryBlockSingleThreaded(instance);
        } else {
            fillMemoryBlockMultiThreaded(instance);
        }
    }

    private static void fillMemoryBlockSingleThreaded(Instance instance) {
        for (int i = 0; i < instance.getPasses(); i++) {
            for (int j = 0; j < ARGON2_SYNC_POINTS; j++) {
                Position position = new Position(i, 0, j, 0);
                FillSegment.fillSegment(instance, position);
            }
        }
    }

    private static void fillMemoryBlockMultiThreaded(Instance instance) {

        Thread[] threads = new Thread[instance.getLanes()];

        for (int i = 0; i < instance.getPasses(); i++) {
            for (int j = 0; j < ARGON2_SYNC_POINTS; j++) {
                for (int k = 0; k < instance.getLanes(); k++) {

                    /* Create thread */
                    Position position = new Position(i, k, j, 0);
                    FillSegmentRunnable runnable = new FillSegmentRunnable(instance, position);

                    threads[k] = new Thread(runnable);
                    threads[k].start();
                }

                /* Joining remaining threads */
                for (int k = 0; k < instance.getLanes(); k++) {
                    try {
                        threads[k].join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
