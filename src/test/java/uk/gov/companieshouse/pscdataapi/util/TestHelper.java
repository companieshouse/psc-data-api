package uk.gov.companieshouse.pscdataapi.util;

import static uk.gov.companieshouse.api.exemptions.PscExemptAsTradingOnRegulatedMarketItem.ExemptionTypeEnum.PSC_EXEMPT_AS_TRADING_ON_REGULATED_MARKET;
import static uk.gov.companieshouse.api.exemptions.PscExemptAsTradingOnUkRegulatedMarketItem.ExemptionTypeEnum.PSC_EXEMPT_AS_TRADING_ON_UK_REGULATED_MARKET;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.exemptions.CompanyExemptions;
import uk.gov.companieshouse.api.exemptions.ExemptionItem;
import uk.gov.companieshouse.api.exemptions.Exemptions;
import uk.gov.companieshouse.api.exemptions.PscExemptAsTradingOnRegulatedMarketItem;
import uk.gov.companieshouse.api.exemptions.PscExemptAsTradingOnUkRegulatedMarketItem;
import uk.gov.companieshouse.api.metrics.CountsApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.PscApi;
import uk.gov.companieshouse.api.psc.*;
import uk.gov.companieshouse.pscdataapi.models.Address;
import uk.gov.companieshouse.pscdataapi.models.DateOfBirth;
import uk.gov.companieshouse.pscdataapi.models.Links;
import uk.gov.companieshouse.pscdataapi.models.NameElements;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.models.PscIdentification;
import uk.gov.companieshouse.pscdataapi.models.PscIdentityVerificationDetails;
import uk.gov.companieshouse.pscdataapi.models.PscSensitiveData;
import uk.gov.companieshouse.pscdataapi.models.Updated;

public class TestHelper {

    public static final String INDIVIDUAL_KIND = Individual.KindEnum.INDIVIDUAL_PERSON_WITH_SIGNIFICANT_CONTROL.toString();
    public static final String CORPORATE_KIND = CorporateEntity.KindEnum.CORPORATE_ENTITY_PERSON_WITH_SIGNIFICANT_CONTROL.toString();
    public static final String LEGAL_KIND = LegalPerson.KindEnum.LEGAL_PERSON_PERSON_WITH_SIGNIFICANT_CONTROL.toString();
    public static final String SECURE_KIND = SuperSecure.KindEnum.SUPER_SECURE_PERSON_WITH_SIGNIFICANT_CONTROL.toString();
    public static final String INDIVIDUAL_BO_KIND = IndividualBeneficialOwner.KindEnum.INDIVIDUAL_BENEFICIAL_OWNER.toString();
    public static final String CORPORATE_BO_KIND = CorporateEntityBeneficialOwner.KindEnum.CORPORATE_ENTITY_BENEFICIAL_OWNER.toString();
    public static final String LEGAL_BO_KIND = LegalPersonBeneficialOwner.KindEnum.LEGAL_PERSON_BENEFICIAL_OWNER.toString();
    public static final String SECURE_BO_KIND = SuperSecureBeneficialOwner.KindEnum.SUPER_SECURE_BENEFICIAL_OWNER.toString();

    public static final String DELTA_AT = "20240219123045999999";
    public static final String STALE_DELTA_AT = "20240119123045999999";
    public static final String COMPANY_NUMBER = "companyNumber";
    public static final String NOTIFICATION_ID = "notificationId";
    public static final String PSC_ID = "pscId";
    public static final String PSC_STATEMENT_ID = "pscStatementId";
    public static final String X_REQUEST_ID = "654321";
    private static final LocalDate EXEMPTION_DATE = LocalDate.of(2022, 11, 3);

    public static FullRecordCompanyPSCApi buildFullRecordPsc(String kind) {
        return buildFullRecordPsc(kind, false, true);
    }

