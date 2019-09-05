package samples.sample3.client;

import java.util.concurrent.atomic.AtomicLong;

public class Statistics implements StatisticsMBean {
    private final AtomicLong maxResponseTime = new AtomicLong();
    private final AtomicLong minResponseTime = new AtomicLong(-1);
    private final AtomicLong successCount = new AtomicLong();
    private final AtomicLong failureCounts = new AtomicLong();
    private final AtomicLong successesPerSecond = new AtomicLong();
    private final AtomicLong failuresPerSecond = new AtomicLong();

    Statistics() {
        FiberScope.background().schedule(() -> {
            while (true) {
                Thread.sleep(1000);
                this.resetMetrics();
            }
        });
    }

    private void resetMetrics() {
        maxResponseTime.set(0);
        minResponseTime.set(-1);
        failuresPerSecond.set(failureCounts.getAndSet(0));
        successesPerSecond.set(successCount.getAndSet(0));
    }

    void recordSuccess(long responseTime) {
        successCount.incrementAndGet();
        minResponseTime.getAndUpdate(i -> i == -1 ? responseTime : Math.min(i, responseTime));
        maxResponseTime.getAndUpdate(i -> Math.max(i, responseTime));
    }

    void recordFailure() {
        this.failureCounts.getAndIncrement();
    }

    @Override
    public long getMaxResponseTime() {
        return maxResponseTime.get();
    }

    @Override
    public long getMinResponseTime() {
        return minResponseTime.get();
    }

    @Override
    public long getSuccessesPerSecond() {
        return successesPerSecond.get();
    }

    @Override
    public long getFailuresPerSecond() {
        return failuresPerSecond.get();
    }
}
