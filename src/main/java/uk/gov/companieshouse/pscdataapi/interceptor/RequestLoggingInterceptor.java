package uk.gov.companieshouse.pscdataapi.interceptor;

import static uk.gov.companieshouse.logging.util.LogContextProperties.REQUEST_ID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.RequestLogger;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;

public class RequestLoggingInterceptor implements HandlerInterceptor, RequestLogger {

    private final Logger logger;

    public RequestLoggingInterceptor(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        logStartRequestProcessing(request, logger);
        DataMapHolder.initialise(Optional
                .ofNullable(request.getHeader(REQUEST_ID.value()))
                .orElse(UUID.randomUUID().toString()));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        logEndRequestProcessing(request, response, logger);
        DataMapHolder.clear();
    }
}
