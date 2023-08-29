package com.zebrunner.carina.core.registrar.report;

import com.zebrunner.agent.core.config.ReportingConfiguration;

public interface ReportingRule {

    /**
     *
     */
    default void attachTestRunLabels() {
        // do nothing
    }

    // todo we cannot just mutate report configuration
    default void modifyReportingConfiguration(ReportingConfiguration configuration) {
        // do nothing
    }
}
