package uk.gov.companieshouse.pscdataapi.interceptor;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

/**
 * Helper class for user authentication
 */
@Component
public class AuthenticationHelperImpl implements AuthenticationHelper {

    public static final String OAUTH2_IDENTITY_TYPE = "oauth2";
    public static final String API_KEY_IDENTITY_TYPE = "key";

    public static final String INTERNAL_APP_PRIVILEGE = "internal-app";
    private static final String SENSITIVE_DATA_PRIVILEGE = "sensitive-data";
    public static final String ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER
            = "ERIC-Authorised-Key-Privileges";
    private static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    private static final String ERIC_AUTHORISED_USER = "ERIC-Authorised-User";

    private static final String GET_METHOD = "GET";

    @Override
    public String getAuthorisedIdentityType(HttpServletRequest request) {
        return getRequestHeader(request, ERIC_IDENTITY_TYPE);
    }

    @Override
    public boolean isApiKeyIdentityType(final String identityType) {
        return API_KEY_IDENTITY_TYPE.equals(identityType);
    }

    @Override
    public String getAuthorisedUser(HttpServletRequest request) {
        return getRequestHeader(request, ERIC_AUTHORISED_USER);
    }

    @Override
    public String[] getApiKeyPrivileges(HttpServletRequest request) {
        // Could be null if header is not present
        final String commaSeparatedPrivilegeString = request
                .getHeader(ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER);

        return Optional.ofNullable(commaSeparatedPrivilegeString)
                .map(v -> v.split(","))
                .orElse(new String[]{});
    }

    @Override
    public boolean isKeyElevatedPrivilegesAuthorised(HttpServletRequest request) {
        String[] privileges = getApiKeyPrivileges(request);
        return request.getMethod().equals(GET_METHOD) ? ArrayUtils.contains(privileges, SENSITIVE_DATA_PRIVILEGE) :
                ArrayUtils.contains(privileges, INTERNAL_APP_PRIVILEGE);
    }

    private String getRequestHeader(HttpServletRequest request, String header) {
        return request == null ? null : request.getHeader(header);
    }
}
