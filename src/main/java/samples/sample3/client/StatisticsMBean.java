package samples.sample3.client;

public interface StatisticsMBean {
    long getMaxResponseTime();
    long getMinResponseTime();
    long getSuccessesPerSecond();
    long getFailuresPerSecond();
}