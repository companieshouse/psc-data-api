package uk.gov.companieshouse.pscdataapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeatureFlags {

    private final boolean streamHookDisabled;
    private final boolean identityVerificationEnabled;

    public FeatureFlags(@Value("${feature.seeding_collection_enabled}") final boolean streamHookDisabled,
            @Value("${feature.identity_verification}") final boolean identityVerificationEnabled,
        @Value("${feature.psc_individual_full_record_add_identity_verification_details}") final boolean individualPscFullRecordAddidentityVerificationDetailsEnabled) {
        this.streamHookDisabled = streamHookDisabled;
        this.identityVerificationEnabled = identityVerificationEnabled;
    }

    public boolean isStreamHookDisabled() {
        return streamHookDisabled;
    }

    public boolean isIdentityVerificationEnabled() {
        return identityVerificationEnabled;
    }

}
