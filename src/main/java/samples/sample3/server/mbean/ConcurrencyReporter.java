package samples.sample3.server.mbean;

import samples.sample3.server.Endpoint;

public class ConcurrencyReporter implements ConcurrencyReporterMBean {
    @Override
    public int getCurrentConcurrency() {
        return Endpoint.getConcurrency();
    }
}
