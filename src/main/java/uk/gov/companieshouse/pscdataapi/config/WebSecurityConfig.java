package uk.gov.companieshouse.pscdataapi.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.interceptor.UserAuthenticationInterceptor;

@Configuration
public class WebSecurityConfig implements WebMvcConfigurer {

    List<String> externalMethods = Arrays.asList("GET");
    List<String> otherAllowedAuthMethods = Arrays.asList("oauth2");

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userAuthenticationInterceptor());
    }

    @Bean
    public InternalUserInterceptor internalUserInterceptor() {
        return new InternalUserInterceptor();
    }

    /**
     * Create UserAuthenticationInterceptor.
     */
    @Bean
    public UserAuthenticationInterceptor userAuthenticationInterceptor() {
        return new UserAuthenticationInterceptor(externalMethods, 
                                                 otherAllowedAuthMethods, 
                                                 internalUserInterceptor());
    }
}
