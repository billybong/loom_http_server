package samples.sample3.client;

public interface StatsMBean {
    long getMaxResponseTime();
    long getMinResponseTime();
    long getSuccessesPerSecond();
    long getFailuresPerSecond();
}