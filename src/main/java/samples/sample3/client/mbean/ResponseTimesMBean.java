package samples.sample3.client.mbean;

public interface ResponseTimesMBean {
    long getMaxDuration();
    long getMinDuration();
    long getConcurrentOutboundRequests();
}
