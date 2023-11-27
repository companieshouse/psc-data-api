package uk.gov.companieshouse.pscdataapi.api;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

public class ResourceChangedApiServiceAsepct {
    @Aspect
    @Component
    @ConditionalOnProperty(prefix = "feature", name = "seeding_collection_enabled")
    public class ResourceChangedApiServiceAspect {
        @Around("@annotation(StreamEvents)")
        public Object checkStreamEventsEnabled() {
            return null;
        }

    }
}
