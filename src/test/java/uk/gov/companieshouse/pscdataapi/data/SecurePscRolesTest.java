package uk.gov.companieshouse.pscdataapi.data;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.Assert.assertEquals;

class SecurePscRolesTest {

    @ParameterizedTest
    @MethodSource("provideParameters")
    void includes(String role, boolean isMatch) {
        assertEquals(SecurePscRoles.includes(role), isMatch);
    }

    private static Stream<Arguments> provideParameters() {
        return Stream.of(
                Arguments.of("super-secure-person-with-significant-control", true),
                Arguments.of("individual-incorrect-psc", false),
                Arguments.of("super-secure-beneficial-owner", true),
                Arguments.of("individual-person-significant-control", false),
                Arguments.of("corporate-owner", false),
                Arguments.of("corporate-entity-owner", false)
        );
    }

}
