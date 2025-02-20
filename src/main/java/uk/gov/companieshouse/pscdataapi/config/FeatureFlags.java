package uk.gov.companieshouse.pscdataapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeatureFlags {

    private final boolean streamHookDisabled;
    private final boolean individualPscFullRecordGetEnabled;
    private final boolean individualPscFullRecordAddVerificationStateEnabled;

    public FeatureFlags(@Value("${feature.seeding_collection_enabled}") final boolean streamHookDisabled,
        @Value("${feature.psc_individual_full_record_get}") final boolean individualPscFullRecordGetEnabled,
        @Value("${feature.psc_individual_full_record_add_verification_state}") final boolean individualPscFullRecordAddVerificationStateEnabled) {
        this.streamHookDisabled = streamHookDisabled;
        this.individualPscFullRecordGetEnabled = individualPscFullRecordGetEnabled;
        this.individualPscFullRecordAddVerificationStateEnabled = individualPscFullRecordAddVerificationStateEnabled;
    }

    public boolean isStreamHookDisabled() {
        return streamHookDisabled;
    }

    public boolean isIndividualPscFullRecordGetEnabled() {
        return individualPscFullRecordGetEnabled;
    }

    public boolean isIndividualPscFullRecordAddVerificationStateEnabled() {
        return individualPscFullRecordAddVerificationStateEnabled;
    }
}
