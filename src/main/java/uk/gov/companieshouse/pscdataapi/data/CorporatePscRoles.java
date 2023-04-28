package uk.gov.companieshouse.pscdataapi.data;

import java.util.EnumSet;
import java.util.stream.Stream;

public enum CorporatePscRoles {
    CORPORATE_PSC("corporate-entity-person-with-significant-control"),
    CORPORATE_BO("corporate-entity-beneficial-owner"),
    LEGAL_PSC("legal-person-person-with-significant-control"),
    LEGAL_BO("legal-person-beneficial-owner");

    private String role;

    CorporatePscRoles(String role) {
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
        return EnumSet.allOf(CorporatePscRoles.class).stream()
                .map(CorporatePscRoles::getRole)
                .anyMatch(role::equals);
    }


    public static Stream<CorporatePscRoles> stream() {
        return Stream.of(CorporatePscRoles.values());
    }

    public String getRole() {
        return role;
    }
}
