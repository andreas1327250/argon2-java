package at.gadermaier.argon2.algorithm;

import at.gadermaier.argon2.model.Instance;
import at.gadermaier.argon2.model.Position;
import at.gadermaier.argon2.model.ThreadData;

import static at.gadermaier.argon2.Constants.ARGON2_SYNC_POINTS;

public class FillMemory {

    public static void fillMemoryBlocks(Instance instance) {

        if (instance.getThreads() == 1) {
            fillMemoryBlockSingleThreaded(instance);
        } else {
            fillMemoryBlockMultiThreaded(instance);
        }
    }

    private static void fillMemoryBlockSingleThreaded(Instance instance) {

        for (int i = 0; i < instance.getPasses(); i++) {
            for (int j = 0; j < ARGON2_SYNC_POINTS; j++) {
                for (int k = 0; k < instance.getLanes(); k++) {
                    Position position = new Position(i, k, (byte) j, 0);
                    FillSegment.fillSegment(instance, position);
                }
            }
        }
    }

    private static void fillMemoryBlockMultiThreaded(Instance instance) {

        Thread[] threads = new Thread[instance.getLanes()];
        ThreadData[] threadData = new ThreadData[instance.getLanes()];

        for (int i = 0; i < instance.getPasses(); i++) {
            for (int j = 0; j < ARGON2_SYNC_POINTS; j++) {

                 /* 2. Calling threads */
                for (int k = 0; k < instance.getLanes(); k++) {

                    /* 2.1 Join a thread if limit is exceeded */
                    if (k >= instance.getThreads()) {
                        try {
                            threads[k - instance.getThreads()].join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    /* 2.2 Create thread */
                    Position position = new Position(i, k, (byte) j, 0);
                    threadData[k] = new ThreadData();
                    threadData[k].instance = instance; /* preparing the thread input */
                    threadData[k].position = position;

                    FillSegmentRunnable runnable = new FillSegmentRunnable(threadData[k]);
                    threads[k] = new Thread(runnable);
                    threads[k].start();
                }

                /* 3. Joining remaining threads */
                for (int k = instance.getLanes() - instance.getThreads(); k < instance.getLanes(); k++) {
                    try {
                        threads[k].join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
