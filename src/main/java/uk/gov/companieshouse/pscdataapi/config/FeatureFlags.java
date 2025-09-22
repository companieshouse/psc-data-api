package uk.gov.companieshouse.pscdataapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeatureFlags {

    private final boolean streamHookDisabled;
    private final boolean identityVerificationEnabled;

    public FeatureFlags(@Value("${feature.seeding_collection_enabled}") final boolean streamHookDisabled,
            @Value("${feature.identity_verification}") final boolean identityVerificationEnabled) {
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