    public static FullRecordCompanyPSCApi buildFullRecordPsc(String kind, boolean showFullDateOfBirth,
            boolean pscStatementsExists) {
        FullRecordCompanyPSCApi output = new FullRecordCompanyPSCApi();
        ExternalData externalData = new ExternalData();
        Data data = new Data();

        externalData.setPscId(PSC_ID);

        if (pscStatementsExists) {
            externalData.setPscStatementId(PSC_STATEMENT_ID);
        }

        // Not setting the notificationId as that is passed to the Transformer
        externalData.setCompanyNumber(COMPANY_NUMBER);

        InternalData internalData = new InternalData();
        internalData.setDeltaAt(OffsetDateTime.parse("2022-01-12T00:00:00Z"));
        internalData.setUpdatedBy("user");
        output.setInternalData(internalData);

        data.setKind(kind);
        data.setCeasedOn(LocalDate.of(2012, 12, 12));
        data.setNotifiedOn(LocalDate.of(2000, 12, 12));
        data.setEtag("etag");
        data.setName("wholeName");
        ItemLinkTypes links = new ItemLinkTypes();
        links.setSelf("self");

        if (pscStatementsExists) {
            links.setStatement("linkStatements");
        }

        data.setLinks(List.of(links));

        data.serviceAddressSameAsRegisteredOfficeAddress(false);
        uk.gov.companieshouse.api.psc.Address principalOfficeAddress = new uk.gov.companieshouse.api.psc.Address();
        principalOfficeAddress.setAddressLine1("office_line1");
        principalOfficeAddress.setAddressLine2("office_line2");
        principalOfficeAddress.setCareOf("office_careof");
        principalOfficeAddress.setCountry("office_country");
        principalOfficeAddress.setLocality("office_locality");
        principalOfficeAddress.setPoBox("office_pobox");
        principalOfficeAddress.setPostalCode("office_postalcode");
        principalOfficeAddress.setPremises("office_premises");
        principalOfficeAddress.setRegion("office_region");
        if (kind.contains("legal") && kind.contains("beneficial")) {
            data.setPrincipalOfficeAddress(principalOfficeAddress);
        } else if (kind.contains("corporate") && kind.contains("beneficial")) {
            data.setPrincipalOfficeAddress(principalOfficeAddress);
        }

        data.naturesOfControl(List.of("part-right-to-share-surplus-assets-75-to-100-percent",
                "right-to-appoint-and-remove-directors-as-trust-registered-overseas-entity",
                "significant-influence-or-control-as-trust-registered-overseas-entity"));

        uk.gov.companieshouse.api.psc.Address address = new uk.gov.companieshouse.api.psc.Address();
        address.setAddressLine1("sa_line1");
        address.setAddressLine2("sa_line2");
        address.setCareOf("sa_care_of");
        address.setCountry("United Kingdom");
        address.setLocality("Cardiff");
        address.setPoBox("sa_po");
        address.setPostalCode("CF2 1B6");
        address.setPremises("SA");
        address.setRegion("sa_region");
        if (!kind.contains("secure")) {
            data.setServiceAddress(address);
        }
        if (kind.contains("beneficial")) {
            data.setIsSanctioned(true);
        }

        if (kind.contains("individual")) {
            SensitiveData sensitiveData = new SensitiveData();
            uk.gov.companieshouse.api.psc.DateOfBirth dateOfBirth = new uk.gov.companieshouse.api.psc.DateOfBirth();
            if (showFullDateOfBirth) {
                dateOfBirth.setDay(21);
            } else {
                dateOfBirth.setDay(null);
            }
            dateOfBirth.setMonth(12);
            dateOfBirth.setYear(1943);
            sensitiveData.setDateOfBirth(dateOfBirth);
            UsualResidentialAddress usualResidentialAddress = new UsualResidentialAddress();
            usualResidentialAddress.setAddressLine1("sa_line1");
            usualResidentialAddress.setAddressLine2("sa_line2");
            usualResidentialAddress.setCareOf("sa_care_of");
            usualResidentialAddress.setCountry("United Kingdom");
            usualResidentialAddress.setLocality("Cardiff");
            usualResidentialAddress.setPoBox("sa_po");
            usualResidentialAddress.setPostalCode("CF2 1B6");
            usualResidentialAddress.setPremise("SA");
            usualResidentialAddress.setRegion("sa_region");
            sensitiveData.setUsualResidentialAddress(usualResidentialAddress);
            sensitiveData.setResidentialAddressSameAsServiceAddress(true);
            externalData.setSensitiveData(sensitiveData);

            uk.gov.companieshouse.api.psc.NameElements nameElements =
                    new uk.gov.companieshouse.api.psc.NameElements();
            nameElements.setTitle("Mr");
            nameElements.setForename("John");
            nameElements.setMiddleName("George");
            nameElements.setSurname("Doe");
            data.setNameElements(nameElements);

            data.setNationality("British");
            data.setCountryOfResidence("England");

            IdentityVerificationDetails ivd = new IdentityVerificationDetails();
            ivd.setAntiMoneyLaunderingSupervisoryBodies(List.of("Supervisory Body"));
            ivd.setAppointmentVerificationEndOn(LocalDate.of(2024, 12, 12));
            ivd.setAppointmentVerificationStatementDate(LocalDate.of(2024, 12, 13));
            ivd.setAppointmentVerificationStatementDueOn(LocalDate.of(2024, 12, 14));
            ivd.setAppointmentVerificationStartOn(LocalDate.of(2024, 12, 15));
            ivd.setAuthorisedCorporateServiceProviderName("Service Provider");
            ivd.setIdentityVerifiedOn(LocalDate.of(2024, 12, 16));
            ivd.setPreferredName("Preferred Name");
            data.setIdentityVerificationDetails(ivd);
        } else if (kind.contains("corporate")) {
            Identification identification = new Identification();
            identification.setLegalForm("Form");
            identification.setLegalAuthority("Authority");
            identification.setCountryRegistered("Wales");
            identification.setPlaceRegistered("Cardiff");
            identification.setRegistrationNumber("16102009");
            data.setIdentification(identification);
        } else if (kind.contains("legal")) {
            Identification identification = new Identification();
            identification.setLegalForm("Form");
            identification.setLegalAuthority("Authority");
            data.setIdentification(identification);
        } else if (kind.contains("secure")) {
            data.setCeasedOn(LocalDate.parse("2022-01-14"));
            data.setDescription("description");
        }

        externalData.setData(data);
        output.setExternalData(externalData);
        return output;
    }

