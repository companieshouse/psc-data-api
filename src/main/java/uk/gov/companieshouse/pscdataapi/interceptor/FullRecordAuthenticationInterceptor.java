package uk.gov.companieshouse.pscdataapi.interceptor;

import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;

@Component
public class FullRecordAuthenticationInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final AuthenticationHelper authHelper;

    public FullRecordAuthenticationInterceptor(AuthenticationHelper authHelper) {
        this.authHelper = authHelper;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        final String identityType = authHelper.getAuthorisedIdentityType(request);
        Map<String, Object> logMap = DataMapHolder.getLogMap();

        if (authHelper.isOauth2IdentityType(identityType)) {

            if (authHelper.isTokenProtected(request)) {
                return true;
            } else {
                LOGGER.errorRequest(request, "User not authorised. Token has insufficient permissions.", logMap);
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
                return false;
            }
        }

        if (!authHelper.isApiKeyIdentityType(identityType)) {
            logMap.put("identityType", identityType);
            LOGGER.errorRequest(request, "User not authorised. Identity type not correct", logMap);

            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return false;
        }

        if (!authHelper.isKeyElevatedPrivilegesAuthorised(request)) {
            logMap.put("privileges", Arrays.asList(authHelper.getApiKeyPrivileges(request)));
            LOGGER.errorRequest(request,
                    "User not authorised. API key does not have sufficient privileges.",
                    logMap);

            response.setStatus(HttpStatus.SC_FORBIDDEN);
            return false;
        }

        return true;
    }
}
