package uk.gov.companieshouse.pscdataapi.transform;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc.*;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscdataapi.data.IndividualPscRoles;
import uk.gov.companieshouse.pscdataapi.data.SecurePscRoles;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscdataapi.models.Address;
import uk.gov.companieshouse.pscdataapi.models.DateOfBirth;
import uk.gov.companieshouse.pscdataapi.models.NameElements;
import uk.gov.companieshouse.pscdataapi.models.*;
import uk.gov.companieshouse.pscdataapi.util.PscTransformationHelper;

@Component
public class CompanyPscTransformer {

    private static final String CORPORATE = "corporate";
    private static final String LEGAL = "legal";

    private final Logger logger;
    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS");

    public CompanyPscTransformer(Logger logger) {
        this.logger = logger;
    }

    /**
     * Transform Individual PSC.
     *
     * @param pscDocument PSC.
     * @return PSC mongo Document.
     */
    public Individual transformPscDocToIndividual(
            PscDocument pscDocument, boolean showFullDateOfBirth) {
        logger.info("Attempting to transform pscDocument to Individual",
                DataMapHolder.getLogMap());
        Individual individual = new Individual();
        individual.setKind(Individual.KindEnum.INDIVIDUAL_PERSON_WITH_SIGNIFICANT_CONTROL);
        if (pscDocument.getData() != null) {
            PscData pscData = pscDocument.getData();
            individual.setEtag(pscData.getEtag());
            individual.setCountryOfResidence(pscData.getCountryOfResidence());
            individual.setName(pscData.getName());
            individual.setNameElements(mapNameElements(pscData.getNameElements()));
            individual.setAddress(mapAddress(pscData.getAddress()));
            individual.setNaturesOfControl(pscData.getNaturesOfControl());
            individual.setNationality(pscData.getNationality());
            individual.setLinks(pscData.getLinks());
            individual.setNotifiedOn(pscData.getNotifiedOn());
            individual.setCeasedOn(pscData.getCeasedOn());
        }
        if (pscDocument.getSensitiveData() != null) {
            individual.setDateOfBirth(mapDateOfBirth(
                    pscDocument.getSensitiveData().getDateOfBirth(), showFullDateOfBirth));
        }
        return individual;
    }

    /**
     * Transform Individual PSC full record.
     *
     * @param pscDocument PSC.
     * @return PSC mongo Document.
     */
    public FullRecordCompanyPSCApi transformPscDocToFullRecord(PscDocument pscDocument) {
        logger.info("Attempting to transform pscDocument to Full Record PSC",
                DataMapHolder.getLogMap());
        FullRecordCompanyPSCApi fullRecordCompanyPSCApi = new FullRecordCompanyPSCApi();

        PscData pscData = pscDocument.getData();
        Data data = new Data();
        data.setName(pscData.getName());
        data.setNameElements(mapNameElements(pscData.getNameElements()));
        data.setCountryOfResidence(pscData.getCountryOfResidence());
        data.setNotifiedOn(pscData.getNotifiedOn());
        data.setCeasedOn(pscData.getCeasedOn());
        data.setNaturesOfControl(pscData.getNaturesOfControl());
        data.setNationality(pscData.getNationality());
//        data.setLinks((List<ItemLinkTypes>) pscData.getLinks());
        data.setServiceAddress(mapAddress(pscData.getAddress()));
        data.setEtag(pscData.getEtag());
        ExternalData externalData = new ExternalData();
        externalData.setData(data);

        PscSensitiveData sensitivePscData = pscDocument.getSensitiveData();
        SensitiveData sensitiveApiData = new SensitiveData();
        sensitiveApiData.setResidentialAddressSameAsServiceAddress(sensitivePscData.getResidentialAddressIsSameAsServiceAddress());
        sensitiveApiData.setDateOfBirth(mapDateOfBirth(sensitivePscData.getDateOfBirth(), true));
        sensitiveApiData.setUsualResidentialAddress(mapToApiUra(sensitivePscData.getUsualResidentialAddress()));
        externalData.setSensitiveData(sensitiveApiData);

        fullRecordCompanyPSCApi.setExternalData(externalData);

        return fullRecordCompanyPSCApi;
    }