    public static FullRecordCompanyPSCApi buildBasicFullRecordPsc() {
        FullRecordCompanyPSCApi result = new FullRecordCompanyPSCApi();
        InternalData internal = new InternalData();
        ExternalData external = new ExternalData();
        Data data = new Data();
        external.setNotificationId(NOTIFICATION_ID);
        data.setKind(INDIVIDUAL_KIND);
        external.setData(data);
        internal.setDeltaAt(createOffsetDateTime());
        result.setExternalData(external);
        result.setInternalData(internal);
        return result;
    }

    public static PscDocument buildPscDocument(String kind) {
        return buildPscDocument(kind, false, true);
    }

    public static PscDocument buildPscDocument(String kind, boolean showFullDateOfBirth, boolean pscStatementsExists) {
        PscDocument output = new PscDocument();
        PscData pscData = new PscData();

        output.setId(NOTIFICATION_ID);
        output.setNotificationId(NOTIFICATION_ID);
        output.setPscId(PSC_ID);
        output.setCompanyNumber(COMPANY_NUMBER);
        output.setDeltaAt("20220112000000000000");
        output.setUpdated(new Updated().setAt(LocalDate.now()));
        output.setUpdatedBy("user");

        pscData.setKind(kind);
        pscData.setCeasedOn(LocalDate.of(2012, 12, 12));
        pscData.setNotifiedOn(LocalDate.of(2000, 12, 12));
        pscData.setEtag("etag");
        pscData.setName("wholeName");
        Links links = new Links();
        links.setSelf("self");
        if (pscStatementsExists) {
            links.setStatement("linkStatements");
        }
        pscData.setLinks(links);

        pscData.setServiceAddressIsSameAsRegisteredOfficeAddress(false);

        Address principalOfficeAddress = new Address();
        principalOfficeAddress.setAddressLine1("office_line1");
        principalOfficeAddress.setAddressLine2("office_line2");
        principalOfficeAddress.setCareOf("office_careof");
        principalOfficeAddress.setCountry("office_country");
        principalOfficeAddress.setLocality("office_locality");
        principalOfficeAddress.setPoBox("office_pobox");
        principalOfficeAddress.setPostalCode("office_postalcode");
        principalOfficeAddress.setPremises("office_premises");
        principalOfficeAddress.setRegion("office_region");
        if (kind.contains("legal") && kind.contains("beneficial")) {
            pscData.setPrincipalOfficeAddress(principalOfficeAddress);
        } else if (kind.contains("corporate") && kind.contains("beneficial")) {
            pscData.setPrincipalOfficeAddress(principalOfficeAddress);
        }

        pscData.setNaturesOfControl(List.of("part-right-to-share-surplus-assets-75-to-100-percent",
                "right-to-appoint-and-remove-directors-as-trust-registered-overseas-entity",
                "significant-influence-or-control-as-trust-registered-overseas-entity"));

        Address address = new Address();
        address.setAddressLine1("sa_line1");
        address.setAddressLine2("sa_line2");
        address.setCareOf("sa_care_of");
        address.setCountry("United Kingdom");
        address.setLocality("Cardiff");
        address.setPoBox("sa_po");
        address.setPostalCode("CF2 1B6");
        address.setPremises("SA");
        address.setRegion("sa_region");
        if (!kind.contains("secure")) {
            pscData.setAddress(address);
        }
        if (kind.contains("beneficial")) {
            pscData.setSanctioned(true);
        }

        if (kind.contains("individual")) {
            PscSensitiveData sensitiveData = new PscSensitiveData();
            DateOfBirth dateOfBirth = new DateOfBirth();
            if (showFullDateOfBirth) {
                dateOfBirth.setDay(21);
            } else {
                dateOfBirth.setDay(null);
            }
            dateOfBirth.setMonth(12);
            dateOfBirth.setYear(1943);
            sensitiveData.setDateOfBirth(dateOfBirth);
            sensitiveData.setUsualResidentialAddress(address);
            sensitiveData.setResidentialAddressIsSameAsServiceAddress(true);
            output.setSensitiveData(sensitiveData);

            NameElements nameElements = new NameElements();
            nameElements.setTitle("Mr");
            nameElements.setForename("John");
            nameElements.setMiddleName("George");
            nameElements.setSurname("Doe");
            pscData.setNameElements(nameElements);

            pscData.setNationality("British");
            pscData.setCountryOfResidence("England");

            PscIdentityVerificationDetails ivd = getPscIdentityVerificationDetails();
            pscData.setIdentityVerificationDetails(ivd);
        } else if (kind.contains("corporate")) {
            Identification identification = new Identification();
            identification.setLegalForm("Form");
            identification.setLegalAuthority("Authority");
            identification.setCountryRegistered("Wales");
            identification.setPlaceRegistered("Cardiff");
            identification.setRegistrationNumber("16102009");
            pscData.setIdentification(new PscIdentification(identification));
        } else if (kind.contains("legal")) {
            Identification identification = new Identification();
            identification.setLegalForm("Form");
            identification.setLegalAuthority("Authority");
            pscData.setIdentification(new PscIdentification(identification));
        } else if (kind.contains("secure")) {
            pscData.setCeasedOn(LocalDate.parse("2022-01-14"));
            pscData.setCeased(true);
            pscData.setDescription("description");
        }

        output.setData(pscData);
        return output;
    }

