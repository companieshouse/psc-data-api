package uk.gov.companieshouse.pscdataapi.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.exemptions.*;
import uk.gov.companieshouse.api.metrics.CountsApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.PscApi;
import uk.gov.companieshouse.api.psc.Data;
import uk.gov.companieshouse.api.psc.ExternalData;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.Identification;
import uk.gov.companieshouse.api.psc.InternalData;
import uk.gov.companieshouse.api.psc.ItemLinkTypes;
import uk.gov.companieshouse.api.psc.ListSummary;
import uk.gov.companieshouse.api.psc.PscList;
import uk.gov.companieshouse.api.psc.SensitiveData;
import uk.gov.companieshouse.api.psc.UsualResidentialAddress;
import uk.gov.companieshouse.pscdataapi.models.Address;
import uk.gov.companieshouse.pscdataapi.models.DateOfBirth;
import uk.gov.companieshouse.pscdataapi.models.Links;
import uk.gov.companieshouse.pscdataapi.models.NameElements;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.models.PscIdentification;
import uk.gov.companieshouse.pscdataapi.models.PscSensitiveData;
import uk.gov.companieshouse.pscdataapi.models.Updated;

import static uk.gov.companieshouse.api.exemptions.PscExemptAsTradingOnRegulatedMarketItem.ExemptionTypeEnum.PSC_EXEMPT_AS_TRADING_ON_REGULATED_MARKET;
import static uk.gov.companieshouse.api.exemptions.PscExemptAsTradingOnUkRegulatedMarketItem.ExemptionTypeEnum.PSC_EXEMPT_AS_TRADING_ON_UK_REGULATED_MARKET;

public class TestHelper {
    public static final String INDIVIDUAL_KIND = "individual-person-with-significant-control";
    public static final String CORPORATE_KIND = "corporate-entity-person-with-significant-control";
    public static final String LEGAL_KIND = "legal-person-person-with-significant-control";
    public static final String SECURE_KIND = "super-secure-person-with-significant-control";
    public static final String INDIVIDUAL_BO_KIND = "individual-beneficial-owner";
    public static final String COMPANY_NUMBER = "companyNumber";
    public static final String NOTIFICATION_ID = "notificationId";
    public static final String PSC_ID = "pscId";
    public static final String PSC_STATEMENT_ID = "pscStatementId";
    public static final String X_REQUEST_ID = "654321";
    private static final LocalDate EXEMPTION_DATE = LocalDate.of(2022, 11, 3);

    public TestHelper(){}

    public static FullRecordCompanyPSCApi buildFullRecordPsc(String kind) {
        return buildFullRecordPsc(kind, false, true);
    }

    public static FullRecordCompanyPSCApi buildFullRecordPsc(String kind, boolean showFullDateOfBirth, boolean pscStatementsExists) {
        FullRecordCompanyPSCApi output  = new FullRecordCompanyPSCApi();
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
        data.setCeasedOn(LocalDate.of(2012,12,12));
        data.setNotifiedOn(LocalDate.of(2000,12,12));
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
        if (kind.contains("beneficial")){
            data.setIsSanctioned(true);
        }

        if(kind.contains("individual")) {
            SensitiveData sensitiveData = new SensitiveData();
            uk.gov.companieshouse.api.psc.DateOfBirth dateOfBirth = new uk.gov.companieshouse.api.psc.DateOfBirth();
            if(showFullDateOfBirth){
                dateOfBirth.setDay(21);
            } else{
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
        } else if(kind.contains("corporate")) {
            Identification identification = new Identification();
            identification.setLegalForm("Form");
            identification.setLegalAuthority("Authority");
            identification.setCountryRegistered("Wales");
            identification.setPlaceRegistered("Cardiff");
            identification.setRegistrationNumber("16102009");
            data.setIdentification(identification);
        } else if(kind.contains("legal")) {
            Identification identification = new Identification();
            identification.setLegalForm("Form");
            identification.setLegalAuthority("Authority");
            data.setIdentification(identification);
        } else if(kind.contains("secure")) {
            data.setCeasedOn(LocalDate.parse("2022-01-14"));
            data.setDescription("description");
        }

        externalData.setData(data);
        output.setExternalData(externalData);
        return output;
    }

    public static FullRecordCompanyPSCApi buildBasicFullRecordPsc(){
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
        pscData.setCeasedOn(LocalDate.of(2012,12,12));
        pscData.setNotifiedOn(LocalDate.of(2000,12,12));
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
        if (kind.contains("beneficial")){
            pscData.setSanctioned(true);
        }

        if(kind.contains("individual")) {
            PscSensitiveData sensitiveData = new PscSensitiveData();
            DateOfBirth dateOfBirth = new DateOfBirth();
            if(showFullDateOfBirth){
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
        } else if(kind.contains("corporate")) {
            Identification identification = new Identification();
            identification.setLegalForm("Form");
            identification.setLegalAuthority("Authority");
            identification.setCountryRegistered("Wales");
            identification.setPlaceRegistered("Cardiff");
            identification.setRegistrationNumber("16102009");
            pscData.setIdentification(new PscIdentification(identification));
        } else if(kind.contains("legal")) {
            Identification identification = new Identification();
            identification.setLegalForm("Form");
            identification.setLegalAuthority("Authority");
            pscData.setIdentification(new PscIdentification(identification));
        } else if(kind.contains("secure")) {
            pscData.setCeasedOn(LocalDate.parse("2022-01-14"));
            pscData.setCeased(true);
            pscData.setDescription("description");
        }

        output.setData(pscData);
        return output;
    }

    public static PscDocument buildBasicDocument(){
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
        if (inputStream == null){
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

    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    public static OffsetDateTime createOffsetDateTime() {
        LocalDateTime localDate = LocalDateTime.parse("2023-01-02T13:04:05.678", formatter);
        ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(localDate);
        return OffsetDateTime.of(localDate, offset);
    }
    public static OffsetDateTime createLaterOffsetDateTime() {
        LocalDateTime laterLocalDate = LocalDateTime.parse("2023-01-03T13:04:05.678", formatter);
        ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(laterLocalDate);
        return OffsetDateTime.of(laterLocalDate, offset);
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

    public CompanyExemptions createExemptions () {
        CompanyExemptions exemptions = new CompanyExemptions();
        exemptions.setExemptions(getExemptions());
        return exemptions;
    }

    private Exemptions getExemptions() {
        ExemptionItem exemptionItem = new ExemptionItem();
        exemptionItem.exemptFrom(EXEMPTION_DATE);
        exemptionItem.exemptTo(null);

        List<ExemptionItem> exemptionItems = Collections.singletonList(exemptionItem);


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

}