    /**
     * Transform Individual Beneficial Owner PSC.
     *
     * @param pscDocument PSC.
     * @return PSC mongo Document.
     */
    public IndividualBeneficialOwner transformPscDocToIndividualBeneficialOwner(
            PscDocument pscDocument, boolean showFullDateOfBirth) {
        logger.info("Attempting to transform pscDocument to IndividualBeneficialOwner",
                DataMapHolder.getLogMap());
        IndividualBeneficialOwner individualBo = new IndividualBeneficialOwner();
        individualBo.setKind(IndividualBeneficialOwner.KindEnum.INDIVIDUAL_BENEFICIAL_OWNER);
        if (pscDocument.getData() != null) {
            PscData pscData = pscDocument.getData();
            individualBo.setEtag(pscData.getEtag());
            individualBo.setName(pscData.getName());
            individualBo.setNameElements(mapNameElements(pscData.getNameElements()));
            individualBo.setAddress(mapBoAddress(pscData.getAddress()));
            individualBo.setNaturesOfControl(pscData.getNaturesOfControl());
            individualBo.setLinks(pscData.getLinks());
            individualBo.setNationality(pscData.getNationality());
            individualBo.setIsSanctioned(pscData.getSanctioned());
            individualBo.setNotifiedOn(pscData.getNotifiedOn());
            individualBo.setCeasedOn(pscData.getCeasedOn());
        }
        if (pscDocument.getSensitiveData() != null) {
            individualBo.setDateOfBirth(mapDateOfBirth(
                    pscDocument.getSensitiveData().getDateOfBirth(), showFullDateOfBirth));
        }
        return individualBo;
    }

    /**
     * Transform Corporate Entity PSC.
     *
     * @param pscDocument PSC.
     * @return PSC mongo Document.
     */
    public CorporateEntity transformPscDocToCorporateEntity(PscDocument pscDocument) {
        logger.info("Attempting to transform pscDocument to CorporateEntity",
                DataMapHolder.getLogMap());
        CorporateEntity corporateEntity = new CorporateEntity();
        corporateEntity.setKind(CorporateEntity
                .KindEnum.CORPORATE_ENTITY_PERSON_WITH_SIGNIFICANT_CONTROL);
        if (pscDocument.getData() != null) {
            PscData pscData = pscDocument.getData();
            corporateEntity.setEtag(pscData.getEtag());
            corporateEntity.setCeasedOn(pscData.getCeasedOn());
            corporateEntity.setNotifiedOn(pscData.getNotifiedOn());
            corporateEntity.setName(pscData.getName());
            corporateEntity.setLinks(pscData.getLinks());
            corporateEntity.setAddress(mapAddress(pscData.getAddress()));
            corporateEntity.setNaturesOfControl(pscData.getNaturesOfControl());
            corporateEntity.setIdentification(
                    mapIdentification(pscData.getIdentification(), CORPORATE));
        }
        return corporateEntity;
    }

