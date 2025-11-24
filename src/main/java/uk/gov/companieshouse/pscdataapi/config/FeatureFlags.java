package uk.gov.companieshouse.pscdataapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeatureFlags {

    private final boolean streamHookDisabled;

    public FeatureFlags(@Value("${feature.seeding_collection_enabled}") final boolean streamHookDisabled) {
        this.streamHookDisabled = streamHookDisabled;
    }

    public boolean isStreamHookDisabled() {
        return streamHookDisabled;
    }

}
