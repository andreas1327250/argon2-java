package at.gadermaier.argon2.algorithm;

import at.gadermaier.argon2.model.Instance;
import at.gadermaier.argon2.model.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static at.gadermaier.argon2.Constants.ARGON2_SYNC_POINTS;

public class FillMemory {

    public static void fillMemoryBlocks(Instance instance, ExecutorService executor) {
        if (instance.getLanes() == 1) {
            fillMemoryBlockSingleThreaded(instance);
        } else {
            fillMemoryBlockMultiThreaded(instance, executor);
        }
    }

    private static void fillMemoryBlockSingleThreaded(Instance instance) {
        for (int i = 0; i < instance.getIterations(); i++) {
            for (int j = 0; j < ARGON2_SYNC_POINTS; j++) {
                Position position = new Position(i, 0, j, 0);
                FillSegment.fillSegment(instance, position);
            }
        }
    }

    private static void fillMemoryBlockMultiThreaded(final Instance instance, ExecutorService executor) {

        List<Future<?>> futures = new ArrayList<Future<?>>();

        for (int i = 0; i < instance.getIterations(); i++) {
            for (int j = 0; j < ARGON2_SYNC_POINTS; j++) {
                for (int k = 0; k < instance.getLanes(); k++) {

                    final Position position = new Position(i, k, j, 0);

                    Future future = executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            FillSegment.fillSegment(instance, position);
                        }
                    });

                    futures.add(future);
                }

                joinThreads(instance, futures);
            }
        }
    }

    private static void joinThreads(Instance instance, List<Future<?>> futures) {
        try {
            for (Future<?> f : futures) {
                f.get();
            }
        } catch (InterruptedException e) {
            instance.clear();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            instance.clear();
            throw new RuntimeException(e);
        }
    }
}
