package com.zebrunner.carina.core.registrar.report;

import com.zebrunner.agent.core.config.ReportingConfiguration;
import com.zebrunner.agent.core.registrar.Label;
import com.zebrunner.carina.utils.config.Configuration;

public class DefaultReportingRule implements ReportingRule {

    @Override
    public void attachTestRunLabels() {
        Configuration.get("branch")
                .ifPresent(branch -> Label.attachToTestRun("branch", branch));
    }

    @Override
    public void modifyReportingConfiguration(ReportingConfiguration configuration) {
        // todo add
    }

}
