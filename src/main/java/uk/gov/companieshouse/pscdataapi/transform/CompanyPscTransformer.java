package uk.gov.companieshouse.pscdataapi.transform;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc.CorporateEntity;
import uk.gov.companieshouse.api.psc.CorporateEntityBeneficialOwner;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.Identification;
import uk.gov.companieshouse.api.psc.Individual;
import uk.gov.companieshouse.api.psc.IndividualBeneficialOwner;
import uk.gov.companieshouse.api.psc.LegalPerson;
import uk.gov.companieshouse.api.psc.LegalPersonBeneficialOwner;
import uk.gov.companieshouse.api.psc.ListSummary;
import uk.gov.companieshouse.api.psc.SensitiveData;
import uk.gov.companieshouse.api.psc.SuperSecure;
import uk.gov.companieshouse.api.psc.SuperSecureBeneficialOwner;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscdataapi.data.IndividualPscRoles;
import uk.gov.companieshouse.pscdataapi.data.SecurePscRoles;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscdataapi.models.Address;
import uk.gov.companieshouse.pscdataapi.models.DateOfBirth;
import uk.gov.companieshouse.pscdataapi.models.NameElements;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.models.PscIdentification;
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
     * @param pscDocument PSC.
     * @return PSC mongo Document.
     */
    public Individual transformPscDocToIndividual(
            PscDocument pscDocument, Boolean registerView) {
        logger.info("Attempting to transform pscDocument to Individual",
                DataMapHolder.getLogMap());
        Individual individual = new Individual();
        individual.setEtag(pscDocument.getData().getEtag());
        individual.setNotifiedOn(LocalDate.parse(pscDocument.getDeltaAt(),dateTimeFormatter));
        individual.setKind(Individual.KindEnum.INDIVIDUAL_PERSON_WITH_SIGNIFICANT_CONTROL);
        individual.setCountryOfResidence(pscDocument.getData().getCountryOfResidence());
        individual.setName(pscDocument.getData().getName());
        individual.setNameElements(mapNameElements(pscDocument.getData().getNameElements()));
        individual.setAddress(mapAddress(pscDocument.getData().getAddress()));
        individual.setNaturesOfControl(pscDocument.getData().getNaturesOfControl());
        individual.setLinks(pscDocument.getData().getLinks());
        individual.setDateOfBirth(mapDateOfBirth(
                pscDocument.getSensitiveData().getDateOfBirth(), registerView));
        return individual;
    }

    /**
     * Transform Individual Beneficial Owner PSC.
     * @param pscDocument PSC.
     * @return PSC mongo Document.
     */
    public IndividualBeneficialOwner transformPscDocToIndividualBeneficialOwner(
            PscDocument pscDocument, Boolean registerView) {
        logger.info("Attempting to transform pscDocument to IndividualBeneficialOwner",
                DataMapHolder.getLogMap());
        IndividualBeneficialOwner individualBo = new IndividualBeneficialOwner();
        individualBo.setEtag(pscDocument.getData().getEtag());
        individualBo.setNotifiedOn(LocalDate.parse(pscDocument.getDeltaAt(),dateTimeFormatter));
        individualBo.setKind(IndividualBeneficialOwner.KindEnum.INDIVIDUAL_BENEFICIAL_OWNER);
        individualBo.setName(pscDocument.getData().getName());
        individualBo.setNameElements(mapNameElements(pscDocument.getData().getNameElements()));
        individualBo.setAddress(mapBoAddress(pscDocument.getData().getAddress()));
        individualBo.setNaturesOfControl(pscDocument.getData().getNaturesOfControl());
        individualBo.setLinks(pscDocument.getData().getLinks());
        individualBo.setNationality(pscDocument.getData().getNationality());
        individualBo.setIsSanctioned(pscDocument.getData().getSanctioned());
        individualBo.setDateOfBirth(mapDateOfBirth(
                pscDocument.getSensitiveData().getDateOfBirth(), registerView));
        return individualBo;
    }

    /**
     * Transform Corporate Entity PSC.
     * @param pscDocument PSC.
     * @return PSC mongo Document.
     */
    public CorporateEntity transformPscDocToCorporateEntity(PscDocument pscDocument) {
        logger.info("Attempting to transform pscDocument to CorporateEntity",
                DataMapHolder.getLogMap());
        CorporateEntity corporateEntity = new CorporateEntity();
        corporateEntity.setEtag(pscDocument.getData().getEtag());
        corporateEntity.setNotifiedOn((pscDocument.getDeltaAt() != null)
                ? LocalDate.parse(pscDocument.getDeltaAt(),dateTimeFormatter) : null);
        corporateEntity.setCeasedOn(pscDocument.getData().getCeasedOn());
        corporateEntity.setKind(CorporateEntity
                .KindEnum.CORPORATE_ENTITY_PERSON_WITH_SIGNIFICANT_CONTROL);
        corporateEntity.setName(pscDocument.getData().getName());
        corporateEntity.setLinks(pscDocument.getData().getLinks());
        corporateEntity.setAddress(mapAddress(pscDocument.getData().getAddress()));
        corporateEntity.setIdentification(
                mapIdentification(pscDocument.getIdentification(), "corporate"));
        corporateEntity.setNaturesOfControl(pscDocument.getData().getNaturesOfControl());
        return corporateEntity;
    }

    /**
     * Transform Corporate Entity Beneficial Owner PSC.
     * @param pscDocument PSC.
     * @return PSC mongo Document.
     */
    public CorporateEntityBeneficialOwner transformPscDocToCorporateEntityBeneficialOwner(
            PscDocument pscDocument) {
        logger.info("Attempting to transform pscDocument to CorporateEntityBeneficialOwner",
                DataMapHolder.getLogMap());
        CorporateEntityBeneficialOwner corporateEntityBo = new CorporateEntityBeneficialOwner();
        corporateEntityBo.setEtag(pscDocument.getData().getEtag());
        corporateEntityBo.setNotifiedOn(LocalDate.parse(pscDocument.getDeltaAt(),
                dateTimeFormatter));
        corporateEntityBo.setKind(CorporateEntityBeneficialOwner
                        .KindEnum.CORPORATE_ENTITY_BENEFICIAL_OWNER);
        corporateEntityBo.setName(pscDocument.getData().getName());
        corporateEntityBo.setAddress(mapBoAddress(pscDocument.getData().getAddress()));
        corporateEntityBo.setNaturesOfControl(pscDocument.getData().getNaturesOfControl());
        corporateEntityBo.setLinks(pscDocument.getData().getLinks());
        corporateEntityBo.setIsSanctioned(pscDocument.getData().getSanctioned());
        corporateEntityBo.setIdentification(mapIdentification(
                pscDocument.getIdentification(), "corporate"));
        return corporateEntityBo;
    }

    /**
     * Transform Legal person PSC.
     * @param pscDocument PSC.
     * @return PSC mongo Document.
     */
    public LegalPerson transformPscDocToLegalPerson(PscDocument pscDocument) {
        logger.info("Attempting to transform pscDocument to LegalPerson",
                DataMapHolder.getLogMap());
        LegalPerson legalPerson = new LegalPerson();
        legalPerson.setEtag(pscDocument.getData().getEtag());
        legalPerson.setNotifiedOn(LocalDate.parse(pscDocument.getDeltaAt(),dateTimeFormatter));
        legalPerson.setKind(LegalPerson
                        .KindEnum.LEGAL_PERSON_PERSON_WITH_SIGNIFICANT_CONTROL);
        legalPerson.setName(pscDocument.getData().getName());
        legalPerson.setAddress(mapAddress(pscDocument.getData().getAddress()));
        legalPerson.setNaturesOfControl(pscDocument.getData().getNaturesOfControl());
        legalPerson.setLinks(pscDocument.getData().getLinks());
        legalPerson.setIdentification(mapIdentification(
                pscDocument.getIdentification(), "legal"));
        legalPerson.setCeasedOn(pscDocument.getData().getCeasedOn());
        return legalPerson;
    }

    /**
     * Transform Legal person Beneficial Owner PSC.
     * @param pscDocument PSC.
     * @return PSC mongo Document.
     */
    public LegalPersonBeneficialOwner transformPscDocToLegalPersonBeneficialOwner(
            PscDocument pscDocument) {
        logger.info("Attempting to transform pscDocument to LegalPersonBeneficialOwner",
                DataMapHolder.getLogMap());
        LegalPersonBeneficialOwner legalPersonBo = new LegalPersonBeneficialOwner();
        legalPersonBo.setEtag(pscDocument.getData().getEtag());
        legalPersonBo.setNotifiedOn(LocalDate.parse(pscDocument.getDeltaAt(),dateTimeFormatter));
        legalPersonBo.setKind(LegalPersonBeneficialOwner
                        .KindEnum.LEGAL_PERSON_BENEFICIAL_OWNER);
        legalPersonBo.setName(pscDocument.getData().getName());
        legalPersonBo.setAddress(mapBoAddress(pscDocument.getData().getAddress()));
        legalPersonBo.setNaturesOfControl(pscDocument.getData().getNaturesOfControl());
        legalPersonBo.setLinks(pscDocument.getData().getLinks());
        legalPersonBo.setIdentification(mapIdentification(
                pscDocument.getIdentification(), "legal"));
        legalPersonBo.setCeasedOn(pscDocument.getData().getCeasedOn());
        legalPersonBo.setIsSanctioned(pscDocument.getData().getSanctioned());
        return legalPersonBo;
    }

    /**
     * Transform Super Secure PSC.
     * @param pscDocument PSC.
     * @return PSC mongo Document.
     */
    public SuperSecure transformPscDocToSuperSecure(PscDocument pscDocument) {
        logger.info("Attempting to transform pscDocument to SuperSecure",
                DataMapHolder.getLogMap());
        SuperSecure superSecure = new SuperSecure();
        superSecure.setEtag(pscDocument.getData().getEtag());
        superSecure
                .setKind(SuperSecure.KindEnum.SUPER_SECURE_PERSON_WITH_SIGNIFICANT_CONTROL);
        superSecure.setDescription(SuperSecure
                .DescriptionEnum.SUPER_SECURE_PERSONS_WITH_SIGNIFICANT_CONTROL);
        superSecure.setCeased(pscDocument.getData().getCeased());
        superSecure.setLinks(pscDocument.getData().getLinks());
        return superSecure;
    }

    /**
     * Transform Super Secure Beneficial Owner PSC.
     * @param pscDocument PSC.
     * @return PSC mongo Document.
     */
    public SuperSecureBeneficialOwner transformPscDocToSuperSecureBeneficialOwner(
            PscDocument pscDocument) {
        logger.info("Attempting to transform pscDocument to SuperSecureBeneficialOwner",
                DataMapHolder.getLogMap());
        SuperSecureBeneficialOwner superSecureBo = new SuperSecureBeneficialOwner();
        superSecureBo.setEtag(pscDocument.getData().getEtag());
        superSecureBo
                .setKind(SuperSecureBeneficialOwner.KindEnum.SUPER_SECURE_BENEFICIAL_OWNER);
        superSecureBo.setDescription(SuperSecureBeneficialOwner
                .DescriptionEnum.SUPER_SECURE_BENEFICIAL_OWNER);
        superSecureBo.setCeased(pscDocument.getData().getCeased());
        superSecureBo.setLinks(pscDocument.getData().getLinks());
        return superSecureBo;
    }

    /**
     * Transform PSC on insert.
     * @param notificationId PSC Id.
     * @param requestBody request payload.
     * @return PSC mongo Document.
     */
    public PscDocument transformPsc(String notificationId, FullRecordCompanyPSCApi requestBody) {
        PscDocument pscDocument = new PscDocument();
        logger.info("Transforming incoming payload", DataMapHolder.getLogMap());

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
        pscDocument.setIdentification(new PscIdentification(
                requestBody.getExternalData().getData().getIdentification()));

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

    /**
     * Transform Legal person Beneficial Owner.
     * @param pscDocument PSC.
     * @return ListSummary mongo Document.
     */
    public ListSummary transformPscDocToListSummary(PscDocument  pscDocument) {
        ListSummary listSummary = new ListSummary();
        PscData pscData = pscDocument.getData();

        listSummary.setEtag(pscData.getEtag());
        listSummary.setKind(listSummary.getKind());
        listSummary.setName(pscData.getName());
        listSummary.setNameElements(mapNameElements(pscData.getNameElements()));
        listSummary.setAddress(mapAddress(pscData.getAddress()));
        listSummary.setPrincipalOfficeAddress(mapAddress(pscData.getAddress()));
        listSummary.setNaturesOfControl(pscData.getNaturesOfControl());
        listSummary.setLinks(pscData.getLinks());
        listSummary.setCeasedOn(pscData.getCeasedOn());
        listSummary.setIsSanctioned(pscData.getSanctioned());
        listSummary.setNationality(pscData.getNationality());
        listSummary.setCountryOfResidence(pscData.getCountryOfResidence());
        if (pscDocument.getData().getDescription() != null) {
            listSummary.setDescription(
                    ListSummary.DescriptionEnum.SUPER_SECURE_PERSONS_WITH_SIGNIFICANT_CONTROL);
        }
        listSummary.setNaturesOfControl(pscData.getNaturesOfControl());
        listSummary.setIdentification(mapIdentification(
                pscDocument.getIdentification(), "list summary"));
        return listSummary;
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

    private uk.gov.companieshouse.api.psc.DateOfBirth mapDateOfBirth(
            DateOfBirth inputDateOfBirth, Boolean registerView) {
        if (inputDateOfBirth != null) {
            uk.gov.companieshouse.api.psc.DateOfBirth dateOfBirth =
                    new uk.gov.companieshouse.api.psc.DateOfBirth();
            if (registerView) {
                dateOfBirth.setDay(inputDateOfBirth.getDay());
            } else {
                dateOfBirth.setDay(null);
            }
            dateOfBirth.setMonth(inputDateOfBirth.getMonth());
            dateOfBirth.setYear(inputDateOfBirth.getYear());
            return dateOfBirth;
        } else {
            return null;
        }
    }

    private uk.gov.companieshouse.api.psc.Address mapAddress(
            uk.gov.companieshouse.pscdataapi.models.Address inputAddress) {
        if (inputAddress != null) {
            uk.gov.companieshouse.api.psc.Address address =
                    new uk.gov.companieshouse.api.psc.Address();
            address.setAddressLine1(inputAddress.getAddressLine1());
            address.setAddressLine2(inputAddress.getAddressLine2());
            address.setCountry(inputAddress.getCountry());
            address.setLocality(inputAddress.getLocality());
            address.setPoBox(inputAddress.getPoBox());
            address.setPostalCode(inputAddress.getPostalCode());
            address.setPremises(inputAddress.getPremises());
            address.setRegion(inputAddress.getRegion());
            return address;
        } else {
            return null;
        }
    }

    private uk.gov.companieshouse.api.psc.BeneficialOwnerAddress mapBoAddress(
            uk.gov.companieshouse.pscdataapi.models.Address inputAddress) {
        if (inputAddress != null) {
            uk.gov.companieshouse.api.psc.BeneficialOwnerAddress address =
                    new uk.gov.companieshouse.api.psc.BeneficialOwnerAddress();
            address.setAddressLine1(inputAddress.getAddressLine1());
            address.setAddressLine2(inputAddress.getAddressLine2());
            address.setCountry(inputAddress.getCountry());
            address.setLocality(inputAddress.getLocality());
            address.setPoBox(inputAddress.getPoBox());
            address.setPostalCode(inputAddress.getPostalCode());
            address.setPremises(inputAddress.getPremises());
            address.setRegion(inputAddress.getRegion());
            return address;
        } else {
            return null;
        }
    }

    private static uk.gov.companieshouse.api.psc.NameElements mapNameElements(
            NameElements inputNameElements) {
        if (inputNameElements != null) {
            uk.gov.companieshouse.api.psc.NameElements nameElements =
                    new uk.gov.companieshouse.api.psc.NameElements();
            nameElements.setTitle(inputNameElements.getTitle());
            nameElements.setForename(inputNameElements.getForename());
            nameElements.setMiddleName(inputNameElements.getMiddleName());
            nameElements.setSurname(inputNameElements.getSurname());
            return nameElements;
        } else {
            return null;
        }
    }

    private static Identification mapIdentification(
            PscIdentification inputIdentification, String kindString) {
        if (inputIdentification != null) {
            Identification identification = new Identification();
            identification.setLegalAuthority(inputIdentification.getLegalAuthority());
            identification.setLegalForm(inputIdentification.getLegalForm());
            if (kindString.equals("corporate") || kindString.equals("list summary")) {
                identification.setCountryRegistered(inputIdentification.getCountryRegistered());
                identification.setPlaceRegistered(inputIdentification.getPlaceRegistered());
                identification.setRegistrationNumber(inputIdentification.getRegistrationNumber());
            } // else "legal"
            return identification;
        } else {
            return null;
        }
    }

}
