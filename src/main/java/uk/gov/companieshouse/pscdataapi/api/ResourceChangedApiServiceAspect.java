package uk.gov.companieshouse.pscdataapi.api;

import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.config.FeatureFlags;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;

@Aspect
@Component
@ConditionalOnProperty(prefix = "feature", name = "seeding_collection_enabled")
public class ResourceChangedApiServiceAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final FeatureFlags featureFlags;

    public ResourceChangedApiServiceAspect(FeatureFlags featureFlags) {
        this.featureFlags = featureFlags;
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
            LOGGER.debug("Stream hook disabled; not publishing change to chs-kafka-api", DataMapHolder.getLogMap());
            return null;
        } else {
            LOGGER.debug("Stream hook enabled; publishing change to chs-kafka-api", DataMapHolder.getLogMap());
            return proceedingJoinPoint.proceed();
        }
    }
}