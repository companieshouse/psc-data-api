package uk.gov.companieshouse.pscdataapi.data;

import static org.junit.Assert.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CorporatePscRolesTest {

    @ParameterizedTest
    @MethodSource("provideParameters")
    void includes(String role, boolean isMatch) {
        assertEquals(CorporatePscRoles.includes(role), isMatch);
    }

    private static Stream<Arguments> provideParameters() {
        return Stream.of(
                Arguments.of("corporate-entity-person-with-significant-control", true),
                Arguments.of("individual-incorrect-psc", false),
                Arguments.of("corporate-entity-beneficial-owner", true),
                Arguments.of("individual-person-significant-control", false),
                Arguments.of("corporate-owner", false),
                Arguments.of("corporate-entity-owner", false),
                Arguments.of("legal-person-with-significant-control", false),
                Arguments.of("legal-person-person-with-significant-control", true)
        );
    }

}
