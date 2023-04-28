package uk.gov.companieshouse.pscdataapi.util;

import java.time.LocalDateTime;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.ItemLinkTypes;
import uk.gov.companieshouse.pscdataapi.models.Created;
import uk.gov.companieshouse.pscdataapi.models.Links;
import uk.gov.companieshouse.pscdataapi.models.NameElements;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.models.Updated;


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
}
