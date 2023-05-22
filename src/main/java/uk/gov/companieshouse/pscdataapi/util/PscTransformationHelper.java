package uk.gov.companieshouse.pscdataapi.util;

import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.ItemLinkTypes;
import uk.gov.companieshouse.pscdataapi.models.Links;


public class PscTransformationHelper {

    private PscTransformationHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates Links field.
     * @param requestBody request payload.
     * @return Links object.
     */
    public static Links createLinks(FullRecordCompanyPSCApi requestBody) {
        Links links = new Links();
        ItemLinkTypes itemLinkTypes = requestBody.getExternalData()
                .getData().getLinks().get(0);
        links.setSelf(itemLinkTypes.getSelf());
        links.setStatements(itemLinkTypes.getStatements());
        return links;
    }

    /**
     * Maps kind from FullRecordCompanyPSCApi object to a valid resource kind for Chs kafka api.
     * @param kind psc kind
     * @return String containing valid resource kind
     */
    public static String mapResourceKind(String kind) {
        String validResourceKind = new String();

        switch (kind) {
            case "individual-person-with-significant-control":
                validResourceKind = "company-psc-individual";
                break;
            case "corporate-entity-person-with-significant-control":
                validResourceKind = "company-psc-corporate";
                break;
            case "legal-person-person-with-significant-control":
                validResourceKind = "company-psc-legal";
                break;
            case "super-secure-person-with-significant-control":
                validResourceKind = "company-psc-supersecure";
                break;
            case "individual-beneficial-owner":
            case "corporate-entity-beneficial-owner":
            case "legal-person-beneficial-owner":
            case "super-secure-beneficial-owner":
                validResourceKind = kind;
                break;
            default:
        }

        return validResourceKind;
    }
}
