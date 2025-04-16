package uk.gov.companieshouse.pscdataapi.interceptor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticationHelperImplTest {

    private AuthenticationHelper testHelper;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        testHelper = new AuthenticationHelperImpl();
    }

    @Test
    void getAuthorisedIdentityType() {
        String expected = "identity-type";

        when(request.getHeader("ERIC-Identity-Type")).thenReturn(expected);

        assertThat(testHelper.getAuthorisedIdentityType(request), is(expected));
    }

    @Test
    void isApiKeyIdentityTypeWhenItIs() {
        assertThat(testHelper.isApiKeyIdentityType("key"), is(true));
    }

    @Test
    void isApiKeyIdentityTypeWhenItIsNot() {
        assertThat(testHelper.isApiKeyIdentityType("KEY"), is(false));
    }

    @Test
    void isOauth2IdentityTypeWhenItIs() {
        assertThat(testHelper.isOauth2IdentityType("oauth2"), is(true));
    }

    @Test
    void isOauth2IdentityTypeWhenItIsNot() {
        assertThat(testHelper.isOauth2IdentityType("Oauth2"), is(false));
    }

    @Test
    void getAuthorisedUser() {
        String expected = "authorised-user";

        when(request.getHeader("ERIC-Authorised-User")).thenReturn(expected);

        assertThat(testHelper.getAuthorisedUser(request), is(expected));
    }

    @Test
    void getKeyPrivileges() {
        Map<String, String[]> testValues = new HashMap<>();
        testValues.put("role-1", new String[]{"role-1"});
        testValues.put("role-1,role-2", new String[]{"role-1", "role-2"});

        testValues.forEach((headerValue, expectedPrivileges) -> {
            when(request.getHeader("ERIC-Authorised-Key-Privileges")).thenReturn(headerValue);

            assertThat(testHelper.getApiKeyPrivileges(request), is(expectedPrivileges));
        });
    }

    @Test
    void isKeyElevatedPrivilegesAuthorisedWhenItIsPUT() {
        when(request.getHeader("ERIC-Authorised-Key-Privileges"))
                .thenReturn("other-role,internal-app");
        when(request.getMethod())
                .thenReturn("PUT");

        assertThat(testHelper.isKeyElevatedPrivilegesAuthorised(request), is(true));
    }

    @Test
    void isKeyElevatedPrivilegesAuthorisedWhenItIsNotPUT() {
        when(request.getHeader("ERIC-Authorised-Key-Privileges")).thenReturn("role-1,sensitive-data");
        when(request.getMethod())
                .thenReturn("PUT");

        assertThat(testHelper.isKeyElevatedPrivilegesAuthorised(request), is(false));
    }

    @Test
    void isKeyElevatedPrivilegesAuthorisedWhenItIsGET() {
        when(request.getHeader("ERIC-Authorised-Key-Privileges"))
                .thenReturn("other-role,sensitive-data");
        when(request.getMethod())
                .thenReturn("GET");

        assertThat(testHelper.isKeyElevatedPrivilegesAuthorised(request), is(true));
    }

    @Test
    void isKeyElevatedPrivilegesAuthorisedWhenItIsNotGET() {
        when(request.getHeader("ERIC-Authorised-Key-Privileges")).thenReturn("role-1,internal-app");
        when(request.getMethod())
                .thenReturn("GET");

        assertThat(testHelper.isKeyElevatedPrivilegesAuthorised(request), is(false));
    }

    @Test
    void getTokenPrivileges() {
        String tokenValue = "company_pscs=readprotected";
        when(request.getHeader("ERIC-Authorised-Token-Permissions")).thenReturn(tokenValue);

        Map<String, List<String>> result = testHelper.getTokenPermissions(request);

        assertThat(result.containsKey("company_pscs"), is(true));
        assertThat(result.get("company_pscs").getFirst(), is("readprotected"));
    }

    @Test
    void getNoTokenPrivileges() {
        when(request.getHeader("ERIC-Authorised-Token-Permissions")).thenReturn(null);

        Map<String, List<String>> result = testHelper.getTokenPermissions(request);

        assertThat(result.size(), is(0));
    }

    @Test
    void isTokenProtectedIsTrue() {
        String tokenValue = "company_pscs=readprotected";
        when(request.getHeader("ERIC-Authorised-Token-Permissions")).thenReturn(tokenValue);

        assertThat(testHelper.isTokenProtected(request), is(true));
    }

    @Test
    void isTokenProtectedIsTrueButWrongValue() {
        String tokenValue = "company_officers=readprotected";
        when(request.getHeader("ERIC-Authorised-Token-Permissions")).thenReturn(tokenValue);

        assertThat(testHelper.isTokenProtected(request), is(false));
    }

    @Test
    void isTokenProtectedFalseWhenNotProtected() {
        String tokenValue = "company_pscs=read";
        when(request.getHeader("ERIC-Authorised-Token-Permissions")).thenReturn(tokenValue);

        assertThat(testHelper.isTokenProtected(request), is(false));
    }

}