    private static @NotNull PscIdentityVerificationDetails getPscIdentityVerificationDetails() {
        PscIdentityVerificationDetails ivd = new PscIdentityVerificationDetails();
        ivd.setAntiMoneyLaunderingSupervisoryBodies(List.of("Supervisory Body"));
        ivd.setAppointmentVerificationEndOn(LocalDate.of(2024, 12, 12));
        ivd.setAppointmentVerificationStatementDate(LocalDate.of(2024, 12, 13));
        ivd.setAppointmentVerificationStatementDueOn(LocalDate.of(2024, 12, 14));
        ivd.setAppointmentVerificationStartOn(LocalDate.of(2024, 12, 15));
        ivd.setAuthorisedCorporateServiceProviderName("Service Provider");
        ivd.setIdentityVerifiedOn(LocalDate.of(2024, 12, 16));
        ivd.setPreferredName("Preferred Name");
        return ivd;
    }

    public static PscDocument buildBasicDocument() {
        PscDocument document = new PscDocument();
        document.setUpdated(new Updated().setAt(LocalDate.now()));
        document.setCompanyNumber(COMPANY_NUMBER);
        document.setPscId(PSC_ID);
        document.setNotificationId(COMPANY_NUMBER);
        PscData pscData = new PscData();
        pscData.setKind(INDIVIDUAL_KIND);
        document.setData(pscData);
        PscIdentification identification = new PscIdentification();
        identification.setCountryRegistered("x");
        identification.setLegalForm("x");
        identification.setPlaceRegistered("x");
        identification.setLegalAuthority("x");
        identification.setRegistrationNumber("x");
        pscData.setIdentification(identification);
        return document;
    }

