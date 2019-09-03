package samples.sample3.client;

public interface LoadGeneratorMBean {
    long getMaxResponseTime();
    long getMinResponseTime();
    long getSuccessesPerSecond();
    long getFailuresPerSecond();
}