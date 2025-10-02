package uk.gov.companieshouse.pscdataapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeatureFlags {

    private final boolean streamHookDisabled;
    private final boolean identityVerificationEnabled;
    private final boolean pscOfficerIvdDeltaUpdatesEnabled;

    public FeatureFlags(@Value("${feature.seeding_collection_enabled}") final boolean streamHookDisabled,
            @Value("${feature.identity_verification}") final boolean identityVerificationEnabled,
        @Value("${feature.psc_officer_ivd_delta_updates}") final boolean pscOfficerIvdDeltaUpdatesEnabled) {
        this.streamHookDisabled = streamHookDisabled;
        this.identityVerificationEnabled = identityVerificationEnabled;
        this.pscOfficerIvdDeltaUpdatesEnabled = pscOfficerIvdDeltaUpdatesEnabled;
    }

    public boolean isStreamHookDisabled() {
        return streamHookDisabled;
    }

    public boolean isIdentityVerificationEnabled() {
        return identityVerificationEnabled;
    }

    public boolean isPscOfficerIvdDeltaUpdatesEnabled() {
        return pscOfficerIvdDeltaUpdatesEnabled;
    }
}