    public static String createJsonPayload() throws IOException {
        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("psc_payload.json");
        if (inputStream == null) {
            throw new IOException("Failed to load Json payload input stream");
        }
        InputStreamReader exampleJsonPayload = new InputStreamReader(inputStream);

        return FileCopyUtils.copyToString(exampleJsonPayload);
    }

    public static PscList createPscList() {
        ListSummary listSummary = new ListSummary();
        Identification identification = new Identification();
        identification.setPlaceRegistered("x");
        identification.setCountryRegistered("x");
        identification.setRegistrationNumber("x");
        identification.setLegalAuthority("x");
        identification.setLegalForm("x");
        listSummary.setIdentification(identification);
        PscList pscList = new PscList();
        pscList.setItems(Collections.singletonList(listSummary));
        pscList.setActiveCount(1);
        pscList.setCeasedCount(1);
        pscList.setTotalResults(2);
        pscList.setStartIndex(0);
        pscList.setItemsPerPage(25);
        pscList.setLinks(createLinks());
        return pscList;
    }

    private static Links createLinks() {
        Links links = new Links();
        links.setSelf(String.format("/company/%s/persons-with-significant-control", COMPANY_NUMBER));
        return links;
    }

    public static PscList createPscListWithExemptions() {
        ListSummary listSummary = new ListSummary();
        Identification identification = new Identification();
        identification.setPlaceRegistered("x");
        identification.setCountryRegistered("x");
        identification.setRegistrationNumber("x");
        identification.setLegalAuthority("x");
        identification.setLegalForm("x");
        listSummary.setIdentification(identification);
        PscList pscList = new PscList();
        pscList.setItems(Collections.singletonList(listSummary));
        pscList.setActiveCount(1);
        pscList.setCeasedCount(1);
        pscList.setTotalResults(2);
        pscList.setStartIndex(0);
        pscList.setItemsPerPage(25);
        pscList.setLinks(createLinksWithExemptions());
        return pscList;
    }

    private static Links createLinksWithExemptions() {
        Links links = new Links();
        links.setSelf(String.format("/company/%s/persons-with-significant-control", COMPANY_NUMBER));
        links.setExemptions(String.format("/company/%s/exemptions", COMPANY_NUMBER));
        return links;
    }

    public static MetricsApi createMetrics() {
        MetricsApi metrics = new MetricsApi();
        CountsApi counts = new CountsApi();
        PscApi pscs = new PscApi();
        pscs.setActivePscsCount(1);
        pscs.setCeasedPscsCount(1);
        pscs.setPscsCount(2);
        counts.setPersonsWithSignificantControl(pscs);
        metrics.setCounts(counts);
        return metrics;
    }

