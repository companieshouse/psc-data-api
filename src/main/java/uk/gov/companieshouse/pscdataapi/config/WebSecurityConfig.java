package uk.gov.companieshouse.pscdataapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;

@Configuration
public class WebSecurityConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(internalUserInterceptor());
    }

    @Bean
    public InternalUserInterceptor internalUserInterceptor() {
        return new InternalUserInterceptor();
    }
}
