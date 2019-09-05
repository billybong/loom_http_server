package samples.sample3.client;

import java.util.concurrent.atomic.AtomicLong;

public class Stats {
    public final AtomicLong maxResponseTime = new AtomicLong();
    public final AtomicLong minResponseTime = new AtomicLong(-1);
    public final AtomicLong successCount = new AtomicLong();
    public final AtomicLong failureCounts = new AtomicLong();
    public final AtomicLong successesPerSecond = new AtomicLong();
    public final AtomicLong failuresPerSecond = new AtomicLong();
}