    public static PscList createPscListWithNoMetrics() {
        ListSummary listSummary = new ListSummary();
        Identification identification = new Identification();
        identification.setPlaceRegistered("x");
        identification.setCountryRegistered("x");
        identification.setRegistrationNumber("x");
        identification.setLegalAuthority("x");
        identification.setLegalForm("x");
        listSummary.setIdentification(identification);
        PscList pscList = new PscList();
        pscList.setItems(Collections.singletonList(listSummary));
        pscList.setStartIndex(0);
        pscList.setItemsPerPage(25);
        pscList.setLinks(createLinks());
        return pscList;
    }

    public Exemptions getUkExemptions() {
        ExemptionItem exemptionItem = new ExemptionItem();
        exemptionItem.exemptFrom(EXEMPTION_DATE);
        exemptionItem.exemptTo(null);

        List<ExemptionItem> exemptionItems = Collections.singletonList(exemptionItem);

        PscExemptAsTradingOnUkRegulatedMarketItem nonUkEeaStateMarket = new PscExemptAsTradingOnUkRegulatedMarketItem();

        nonUkEeaStateMarket.setItems(exemptionItems);
        nonUkEeaStateMarket.setExemptionType(PSC_EXEMPT_AS_TRADING_ON_UK_REGULATED_MARKET);

        PscExemptAsTradingOnUkRegulatedMarketItem ukEeaStateMarket = new PscExemptAsTradingOnUkRegulatedMarketItem();
        ukEeaStateMarket.setItems(exemptionItems);
        ukEeaStateMarket.setExemptionType(PSC_EXEMPT_AS_TRADING_ON_UK_REGULATED_MARKET);

        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsTradingOnUkRegulatedMarket(nonUkEeaStateMarket);
        exemptions.setPscExemptAsTradingOnUkRegulatedMarket(ukEeaStateMarket);

        return exemptions;
    }

    public CompanyExemptions createExemptions() {
        CompanyExemptions exemptions = new CompanyExemptions();
        exemptions.setExemptions(getExemptions());
        return exemptions;
    }

    private Exemptions getExemptions() {
        ExemptionItem exemptionItem = new ExemptionItem();
        exemptionItem.exemptFrom(EXEMPTION_DATE);
        exemptionItem.exemptTo(null);

        ExemptionItem ceasedExemptionItem = new ExemptionItem();
        exemptionItem.exemptFrom(EXEMPTION_DATE);
        exemptionItem.exemptTo(EXEMPTION_DATE);

        List<ExemptionItem> exemptionItems = Arrays.asList(exemptionItem, ceasedExemptionItem);

        PscExemptAsTradingOnRegulatedMarketItem nonUkEeaStateMarket = new PscExemptAsTradingOnRegulatedMarketItem();
        nonUkEeaStateMarket.setItems(exemptionItems);
        nonUkEeaStateMarket.setExemptionType(PSC_EXEMPT_AS_TRADING_ON_REGULATED_MARKET);

        PscExemptAsTradingOnUkRegulatedMarketItem ukEeaStateMarket = new PscExemptAsTradingOnUkRegulatedMarketItem();
        ukEeaStateMarket.setItems(exemptionItems);
        ukEeaStateMarket.setExemptionType(PSC_EXEMPT_AS_TRADING_ON_UK_REGULATED_MARKET);

        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsTradingOnRegulatedMarket(nonUkEeaStateMarket);
        exemptions.setPscExemptAsTradingOnUkRegulatedMarket(ukEeaStateMarket);

        return exemptions;
    }

    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static OffsetDateTime createOffsetDateTime() {
        LocalDateTime localDate = LocalDateTime.parse("2023-01-02T13:04:05.678", formatter);
        return OffsetDateTime.of(localDate, ZoneOffset.UTC);
    }

    public static OffsetDateTime createLaterOffsetDateTime() {
        LocalDateTime laterLocalDate = LocalDateTime.parse("2023-01-03T13:04:05.678", formatter);
        return OffsetDateTime.of(laterLocalDate, ZoneOffset.UTC);
    }
}
