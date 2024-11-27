package uk.gov.companieshouse.pscdataapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeatureFlags {

    private final boolean streamHookDisabled;
    private final boolean individualPscFullRecordGetEnabled;

    public FeatureFlags(@Value("${feature.seeding_collection_enabled}") final boolean streamHookDisabled,
        @Value("${feature.psc_individual_full_record_get}") final boolean individualPscFullRecordGetEnabled) {
        this.streamHookDisabled = streamHookDisabled;
        this.individualPscFullRecordGetEnabled = individualPscFullRecordGetEnabled;
    }

    public boolean isStreamHookDisabled() {
        return streamHookDisabled;
    }

    public boolean isIndividualPscFullRecordGetEnabled() {
        return individualPscFullRecordGetEnabled;
    }
}