    /**
     * Transform Corporate Entity Beneficial Owner PSC.
     *
     * @param pscDocument PSC.
     * @return PSC mongo Document.
     */
    public CorporateEntityBeneficialOwner transformPscDocToCorporateEntityBeneficialOwner(
            PscDocument pscDocument) {
        logger.info("Attempting to transform pscDocument to CorporateEntityBeneficialOwner",
                DataMapHolder.getLogMap());
        CorporateEntityBeneficialOwner corporateEntityBo = new CorporateEntityBeneficialOwner();
        corporateEntityBo.setKind(CorporateEntityBeneficialOwner
                .KindEnum.CORPORATE_ENTITY_BENEFICIAL_OWNER);
        if (pscDocument.getData() != null) {
            PscData pscData = pscDocument.getData();
            corporateEntityBo.setEtag(pscData.getEtag());
            corporateEntityBo.setName(pscData.getName());
            corporateEntityBo.setAddress(mapBoAddress(pscData.getAddress()));
            corporateEntityBo.setNaturesOfControl(pscData.getNaturesOfControl());
            corporateEntityBo.setLinks(pscData.getLinks());
            corporateEntityBo.setIsSanctioned(pscData.getSanctioned());
            corporateEntityBo.setIdentification(mapIdentification(
                    pscData.getIdentification(), CORPORATE));
            corporateEntityBo.setNotifiedOn(pscData.getNotifiedOn());
            corporateEntityBo.setPrincipalOfficeAddress(mapPrincipleAddress(
                    pscData.getPrincipalOfficeAddress()));
            corporateEntityBo.setCeasedOn(pscData.getCeasedOn());
        }
        return corporateEntityBo;
    }

    /**
     * Transform Legal person PSC.
     *
     * @param pscDocument PSC.
     * @return PSC mongo Document.
     */
    public LegalPerson transformPscDocToLegalPerson(PscDocument pscDocument) {
        logger.info("Attempting to transform pscDocument to LegalPerson",
                DataMapHolder.getLogMap());
        LegalPerson legalPerson = new LegalPerson();
        legalPerson.setKind(LegalPerson
                .KindEnum.LEGAL_PERSON_PERSON_WITH_SIGNIFICANT_CONTROL);
        if (pscDocument.getData() != null) {
            PscData pscData = pscDocument.getData();
            legalPerson.setEtag(pscData.getEtag());
            legalPerson.setName(pscData.getName());
            legalPerson.setAddress(mapAddress(pscData.getAddress()));
            legalPerson.setNaturesOfControl(pscData.getNaturesOfControl());
            legalPerson.setLinks(pscData.getLinks());
            legalPerson.setCeasedOn(pscData.getCeasedOn());
            legalPerson.setNotifiedOn(pscData.getNotifiedOn());
            legalPerson.setIdentification(mapIdentification(
                    pscData.getIdentification(), LEGAL));
        }
        return legalPerson;
    }

    /**
     * Transform Legal person Beneficial Owner PSC.
     *
     * @param pscDocument PSC.
     * @return PSC mongo Document.
     */
    public LegalPersonBeneficialOwner transformPscDocToLegalPersonBeneficialOwner(
            PscDocument pscDocument) {
        logger.info("Attempting to transform pscDocument to LegalPersonBeneficialOwner",
                DataMapHolder.getLogMap());
        LegalPersonBeneficialOwner legalPersonBo = new LegalPersonBeneficialOwner();
        legalPersonBo.setKind(LegalPersonBeneficialOwner
                .KindEnum.LEGAL_PERSON_BENEFICIAL_OWNER);
        if (pscDocument.getData() != null) {
            PscData pscData = pscDocument.getData();
            legalPersonBo.setEtag(pscData.getEtag());
            legalPersonBo.setName(pscData.getName());
            legalPersonBo.setAddress(mapBoAddress(pscData.getAddress()));
            legalPersonBo.setNaturesOfControl(pscData.getNaturesOfControl());
            legalPersonBo.setLinks(pscData.getLinks());
            legalPersonBo.setCeasedOn(pscData.getCeasedOn());
            legalPersonBo.setNotifiedOn(pscData.getNotifiedOn());
            legalPersonBo.setIsSanctioned(pscData.getSanctioned());
            legalPersonBo.setIdentification(mapIdentification(
                    pscData.getIdentification(), LEGAL));
            legalPersonBo.setPrincipalOfficeAddress(mapPrincipleAddress(
                    pscData.getPrincipalOfficeAddress()));
        }
        return legalPersonBo;
    }

