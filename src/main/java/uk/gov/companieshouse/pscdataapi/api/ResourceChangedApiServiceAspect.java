package uk.gov.companieshouse.pscdataapi.api;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscdataapi.config.FeatureFlags;

@Aspect
@Component
@ConditionalOnProperty(prefix = "feature", name = "seeding_collection_enabled")
public class ResourceChangedApiServiceAspect {

    private final FeatureFlags featureFlags;
    private final Logger logger;

    public ResourceChangedApiServiceAspect(FeatureFlags featureFlags, Logger logger) {
        this.featureFlags = featureFlags;
        this.logger = logger;
    }

    /**
     * Feature flag check.
     *
     * @param proceedingJoinPoint the proceeding join point.
     * @return returns an object.
     * @throws Throwable throws something.
     */
    @Around("@annotation(StreamEvents)")
    public Object invokeChsKafkaApi(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        if (featureFlags.isStreamHookDisabled()) {
            logger.debug("Stream hook disabled; not publishing change to chs-kafka-api");
            return null;
        } else {
            logger.debug("Stream hook enabled; publishing change to chs-kafka-api");
            return proceedingJoinPoint.proceed();
        }
    }
}