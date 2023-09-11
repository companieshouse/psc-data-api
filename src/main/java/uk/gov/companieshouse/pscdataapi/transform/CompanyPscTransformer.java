package uk.gov.companieshouse.pscdataapi.transform;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.Individual;
import uk.gov.companieshouse.api.psc.SensitiveData;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscdataapi.data.IndividualPscRoles;
import uk.gov.companieshouse.pscdataapi.data.SecurePscRoles;
import uk.gov.companieshouse.pscdataapi.models.Address;
import uk.gov.companieshouse.pscdataapi.models.DateOfBirth;
import uk.gov.companieshouse.pscdataapi.models.NameElements;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.models.PscSensitiveData;
import uk.gov.companieshouse.pscdataapi.models.Updated;
import uk.gov.companieshouse.pscdataapi.util.PscTransformationHelper;

import javax.xml.transform.TransformerException;


@Component
public class CompanyPscTransformer {

    @Autowired
    private Logger logger;

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS");


    /**
     * Transform PSC.
     * @param optionalPscDocument PSC.
     * @return PSC mongo Document.
     */
    public Individual transformPscDocToIndividual(Optional<PscDocument> optionalPscDocument) throws TransformerException
    {



        if(optionalPscDocument.isPresent()){
            PscDocument pscDocument = optionalPscDocument.get();
            Individual individual = new Individual();
            if(individual.getEtag() == null){
                logger.error("Etag is null");
            }
            else{individual.setEtag(pscDocument.getData().getEtag());}
            if(individual.getNotifiedOn()== null){
                logger.error("NotifiedOn is null");
            }
            else{individual.setNotifiedOn(LocalDate.parse(pscDocument.getDeltaAt()));}
            if(individual.getCeasedOn() == null){
                logger.error("CeasedOn is null");
            }
            else{individual.setCeasedOn(pscDocument.getData().getCeasedOn());}
            if(individual.getKind() == null){
                logger.error("Kind is null");
            }
            else{individual.setKind(Individual.KindEnum.INDIVIDUAL_PERSON_WITH_SIGNIFICANT_CONTROL);}
            if(individual.getCountryOfResidence() == null){
                logger.error("CountryOfResidence is null");
            }
            else{individual.setCountryOfResidence(pscDocument.getData().getCountryOfResidence());}
            if(individual.getName() == null){
                logger.error("Name is null");
            }
            else{individual.setName(pscDocument.getData().getName());}
            if(individual.getNameElements() == null){
                logger.error("NameElements is null");
            }
            else{individual.setNameElements(pscDocument.getData().getNameElements());}
            if(individual.getDateOfBirth() == null){
                logger.error("DateOfBirth is null");
            }
            else{individual.setDateOfBirth(pscDocument.getSensitiveData().getDateOfBirth());}
            if(individual.getAddress() == null){
                logger.error("Address is null");
            }
            else{individual.setAddress(pscDocument.getData().getAddress());}
            if(individual.getNaturesOfControl() == null){
                logger.error("NaturesOfControl is null");
            }
            else{individual.setNaturesOfControl(pscDocument.getData().getNaturesOfControl());}
            if(individual.getLinks() == null){
                logger.error("Links is null");
            }
            else{individual.setLinks(pscDocument.getData().getLinks());}
            return individual;
        }
        else {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,"PscDocument not found");
        }

    }


    /**
     * Transform PSC.
     * @param notificationId PSC Id.
     * @param requestBody request payload.
     * @return PSC mongo Document.
     */
    public PscDocument transformPsc(String notificationId, FullRecordCompanyPSCApi requestBody) {
        PscDocument pscDocument = new PscDocument();
        logger.info(String.format("transforming incoming payload with Id: %s", notificationId));

        pscDocument.setId(notificationId);
        pscDocument.setNotificationId(notificationId);
        pscDocument.setPscId(requestBody.getExternalData().getPscId());
        pscDocument.setCompanyNumber(requestBody.getExternalData().getCompanyNumber());
        OffsetDateTime deltaAt = requestBody.getInternalData().getDeltaAt();
        pscDocument.setDeltaAt(dateTimeFormatter.format(deltaAt));
        pscDocument.setUpdated(new Updated().setAt(LocalDate.now()));
        pscDocument.setUpdatedBy(requestBody.getInternalData().getUpdatedBy());
        pscDocument.setData(transformDataFields(requestBody));
        pscDocument.setIdentification(requestBody.getExternalData().getData().getIdentification());

        String kind = requestBody.getExternalData().getData().getKind();

        if (IndividualPscRoles.includes(kind)) {
            pscDocument.setSensitiveData(transformSensitiveDataFields(requestBody));

            handleIndividualFields(requestBody, pscDocument.getData());
        }
        if (SecurePscRoles.includes(kind)) {
            handleSecureFields(requestBody, pscDocument.getData());
        } else {
            Address serviceAddress = new Address(requestBody.getExternalData()
                    .getData().getServiceAddress());
            pscDocument.getData().setAddress(serviceAddress);
        }
        return pscDocument;
    }

    private PscSensitiveData transformSensitiveDataFields(FullRecordCompanyPSCApi requestBody) {
        PscSensitiveData pscSensitiveData = new PscSensitiveData();
        SensitiveData sensitiveData = requestBody.getExternalData().getSensitiveData();
        DateOfBirth dateOfBirth = new DateOfBirth(sensitiveData.getDateOfBirth());
        pscSensitiveData.setResidentialAddressIsSameAsServiceAddress(requestBody.getExternalData()
                .getSensitiveData().getResidentialAddressSameAsServiceAddress());
        pscSensitiveData.setDateOfBirth(dateOfBirth);
        pscSensitiveData.setUsualResidentialAddress(
                new Address(sensitiveData.getUsualResidentialAddress()));

        return pscSensitiveData;
    }

    private PscData transformDataFields(FullRecordCompanyPSCApi requestBody) {
        PscData data = new PscData();
        data.setCeasedOn(requestBody.getExternalData().getData().getCeasedOn());
        data.setDescription(requestBody.getExternalData().getData().getDescription());
        data.setEtag(requestBody.getExternalData().getData().getEtag());
        data.setKind(requestBody.getExternalData().getData().getKind());
        data.setLinks(PscTransformationHelper.createLinks(requestBody));
        data.setName(requestBody.getExternalData().getData().getName());
        data.setNationality(requestBody.getExternalData().getData().getNationality());
        data.setNaturesOfControl(requestBody.getExternalData().getData().getNaturesOfControl());
        data.setSanctioned(requestBody.getExternalData().getData().getIsSanctioned());
        data.setServiceAddressIsSameAsRegisteredOfficeAddress(requestBody.getExternalData()
                    .getData().getServiceAddressSameAsRegisteredOfficeAddress());
        return data;
    }

    private void handleIndividualFields(FullRecordCompanyPSCApi requestBody, PscData data) {
        data.setNameElements(new NameElements(
                requestBody.getExternalData().getData().getNameElements()));
        data.setCountryOfResidence(requestBody.getExternalData().getData().getCountryOfResidence());
    }

    private void handleSecureFields(FullRecordCompanyPSCApi requestBody, PscData data) {
        Boolean ceased = requestBody.getExternalData().getData().getCeasedOn() != null;
        data.setCeased(ceased);
    }
}
