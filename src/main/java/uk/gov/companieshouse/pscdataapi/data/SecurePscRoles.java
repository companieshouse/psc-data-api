package uk.gov.companieshouse.pscdataapi.data;

import java.util.EnumSet;
import java.util.stream.Stream;

public enum SecurePscRoles {
    SUPER_SECURE_PSC("super-secure-person-with-significant-control"),
    SUPER_SECURE_BO("super-secure-beneficial-owner");

    private String role;

    SecurePscRoles(String role) {

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
        return EnumSet.allOf(SecurePscRoles.class).stream()
                .map(SecurePscRoles::getRole)
                .anyMatch(role::equals);
    }

    public static Stream<SecurePscRoles> stream() {
        return Stream.of(SecurePscRoles.values());
    }

    public String getRole() {
        return role;
    }
}
