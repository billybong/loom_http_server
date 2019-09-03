package samples.sample3.client.mbean;

import samples.sample3.client.LoadGenerator;

public class ResponseTimes implements ResponseTimesMBean {

    private final LoadGenerator loadGenerator;

    public ResponseTimes(LoadGenerator loadGenerator) {
        this.loadGenerator = loadGenerator;
    }

    public long getMaxResponseTime() {
        return loadGenerator.maxResponseTime.get();
    }

    public long getMinResponseTime() {
        return loadGenerator.minResponseTime.get();
    }

    public long getRequestsPerSecond() {
        return loadGenerator.requestsPerSecond.get();
    }
}