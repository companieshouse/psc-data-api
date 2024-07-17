package uk.gov.companieshouse.pscdataapi.util;

import uk.gov.companieshouse.api.psc.Data;
import uk.gov.companieshouse.api.psc.ItemLinkTypes;
import uk.gov.companieshouse.pscdataapi.models.Links;


public class PscTransformationHelper {

    private PscTransformationHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates Links field.
     *
     * @param data the Data in ExternalData in the request payload.
     * @return Links object.
     */
    public static Links createLinks(Data data, String pscStatementId) {
        Links links = new Links();
        if (data.getLinks() != null) {
            ItemLinkTypes itemLinkTypes = data.getLinks().get(0);
            links.setSelf(itemLinkTypes.getSelf());

            if (pscStatementId != null) {
                links.setStatement(itemLinkTypes.getStatement());
            } else {
                links.setStatement(null);
            }
        }
        return links;
    }

    /**
     * Maps kind from FullRecordCompanyPSCApi object to a valid resource kind for Chs kafka api.
     *
     * @param kind psc kind
     * @return String containing valid resource kind
     */
    public static String mapResourceKind(final String kind) {
        return switch (kind) {
            case "individual-person-with-significant-control" -> "company-psc-individual";
            case "corporate-entity-person-with-significant-control" -> "company-psc-corporate";
            case "legal-person-person-with-significant-control" -> "company-psc-legal";
            case "super-secure-person-with-significant-control" -> "company-psc-supersecure";
            case "individual-beneficial-owner", "corporate-entity-beneficial-owner", "legal-person-beneficial-owner",
                 "super-secure-beneficial-owner" -> kind;
            default -> "";
        };
    }
}
