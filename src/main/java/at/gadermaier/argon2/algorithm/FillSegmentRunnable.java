package at.gadermaier.argon2.algorithm;

import at.gadermaier.argon2.model.ThreadData;

public class FillSegmentRunnable implements Runnable {

    private ThreadData threadData;

    FillSegmentRunnable(ThreadData threadData) {
        this.threadData = threadData;
    }

    @Override
    public void run() {
        FillSegment.fillSegment(threadData.instance, threadData.position);
    }
}
