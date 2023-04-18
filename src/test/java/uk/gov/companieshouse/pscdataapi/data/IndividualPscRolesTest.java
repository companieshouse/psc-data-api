package uk.gov.companieshouse.pscdataapi.data;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.Assert.assertEquals;

class IndividualPscRolesTest {

    @ParameterizedTest
    @MethodSource("provideParameters")
    void includes(String role, boolean isMatch) {
        assertEquals(IndividualPscRoles.includes(role), isMatch);
    }

    private static Stream<Arguments> provideParameters() {
        return Stream.of(
                Arguments.of("individual-person-with-significant-control", true),
                Arguments.of("individual-incorrect-psc", false),
                Arguments.of("corporate-entity-beneficial-owner", false),
                Arguments.of("individual-person-significant-control", false),
                Arguments.of("legal-person-person-with-significant-control", true),
                Arguments.of("legal-person-with-significant-control", false)
        );
    }

}
