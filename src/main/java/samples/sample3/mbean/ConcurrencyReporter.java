package samples.sample3.mbean;

import samples.sample3.Endpoint;

public class ConcurrencyReporter implements ConcurrencyReporterMBean {
    @Override
    public int getCurrentConcurrency() {
        return Endpoint.getConcurrency();
    }
}
