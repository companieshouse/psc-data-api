package uk.gov.companieshouse.pscdataapi.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PscRolesTest {

    @ParameterizedTest
    @MethodSource("provideParameters")
    void getRole(PscRoles role, String expected) {
        assertEquals(expected, role.getRole());
    }

    private static Stream<Arguments> provideParameters() {
        return Stream.of(
                Arguments.of(PscRoles.INDIVIDUAL_PSC, "individual-person-with-significant-control"),
                Arguments.of(PscRoles.CORPORATE_PSC, "corporate-entity-person-with-significant-control"),
                Arguments.of(PscRoles.LEGAL_PSC, "legal-person-person-with-significant-control"),
                Arguments.of(PscRoles.SUPER_SECURE_PSC, "super-secure-person-with-significant-control"),
                Arguments.of(PscRoles.INDIVIDUAL_BO, "individual-beneficial-owner"),
                Arguments.of(PscRoles.CORPORATE_BO, "corporate-entity-beneficial-owner"),
                Arguments.of(PscRoles.LEGAL_BO, "legal-person-beneficial-owner"),
                Arguments.of(PscRoles.SUPER_SECURE_BO, "super-secure-beneficial-owner")
        );
    }
}
