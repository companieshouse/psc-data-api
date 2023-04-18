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
     * Creates the created and updated fields.
     * @param requestBody request payload.
     * @param pscDocument output document.
     */
    public static void createDateFields(FullRecordCompanyPSCApi requestBody,
                                        PscDocument pscDocument) {
        Created created = new Created();
        created.setAt(LocalDateTime.parse(requestBody.getInternalData().getCreatedAt()));
        Updated updated = new Updated();
        updated.setAt(requestBody.getInternalData().getUpdatedAt());
        pscDocument.setUpdated(updated);
        pscDocument.setCreated(created);
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
     * Creates NameElements field.
     * @param requestBody request payload.
     * @return NameElements object.
     */
    public static NameElements createNameElements(FullRecordCompanyPSCApi requestBody) {
        NameElements nameElements = new NameElements();
        nameElements.setTitle(requestBody.getExternalData().getData().getTitle());
        nameElements.setForename(requestBody.getExternalData().getData().getForename());
        nameElements.setMiddleName(requestBody.getExternalData().getData().getOtherForenames());
        nameElements.setSurname(requestBody.getExternalData().getData().getSurname());
        return nameElements;
    }
}
