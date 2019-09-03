package samples.sample3.client.mbean;

public interface ResponseTimesMBean {
    long getMaxResponseTime();
    long getMinResponseTime();
    long getRequestsPerSecond();
}