    /**
     * Transform Super Secure PSC.
     *
     * @param pscDocument PSC.
     * @return PSC mongo Document.
     */
    public SuperSecure transformPscDocToSuperSecure(PscDocument pscDocument) {
        logger.info("Attempting to transform pscDocument to SuperSecure",
                DataMapHolder.getLogMap());
        SuperSecure superSecure = new SuperSecure();
        superSecure.setKind(SuperSecure.KindEnum.SUPER_SECURE_PERSON_WITH_SIGNIFICANT_CONTROL);
        superSecure.setDescription(
                SuperSecure.DescriptionEnum.SUPER_SECURE_PERSONS_WITH_SIGNIFICANT_CONTROL);
        if (pscDocument.getData() != null) {
            PscData pscData = pscDocument.getData();
            superSecure.setEtag(pscData.getEtag());
            if (Boolean.TRUE.equals(pscData.getCeased())) {
                superSecure.setCeased("1");
            } else {
                superSecure.setCeased("0");
            }
            superSecure.setLinks(pscData.getLinks());
        }
        return superSecure;
    }

    /**
     * Transform Super Secure Beneficial Owner PSC.
     *
     * @param pscDocument PSC.
     * @return PSC mongo Document.
     */
    public SuperSecureBeneficialOwner transformPscDocToSuperSecureBeneficialOwner(
            PscDocument pscDocument) {
        logger.info("Attempting to transform pscDocument to SuperSecureBeneficialOwner",
                DataMapHolder.getLogMap());
        SuperSecureBeneficialOwner superSecureBo = new SuperSecureBeneficialOwner();
        superSecureBo.setKind(SuperSecureBeneficialOwner.KindEnum.SUPER_SECURE_BENEFICIAL_OWNER);
        superSecureBo.setDescription(
                SuperSecureBeneficialOwner.DescriptionEnum.SUPER_SECURE_BENEFICIAL_OWNER);
        if (pscDocument.getData() != null) {
            PscData pscData = pscDocument.getData();
            superSecureBo.setEtag(pscData.getEtag());
            if (Boolean.TRUE.equals(pscData.getCeased())) {
                superSecureBo.setCeased("1");
            } else {
                superSecureBo.setCeased("0");
            }
            superSecureBo.setLinks(pscData.getLinks());
        }
        return superSecureBo;
    }

    /**
     * Transform Psc List.
     *
     * @param pscDocument PSC.
     * @return ListSummary mongo Document.
     */
    public ListSummary transformPscDocToListSummary(PscDocument pscDocument, Boolean registerView) {
        ListSummary listSummary = new ListSummary();


        if (pscDocument.getData() != null) {
            PscData pscData = pscDocument.getData();

            try {
                listSummary.setKind(ListSummary.KindEnum.fromValue(pscData.getKind()));
            } catch (Exception ex) {
                listSummary.setKind(null);
            }

            try {
                listSummary.setDescription(ListSummary.DescriptionEnum.fromValue(
                        pscData.getDescription()));
            } catch (Exception ex) {
                listSummary.setDescription(null);
            }

            listSummary.setEtag(pscData.getEtag());
            listSummary.setName(pscData.getName());
            listSummary.setNameElements(mapNameElements(pscData.getNameElements()));
            listSummary.setAddress(mapAddress(pscData.getAddress()));
            listSummary.setPrincipalOfficeAddress(mapAddress(pscData.getPrincipalOfficeAddress()));
            listSummary.setNaturesOfControl(pscData.getNaturesOfControl());
            listSummary.setLinks(pscData.getLinks());
            listSummary.setCeasedOn(pscData.getCeasedOn());
            listSummary.setNotifiedOn(pscData.getNotifiedOn());
            listSummary.setIsSanctioned(pscData.getSanctioned());
            listSummary.setNationality(pscData.getNationality());
            listSummary.setCountryOfResidence(pscData.getCountryOfResidence());
            listSummary.setNaturesOfControl(pscData.getNaturesOfControl());
            listSummary.setCeased(pscData.getCeasedOn() != null);
            listSummary.setIdentification(mapIdentification(
                    pscData.getIdentification(), "list summary"));
        }
        if (pscDocument.getSensitiveData() != null) {
            listSummary.setDateOfBirth(mapDateOfBirth(pscDocument.getSensitiveData()
                    .getDateOfBirth(), registerView));
        }
        return listSummary;
    }

