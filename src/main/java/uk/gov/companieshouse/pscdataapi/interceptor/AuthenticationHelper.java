package uk.gov.companieshouse.pscdataapi.interceptor;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Helper class for authenticating users
 */
public interface AuthenticationHelper {

    /**
     * Returns the authorised identity type
     *
     * @param request the {@link HttpServletRequest}
     * @return the identity type
     */
    String getAuthorisedIdentityType(HttpServletRequest request);

    /**
     * Verifies that the identity type is key
     *
     * @param identityType the identify type to be checked
     * @return true if the identity type is the key
     */
    boolean isApiKeyIdentityType(final String identityType);

    /**
     * Verifies that the identity type is Oauth2
     *
     * @param identityType the identity type to be checked
     * @return true if the identity type is Oauth2
     */
    boolean isOauth2IdentityType(final String identityType);

    /**
     * Returns the authorised user information
     *
     * @param request the {@link HttpServletRequest}
     * @return the authorised user
     */
    String getAuthorisedUser(HttpServletRequest request);

    /**
     * Returns the privileges granted to the API key
     *
     * @param request the {@link HttpServletRequest}
     * @return the privileges of the API key
     */
    String[] getApiKeyPrivileges(HttpServletRequest request);

    /**
     * Checks whether the key has elevated privileges
     *
     * @param request the {@link HttpServletRequest}
     * @return true if the key has elevated privileges
     */
    boolean isKeyElevatedPrivilegesAuthorised(HttpServletRequest request);

    /**
     * Returns the permissions granted to the OAuth2 Token
     *
     * @param request the {@link HttpServletRequest}
     * @return the privileges of the OAuth2 Token
     */
    Map<String, List<String>> getTokenPermissions(HttpServletRequest request);

    /**
     * Checks whether the token has required permissions
     *
     * @param request the {@link HttpServletRequest}
     * @return true if the token has required permissions
     */
    boolean isTokenProtected(HttpServletRequest request);
}
