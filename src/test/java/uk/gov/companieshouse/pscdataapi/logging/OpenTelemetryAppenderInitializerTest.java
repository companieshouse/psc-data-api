package uk.gov.companieshouse.pscdataapi.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class OpenTelemetryAppenderInitializerTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(OpenTelemetry.class, () -> mock(OpenTelemetry.class))
            .withUserConfiguration(OpenTelemetryAppenderInitializer.class);

    @Test
    void shouldInstallAppenderWithOpenTelemetryInstance() {
        // given
        final OpenTelemetry openTelemetry = mock(OpenTelemetry.class);

        try (MockedStatic<OpenTelemetryAppender> appender = mockStatic(OpenTelemetryAppender.class)) {
            // when
            new OpenTelemetryAppenderInitializer(openTelemetry).afterPropertiesSet();

            // then
            appender.verify(() -> OpenTelemetryAppender.install(openTelemetry));
        }
    }

    @Test
    void shouldNotCreateBeanWhenDisabledByDefault() {
        contextRunner.run(context ->
                assertThat(context).doesNotHaveBean(OpenTelemetryAppenderInitializer.class));
    }

    @Test
    void shouldCreateBeanWhenEnabled() {
        try (MockedStatic<OpenTelemetryAppender> ignored = mockStatic(OpenTelemetryAppender.class)) {
            contextRunner.withPropertyValues("management.opentelemetry.enabled=true")
                    .run(context -> assertThat(context).hasSingleBean(OpenTelemetryAppenderInitializer.class));
        }
    }
}
