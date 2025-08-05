package uk.gov.companieshouse.pscdataapi.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.filter.CustomCorsFilter;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.pscdataapi.interceptor.AuthenticationHelper;
import uk.gov.companieshouse.pscdataapi.interceptor.AuthenticationHelperImpl;
import uk.gov.companieshouse.pscdataapi.interceptor.FullRecordAuthenticationInterceptor;

@Configuration
public class WebSecurityConfig implements WebMvcConfigurer {

    // feature flag marked for future removal
    @Value("${feature.identity_verification:false}")
    private Boolean identityVerificationEnabled;

    public static final String PATTERN_FULL_RECORD =
            "/company/{company_number}/persons-with-significant-control/individual/{notification_id}/full_record";
    public static final String PATTERN_VERIFICATION_STATE =
            "/company/{company_number}/persons-with-significant-control/individual/{notification_id}/identity-verification-details";

    List<String> otherAllowedAuthMethods = Arrays.asList("oauth2");

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(userAuthenticationInterceptor());
        if (Boolean.TRUE.equals(identityVerificationEnabled)) {
            registry.addInterceptor(internalUserInterceptor())
                    .addPathPatterns(PATTERN_VERIFICATION_STATE);
            registry.addInterceptor(fullRecordAuthenticationInterceptor())
                    .addPathPatterns(PATTERN_FULL_RECORD);
        }
    }

    @Override
    public void configurePathMatch(final PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(true);
    }

    @Bean
    public InternalUserInterceptor internalUserInterceptor() {
        return new InternalUserInterceptor();
    }

    @Bean
    public FullRecordAuthenticationInterceptor fullRecordAuthenticationInterceptor() {
        return new FullRecordAuthenticationInterceptor(authenticationHelper());
    }

    @Bean
    @Primary
    public AuthenticationHelper authenticationHelper() {
        return new AuthenticationHelperImpl();
    }

    /**
     * Create UserAuthenticationInterceptor.
     */
    @Bean
    public UserAuthenticationInterceptor userAuthenticationInterceptor() {
        return new UserAuthenticationInterceptor(externalMethods(),
                otherAllowedAuthMethods,
                internalUserInterceptor());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http.cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new CustomCorsFilter(externalMethods()), CsrfFilter.class);

        return http.build();
    }

    @Bean
    public List<String> externalMethods() {
        return Arrays.asList(HttpMethod.GET.name());
    }
}
