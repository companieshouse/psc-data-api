package uk.gov.companieshouse.pscdataapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeatureFlags {

    private final boolean streamHookDisabled;
    private final boolean individualPscIdentityVerificationEnabled;
    private final boolean individualPscFullRecordAddVerificationStateEnabled;

    public FeatureFlags(@Value("${feature.seeding_collection_enabled}") final boolean streamHookDisabled,
        @Value("${feature.identity_verification}") final boolean individualPscIdentityVerificationEnabled,
        @Value("${feature.psc_individual_full_record_add_verification_state}") final boolean individualPscFullRecordAddVerificationStateEnabled) {
        this.streamHookDisabled = streamHookDisabled;
        this.individualPscIdentityVerificationEnabled = individualPscIdentityVerificationEnabled;
        this.individualPscFullRecordAddVerificationStateEnabled = individualPscFullRecordAddVerificationStateEnabled;
    }

    public boolean isStreamHookDisabled() {
        return streamHookDisabled;
    }

    public boolean isIndividualPscIdentityVerificationEnabled() {
        return individualPscIdentityVerificationEnabled;
    }

    public boolean isIndividualPscFullRecordAddVerificationStateEnabled() {
        return individualPscFullRecordAddVerificationStateEnabled;
    }
}
