package at.gadermaier.argon2.algorithm;

import static at.gadermaier.argon2.Constants.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import at.gadermaier.argon2.model.Instance;
import at.gadermaier.argon2.model.Position;

public class FillMemory {

    public static void fillMemoryBlocks(Instance instance, ExecutorService executor) {
        if (instance.getLanes() == 1) {
            fillMemoryBlockSingleThreaded(instance);
        } else {
            fillMemoryBlockMultiThreaded(instance, executor);
        }
    }

    private static void fillMemoryBlockSingleThreaded(Instance instance) {
    	FillSegment worker = new FillSegment();
        for (int i = 0; i < instance.getIterations(); i++) {
            for (int j = 0; j < ARGON2_SYNC_POINTS; j++) {
                Position position = new Position(i, 0, j, 0);
                worker.fillSegment(instance, position);
            }
        }
    }

    private static void fillMemoryBlockMultiThreaded(final Instance instance, ExecutorService executor) {

        Future<?>[] futures = new Future<?>[instance.getLanes()];
    	FillSegment[] workers = new FillSegment[instance.getLanes()];
        for (int k = 0; k < instance.getLanes(); k++) {
        	workers[k] = new FillSegment();
        }

        for (int i = 0; i < instance.getIterations(); i++) {
            for (int j = 0; j < ARGON2_SYNC_POINTS; j++) {
                for (int k = 0; k < instance.getLanes(); k++) {
                    final Position position = new Position(i, k, j, 0);
                    final FillSegment worker = workers[k];
                    
                    futures[k] = executor.submit(new Runnable() {
                        @Override
                        public void run() {
                        	worker.fillSegment(instance, position);
                        }
                    });
                }

                joinThreads(instance, futures);
            }
        }
    }

    private static void joinThreads(Instance instance, Future<?>[] futures) {
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