    /**
     * Transform PSC on insert.
     *
     * @param notificationId PSC Id.
     * @param requestBody    request payload.
     * @return PSC mongo Document.
     */
    public PscDocument transformPscOnInsert(
            String notificationId, FullRecordCompanyPSCApi requestBody) {
        String pscStatementId = null;
        if (requestBody.getExternalData() != null) {
            ExternalData externalData = requestBody.getExternalData();
            if (externalData != null) {
                pscStatementId = requestBody.getExternalData().getPscStatementId();
            }
        }
        PscDocument pscDocument = new PscDocument();
        logger.info("Transforming incoming payload", DataMapHolder.getLogMap());

        pscDocument.setId(notificationId);
        pscDocument.setNotificationId(notificationId);
        if (requestBody.getExternalData() != null) {
            ExternalData externalData = requestBody.getExternalData();
            pscDocument.setPscId(externalData.getPscId());
            pscDocument.setCompanyNumber(externalData.getCompanyNumber());

            if (externalData.getData() != null) {
                Data data = externalData.getData();
                PscData pscData;
                pscData = transformDataFields(data, pscStatementId);

                String kind = data.getKind();
                if (IndividualPscRoles.includes(kind)) {
                    if (externalData.getSensitiveData() != null) {
                        pscDocument.setSensitiveData(transformSensitiveDataFields(
                                externalData.getSensitiveData()));
                    }

                    handleIndividualFields(data, pscData);
                }
                if (SecurePscRoles.includes(kind)) {
                    handleSecureFields(data, pscData);
                } else {
                    pscData.setAddress(new Address(data.getServiceAddress()));
                }

                pscDocument.setData(pscData);
            }
        }
        if (requestBody.getInternalData() != null) {
            InternalData internalData = requestBody.getInternalData();
            pscDocument.setDeltaAt(dateTimeFormatter.format(internalData.getDeltaAt()));

            pscDocument.setUpdated(new Updated().setAt(LocalDate.now()));
            pscDocument.setUpdatedBy(internalData.getUpdatedBy());
        }
        return pscDocument;
    }

    private PscSensitiveData transformSensitiveDataFields(SensitiveData sensitiveData) {
        PscSensitiveData pscSensitiveData = new PscSensitiveData();
        if (sensitiveData.getDateOfBirth() != null) {
            DateOfBirth dateOfBirth = new DateOfBirth(sensitiveData.getDateOfBirth());
            pscSensitiveData.setDateOfBirth(dateOfBirth);
        }
        pscSensitiveData.setResidentialAddressIsSameAsServiceAddress(
                sensitiveData.getResidentialAddressSameAsServiceAddress());
        if (sensitiveData.getUsualResidentialAddress() != null) {
            pscSensitiveData.setUsualResidentialAddress(
                    new Address(sensitiveData.getUsualResidentialAddress()));
        }
        return pscSensitiveData;
    }

    private PscData transformDataFields(Data data, String pscStatementId) {
        PscData pscData = new PscData();
        pscData.setCeasedOn(data.getCeasedOn());
        pscData.setDescription(data.getDescription());
        pscData.setEtag(data.getEtag());
        pscData.setKind(data.getKind());
        pscData.setNotifiedOn(data.getNotifiedOn());
        pscData.setLinks(PscTransformationHelper.createLinks(data, pscStatementId));
        pscData.setName(data.getName());
        pscData.setNationality(data.getNationality());
        pscData.setNaturesOfControl(data.getNaturesOfControl());
        pscData.setSanctioned(data.getIsSanctioned());
        if (data.getPrincipalOfficeAddress() != null) {
            pscData.setPrincipalOfficeAddress(new Address(data.getPrincipalOfficeAddress()));
        }
        pscData.setServiceAddressIsSameAsRegisteredOfficeAddress(
                data.getServiceAddressSameAsRegisteredOfficeAddress());
        if (data.getIdentification() != null &&
                (pscData.getKind().contains(CORPORATE) || pscData.getKind().contains(LEGAL))) {
            PscIdentification identification = new PscIdentification(data.getIdentification());
            pscData.setIdentification(identification);
        }
        return pscData;
    }

