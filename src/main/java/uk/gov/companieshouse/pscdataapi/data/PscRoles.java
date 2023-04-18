package uk.gov.companieshouse.pscdataapi.data;

import java.util.stream.Stream;

public enum PscRoles {
    INDIVIDUAL_PSC("individual-person-with-significant-control"),
    CORPORATE_PSC("corporate-entity-person-with-significant-control"),
    LEGAL_PSC("legal-person-person-with-significant-control"),
    SUPER_SECURE_PSC("super-secure-person-with-significant-control"),

    INDIVIDUAL_BO("individual-beneficial-owner"),
    CORPORATE_BO("corporate-entity-beneficial-owner"),
    LEGAL_BO("legal-person-beneficial-owner"),
    SUPER_SECURE_BO("super-secure-beneficial-owner");

    private String role;

    PscRoles(String role) {
        this.role = role;
    }

    public static Stream<PscRoles> stream() {
        return Stream.of(PscRoles.values());
    }

    public String getRole() {
        return role;
    }
}
