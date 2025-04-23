package uk.gov.companieshouse.pscdataapi.api;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.pscdataapi.config.FeatureFlags;


@ExtendWith(MockitoExtension.class)
class ResourceChangedApiServiceAspectTest {

    @InjectMocks
    private ResourceChangedApiServiceAspect resourceChangedApiServiceAspect;
    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;
    @Mock
    private Object object;
    @Mock
    private FeatureFlags featureFlags;

    @Test
    void testAspectDoesNotProceedWhenFlagEnabled() throws Throwable {
        //given
        when(featureFlags.isStreamHookDisabled()).thenReturn(true);
        // when
        Object actual = resourceChangedApiServiceAspect.invokeChsKafkaApi(proceedingJoinPoint);

        // then
        assertNull(actual);
        verifyNoInteractions(proceedingJoinPoint);
    }

    @Test
    void testAspectProceedsWhenFlagDisabled() throws Throwable {
        //given
        when(proceedingJoinPoint.proceed()).thenReturn(object);
        //when
        Object actual = resourceChangedApiServiceAspect.invokeChsKafkaApi(proceedingJoinPoint);
        //then
        assertSame(object, actual);
        verify(proceedingJoinPoint).proceed();
    }
}