    private void handleIndividualFields(Data data, PscData pscData) {
        pscData.setNameElements(new NameElements(data.getNameElements()));
        pscData.setCountryOfResidence(data.getCountryOfResidence());
    }

    private void handleSecureFields(Data data, PscData pscData) {
        Boolean ceased = data.getCeasedOn() != null;
        pscData.setCeased(ceased);
    }

    private uk.gov.companieshouse.api.psc.DateOfBirth mapDateOfBirth(
            DateOfBirth inputDateOfBirth, boolean showFullDateOfBirth) {
        if (inputDateOfBirth != null) {
            uk.gov.companieshouse.api.psc.DateOfBirth dateOfBirth =
                    new uk.gov.companieshouse.api.psc.DateOfBirth();
            if (showFullDateOfBirth) {
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
            address.setCareOf(inputAddress.getCareOf());
            return address;
        } else {
            return null;
        }
    }

    private uk.gov.companieshouse.api.psc.UsualResidentialAddress mapToApiUra(
            Address inputAddress) {
        if (inputAddress != null) {
            uk.gov.companieshouse.api.psc.UsualResidentialAddress address =
                    new uk.gov.companieshouse.api.psc.UsualResidentialAddress();
            address.setAddressLine1(inputAddress.getAddressLine1());
            address.setAddressLine2(inputAddress.getAddressLine2());
            address.setCountry(inputAddress.getCountry());
            address.setLocality(inputAddress.getLocality());
            address.setPoBox(inputAddress.getPoBox());
            address.setPostalCode(inputAddress.getPostalCode());
            address.setPremise(inputAddress.getPremises());
            address.setRegion(inputAddress.getRegion());
            address.setCareOf(inputAddress.getCareOf());
            return address;
        } else {
            return null;
        }
    }


    private uk.gov.companieshouse.api.psc.Address mapPrincipleAddress(
            uk.gov.companieshouse.pscdataapi.models.Address inputPrincipleAddress) {
        if (inputPrincipleAddress != null) {
            uk.gov.companieshouse.api.psc.Address principleAddress =
                    new uk.gov.companieshouse.api.psc.Address();
            principleAddress.setAddressLine1(inputPrincipleAddress.getAddressLine1());
            principleAddress.setAddressLine2(inputPrincipleAddress.getAddressLine2());
            principleAddress.setCareOf(inputPrincipleAddress.getCareOf());
            principleAddress.setCountry(inputPrincipleAddress.getCountry());
            principleAddress.setLocality(inputPrincipleAddress.getLocality());
            principleAddress.setPoBox(inputPrincipleAddress.getPoBox());
            principleAddress.setPostalCode(inputPrincipleAddress.getPostalCode());
            principleAddress.setPremises(inputPrincipleAddress.getPremises());
            principleAddress.setRegion(inputPrincipleAddress.getRegion());
            return principleAddress;
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
            address.setCareOf(inputAddress.getCareOf());
            return address;
        } else {
            return null;
        }
    }

    private uk.gov.companieshouse.api.psc.NameElements mapNameElements(
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

    private Identification mapIdentification(
            PscIdentification inputIdentification, String kindString) {
        if (inputIdentification != null) {
            Identification identification = new Identification();
            identification.setLegalAuthority(inputIdentification.getLegalAuthority());
            identification.setLegalForm(inputIdentification.getLegalForm());
            if (kindString.equals(CORPORATE) || kindString.equals("list summary")) {
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
