package uk.gov.companieshouse.pscdataapi.transform;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javax.xml.transform.TransformerException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.api.psc.CorporateEntityBeneficialOwner;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.Identification;
import uk.gov.companieshouse.api.psc.Individual;
import uk.gov.companieshouse.api.psc.IndividualBeneficialOwner;
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




@Component
public class CompanyPscTransformer {

    @Autowired
    private Logger logger;

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS");


    /**
     * Transform Individual PSC.
     * @param optionalPscDocument PSC.
     * @return PSC mongo Document.
     */
    public Individual transformPscDocToIndividual(Optional<PscDocument> optionalPscDocument)
            throws TransformerException {

        logger.info("Attempting to transform pscDocument to individual");

        if (optionalPscDocument.isPresent()) {
            PscDocument pscDocument = optionalPscDocument.get();
            Individual individual = new Individual();
            if (pscDocument.getData().getEtag() != null) {
                individual.setEtag(pscDocument.getData().getEtag());
            }
            if (pscDocument.getDeltaAt() != null) {
                individual.setNotifiedOn(LocalDate
                        .parse(pscDocument.getDeltaAt(),dateTimeFormatter));
            }
            individual.setKind(Individual.KindEnum.INDIVIDUAL_PERSON_WITH_SIGNIFICANT_CONTROL);
            if (pscDocument.getData().getCountryOfResidence() != null) {
                individual.setCountryOfResidence(pscDocument.getData().getCountryOfResidence());
            }
            if (pscDocument.getData().getName() != null) {
                individual.setName(pscDocument.getData().getName());
            }
            if (pscDocument.getData().getNameElements() != null) {
                NameElements nameElements = new NameElements();
                if (pscDocument.getData().getNameElements().getTitle() != null) {
                    nameElements.setTitle(pscDocument.getData().getNameElements().getTitle());
                }
                if (pscDocument.getData().getNameElements().getForename() != null) {
                    nameElements.setForename(pscDocument.getData().getNameElements().getForename());
                }
                if (pscDocument.getData().getNameElements().getMiddleName() != null) {
                    nameElements.setMiddleName(pscDocument.getData()
                            .getNameElements().getMiddleName());
                }
                if (pscDocument.getData().getNameElements().getSurname() != null) {
                    nameElements.setSurname(pscDocument.getData().getNameElements().getSurname());
                }
                individual.setNameElements(nameElements);
            }
            if (pscDocument.getData().getAddress() != null) {
                Address address = new Address();
                if (pscDocument.getData().getAddress().getAddressLine1() != null) {
                    address.setAddressLine1(pscDocument.getData().getAddress().getAddressLine1());
                }
                if (pscDocument.getData().getAddress().getAddressLine2() != null) {
                    address.setAddressLine2(pscDocument.getData().getAddress().getAddressLine2());
                }
                if (pscDocument.getData().getAddress().getCountry() != null) {
                    address.setCountry(pscDocument.getData().getAddress().getCountry());
                }
                if (pscDocument.getData().getAddress().getLocality() != null) {
                    address.setLocality(pscDocument.getData().getAddress().getLocality());
                }
                if (pscDocument.getData().getAddress().getPostalCode() != null) {
                    address.setPostalCode(pscDocument.getData().getAddress().getPostalCode());
                }
                if (pscDocument.getData().getAddress().getPremises() != null) {
                    address.setPremises(pscDocument.getData().getAddress().getPremises());
                }
                if (pscDocument.getData().getAddress().getRegion() != null) {
                    address.setRegion(pscDocument.getData().getAddress().getRegion());
                }
                if (pscDocument.getData().getAddress().getCareOf() != null) {
                    address.setCareOf(pscDocument.getData().getAddress().getCareOf());
                }
                if (pscDocument.getData().getAddress().getPoBox() != null) {
                    address.setPoBox(pscDocument.getData().getAddress().getPoBox());
                }
                individual.setAddress(address);
            }
            if (pscDocument.getData().getNaturesOfControl() != null) {
                individual.setNaturesOfControl(pscDocument.getData().getNaturesOfControl());
            }
            if (pscDocument.getData().getLinks() != null) {
                individual.setLinks(pscDocument.getData().getLinks());
            }
            return individual;
        } else {
            logger.error("Skipped transforming pscDoc to individual");
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,"PscDocument not found");
        }

    }

    /**
     * Transform Individual Beneficial Owner PSC.
     * @param optionalPscDocument PSC.
     * @return PSC mongo Document.
     */
    public IndividualBeneficialOwner transformPscDocToIndividualBeneficialOwner(
            Optional<PscDocument> optionalPscDocument) throws TransformerException {

        logger.info("Attempting to transform pscDocument to IndividualBeneficialOwner");

        if (optionalPscDocument.isPresent()) {
            PscDocument pscDocument = optionalPscDocument.get();
            IndividualBeneficialOwner individualBeneficialOwner = new IndividualBeneficialOwner();
            if (pscDocument.getData().getEtag() != null) {
                individualBeneficialOwner.setEtag(pscDocument.getData().getEtag());
            }
            if (pscDocument.getDeltaAt() != null) {
                individualBeneficialOwner.setNotifiedOn(LocalDate
                        .parse(pscDocument.getDeltaAt(),dateTimeFormatter));
            }
            individualBeneficialOwner
                    .setKind(IndividualBeneficialOwner.KindEnum.INDIVIDUAL_BENEFICIAL_OWNER);
            if (pscDocument.getData().getName() != null) {
                individualBeneficialOwner.setName(pscDocument.getData().getName());
            }
            if (pscDocument.getData().getNameElements() != null) {
                NameElements nameElements = new NameElements();
                if (pscDocument.getData().getNameElements().getTitle() != null) {
                    nameElements.setTitle(pscDocument.getData().getNameElements().getTitle());
                }
                if (pscDocument.getData().getNameElements().getForename() != null) {
                    nameElements.setForename(pscDocument.getData().getNameElements().getForename());
                }
                if (pscDocument.getData().getNameElements().getMiddleName() != null) {
                    nameElements.setMiddleName(pscDocument.getData()
                            .getNameElements().getMiddleName());
                }
                if (pscDocument.getData().getNameElements().getSurname() != null) {
                    nameElements.setSurname(pscDocument.getData().getNameElements().getSurname());
                }
                individualBeneficialOwner.setNameElements(nameElements);
            }
            if (pscDocument.getData().getAddress() != null) {
                Address address = new Address();
                if (pscDocument.getData().getAddress().getAddressLine1() != null) {
                    address.setAddressLine1(pscDocument.getData().getAddress().getAddressLine1());
                }
                if (pscDocument.getData().getAddress().getAddressLine2() != null) {
                    address.setAddressLine2(pscDocument.getData().getAddress().getAddressLine2());
                }
                if (pscDocument.getData().getAddress().getCountry() != null) {
                    address.setCountry(pscDocument.getData().getAddress().getCountry());
                }
                if (pscDocument.getData().getAddress().getLocality() != null) {
                    address.setLocality(pscDocument.getData().getAddress().getLocality());
                }
                if (pscDocument.getData().getAddress().getPostalCode() != null) {
                    address.setPostalCode(pscDocument.getData().getAddress().getPostalCode());
                }
                if (pscDocument.getData().getAddress().getPremises() != null) {
                    address.setPremises(pscDocument.getData().getAddress().getPremises());
                }
                if (pscDocument.getData().getAddress().getRegion() != null) {
                    address.setRegion(pscDocument.getData().getAddress().getRegion());
                }
                if (pscDocument.getData().getAddress().getCareOf() != null) {
                    address.setCareOf(pscDocument.getData().getAddress().getCareOf());
                }
                if (pscDocument.getData().getAddress().getPoBox() != null) {
                    address.setPoBox(pscDocument.getData().getAddress().getPoBox());
                }
                individualBeneficialOwner.setAddress(address);
            }
            if (pscDocument.getData().getNaturesOfControl() != null) {
                individualBeneficialOwner
                        .setNaturesOfControl(pscDocument.getData().getNaturesOfControl());
            }
            if (pscDocument.getData().getLinks() != null) {
                individualBeneficialOwner.setLinks(pscDocument.getData().getLinks());
            }
            if (pscDocument.getData().getNationality() != null) {
                individualBeneficialOwner.setNationality(pscDocument.getData().getNationality());
            }
            if (pscDocument.getData().getSanctioned() != null) {
                individualBeneficialOwner.setIsSanctioned(pscDocument.getData().getSanctioned());
            }
            return individualBeneficialOwner;
        } else {
            logger.error("Skipped transforming pscDoc to individualBeneficialOwner");
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
        if (requestBody.getExternalData().getCompanyNumber() == null) {
            pscDocument.setCompanyNumber(
                    requestBody.getExternalData().getData().getCompanyNumber());
        } else {
            pscDocument.setCompanyNumber(requestBody.getExternalData().getCompanyNumber());
        }
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

    /**
     * Transform Corporate Entity Beneficial Owner.
     * @param optionalPscDocument PSC.
     * @return PSC mongo Document.
     */
    public CorporateEntityBeneficialOwner transformPscDocToCorporateEntityBeneficialOwner(
            Optional<PscDocument> optionalPscDocument) {
        logger.info("Attempting to transform pscDocument to CorporateEntityBeneficialOwner");

        if (optionalPscDocument.isPresent()) {
            PscDocument pscDocument = optionalPscDocument.get();
            CorporateEntityBeneficialOwner corporateEntityBeneficialOwner =
                    new CorporateEntityBeneficialOwner();
            if (pscDocument.getData().getEtag() != null) {
                corporateEntityBeneficialOwner.setEtag(pscDocument.getData().getEtag());
            }
            if (pscDocument.getDeltaAt() != null) {
                corporateEntityBeneficialOwner.setNotifiedOn(LocalDate
                        .parse(pscDocument.getDeltaAt(),dateTimeFormatter));
            }
            corporateEntityBeneficialOwner
                    .setKind(CorporateEntityBeneficialOwner
                            .KindEnum.CORPORATE_ENTITY_BENEFICIAL_OWNER);
            if (pscDocument.getData().getName() != null) {
                corporateEntityBeneficialOwner.setName(pscDocument.getData().getName());
            }

            Address address = new Address();
            if (pscDocument.getData().getAddress() != null) {

                if (pscDocument.getData().getAddress().getAddressLine1() != null) {
                    address.setAddressLine1(pscDocument.getData().getAddress().getAddressLine1());
                }
                if (pscDocument.getData().getAddress().getAddressLine2() != null) {
                    address.setAddressLine2(pscDocument.getData().getAddress().getAddressLine2());
                }
                if (pscDocument.getData().getAddress().getCountry() != null) {
                    address.setCountry(pscDocument.getData().getAddress().getCountry());
                }
                if (pscDocument.getData().getAddress().getLocality() != null) {
                    address.setLocality(pscDocument.getData().getAddress().getLocality());
                }
                if (pscDocument.getData().getAddress().getPostalCode() != null) {
                    address.setPostalCode(pscDocument.getData().getAddress().getPostalCode());
                }
                if (pscDocument.getData().getAddress().getPremises() != null) {
                    address.setPremises(pscDocument.getData().getAddress().getPremises());
                }
                if (pscDocument.getData().getAddress().getRegion() != null) {
                    address.setRegion(pscDocument.getData().getAddress().getRegion());
                }
                if (pscDocument.getData().getAddress().getCareOf() != null) {
                    address.setCareOf(pscDocument.getData().getAddress().getCareOf());
                }
                if (pscDocument.getData().getAddress().getPoBox() != null) {
                    address.setPoBox(pscDocument.getData().getAddress().getPoBox());
                }
                corporateEntityBeneficialOwner.setAddress(address);
            }
            if (pscDocument.getData().getNaturesOfControl() != null) {
                corporateEntityBeneficialOwner
                        .setNaturesOfControl(pscDocument.getData().getNaturesOfControl());
            }
            if (pscDocument.getData().getLinks() != null) {
                corporateEntityBeneficialOwner.setLinks(pscDocument.getData().getLinks());
            }
            if (pscDocument.getData().getSanctioned() != null) {
                corporateEntityBeneficialOwner
                        .setIsSanctioned(pscDocument.getData().getSanctioned());
            }
            if (pscDocument.getIdentification() != null) {
                Identification identification = new Identification();
                if (pscDocument.getIdentification().getCountryRegistered() != null) {
                    identification.setCountryRegistered(
                            pscDocument.getIdentification().getCountryRegistered());
                }
                if (pscDocument.getIdentification().getLegalAuthority() != null) {
                    identification.setLegalAuthority(
                            pscDocument.getIdentification().getLegalAuthority());
                }
                if (pscDocument.getIdentification().getLegalForm() != null) {
                    identification.setLegalForm(
                            pscDocument.getIdentification().getLegalForm());
                }
                if (pscDocument.getIdentification().getPlaceRegistered() != null) {
                    identification.setPlaceRegistered(
                            pscDocument.getIdentification().getPlaceRegistered());
                }
                if (pscDocument.getIdentification().getRegistrationNumber() != null) {
                    identification.setRegistrationNumber(
                            pscDocument.getIdentification().getRegistrationNumber());
                }
                pscDocument.setIdentification(identification);
            }
            return corporateEntityBeneficialOwner;
        } else {
            logger.error("Skipped transforming pscDoc to individualBeneficialOwner");
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,"PscDocument not found");
        }


    }
}
