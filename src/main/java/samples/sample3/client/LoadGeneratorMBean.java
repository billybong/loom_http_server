package samples.sample3.client;

public interface LoadGeneratorMBean {
    long getMaxResponseTime();
    long getMinResponseTime();
    long getRequestsPerSecond();
    long getFailuresPerSecond();
}