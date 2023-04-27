package uk.gov.companieshouse.pscdataapi.data;

import java.util.EnumSet;
import java.util.stream.Stream;

public enum IndividualPscRoles {
    INDIVIDUAL_PSC("individual-person-with-significant-control"),
    INDIVIDUAL_BO("individual-beneficial-owner");

    private String role;

    IndividualPscRoles(String role) {
        this.role = role;
    }

    public static boolean includes(final PscRoles role) {
        return includes(role.getRole());
    }

    /**
    * Checks if enum contains entry.
    * @param role the entry to check
    * */
    public static boolean includes(final String role) {
        return EnumSet.allOf(IndividualPscRoles.class).stream()
                .map(IndividualPscRoles::getRole)
                .anyMatch(role::equals);
    }

    public static Stream<IndividualPscRoles> stream() {
        return Stream.of(IndividualPscRoles.values());
    }

    public String getRole() {
        return role;
    }
}
