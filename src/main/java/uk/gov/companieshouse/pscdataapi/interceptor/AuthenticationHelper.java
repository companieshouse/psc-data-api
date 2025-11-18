package uk.gov.companieshouse.pscdataapi.interceptor;

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
}
