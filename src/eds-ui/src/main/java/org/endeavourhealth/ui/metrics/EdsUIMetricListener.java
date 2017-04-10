package org.endeavourhealth.ui.metrics;

import com.codahale.metrics.MetricRegistry;

import com.codahale.metrics.servlets.MetricsServlet;


public class EdsUIMetricListener extends MetricsServlet.ContextListener {
    public static final MetricRegistry edsMetricRegistry = EdsInstrumentedFilterContextListener.REGISTRY;

    @Override
    protected MetricRegistry getMetricRegistry() {
        return edsMetricRegistry;
    }
}
