package uk.gov.companieshouse.pscdataapi.transform;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.data.IndividualPscRoles;
import uk.gov.companieshouse.pscdataapi.data.SecurePscRoles;
import uk.gov.companieshouse.pscdataapi.exceptions.FailedToTransformException;
import uk.gov.companieshouse.pscdataapi.models.Created;
import uk.gov.companieshouse.pscdataapi.models.NameElements;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.models.Updated;


@Component
public class CompanyPscTransformer {

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS");

    private static final Logger logger = LoggerFactory.getLogger("psc-data-api");


    /**
     * Transform PSC.
     * @param notificationId PSC Id.
     * @param requestBody request payload.
     * @return PSC mongo Document.
     */
    public PscDocument transformPsc(String notificationId, FullRecordCompanyPSCApi requestBody) {
        PscDocument pscDocument = new PscDocument();

        try {
            pscDocument.setId(notificationId);
            pscDocument.setNotificationId(notificationId);
            pscDocument.setPscId(requestBody.getExternalData().getPscId());
            pscDocument.setCompanyNumber(requestBody.getExternalData().getCompanyNumber());
            OffsetDateTime deltaAt = requestBody.getInternalData().getDeltaAt();
            pscDocument.setDeltaAt(dateTimeFormatter.format(deltaAt));
            Created created = new Created();
            created.setAt(LocalDateTime.parse(requestBody.getInternalData().getCreatedAt()));
            Updated updated = new Updated();
            updated.setAt(requestBody.getInternalData().getUpdatedAt());
            pscDocument.setCreated(created);
            pscDocument.setUpdatedBy(requestBody.getInternalData().getUpdatedBy());
            pscDocument.setUpdated(updated);
            pscDocument.setSensitiveData(requestBody.getExternalData().getSensitiveData());
            manageDataFields(requestBody, pscDocument);
        } catch (Exception exception) {
            throw new FailedToTransformException(String.format(
                    "Failed to transform API payload: %s", exception.getMessage()));
        }

        return pscDocument;
    }

    private void manageDataFields(FullRecordCompanyPSCApi requestBody, PscDocument pscDocument) {
        PscData data = new PscData();

        data.setAddress(requestBody.getExternalData().getData().getServiceAddress());
        data.setCeasedOn(requestBody.getExternalData().getData().getCeasedOn());
        data.setDescription(requestBody.getExternalData().getData().getDescription());
        data.setEtag(requestBody.getExternalData().getData().getEtag());
        data.setKind(requestBody.getExternalData().getData().getKind());
        data.setLinks(requestBody.getExternalData().getData().getLinks());
        data.setName(requestBody.getExternalData().getData().getName());
        data.setNationality(requestBody.getExternalData().getData().getNationality());
        data.setNaturesOfControl(requestBody.getExternalData().getData().getNaturesOfControl());
        data.setResidentialAddressIsSameAsServiceAddress(requestBody.getExternalData()
                    .getData().getResidentialAddressIsSameAsServiceAddress());
        data.setSanctioned(requestBody.getExternalData().getData().getIsSanctioned());
        data.setServiceAddressIsSameAsRegisteredOfficeAddress(requestBody.getExternalData()
                    .getData().getServiceAddressSameAsRegisteredOfficeAddress());

        String kind = requestBody.getExternalData().getData().getKind();

        if (IndividualPscRoles.includes(kind)) {
            handleIndividualFields(requestBody, data);
        }
        if (SecurePscRoles.includes(kind)) {
            handleSecureFields(requestBody, data);
        }
        pscDocument.setData(data);
    }

    private void handleIndividualFields(FullRecordCompanyPSCApi requestBody, PscData data) {
        createNameElements(requestBody, data);
        data.setCountryOfResidence(requestBody.getExternalData().getData().getCountryOfResidence());
    }

    private void handleSecureFields(FullRecordCompanyPSCApi requestBody, PscData data) {
        Boolean ceased = requestBody.getExternalData().getData().getCeasedOn() != null;
        data.setCeased(ceased);
    }

    private void createNameElements(FullRecordCompanyPSCApi requestBody, PscData data) {
        NameElements nameElements = new NameElements();
        nameElements.setTitle(requestBody.getExternalData().getData().getTitle());
        nameElements.setForename(requestBody.getExternalData().getData().getForename());
        nameElements.setMiddleName(requestBody.getExternalData().getData().getOtherForenames());
        nameElements.setSurname(requestBody.getExternalData().getData().getSurname());
        data.setNameElements(nameElements);
    }
}
