package uk.gov.companieshouse.pscdataapi.util;

import java.io.InputStreamReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.metrics.*;
import uk.gov.companieshouse.api.psc.*;
import uk.gov.companieshouse.api.psc.InternalData;
import uk.gov.companieshouse.pscdataapi.models.*;
import uk.gov.companieshouse.pscdataapi.models.Address;
import uk.gov.companieshouse.pscdataapi.models.DateOfBirth;
import uk.gov.companieshouse.pscdataapi.models.NameElements;

public class TestHelper {
    public static final String INDIVIDUAL_KIND = "individual-person-with-significant-control";
    public static final String CORPORATE_KIND = "corporate-entity-person-with-significant-control";
    public static final String LEGAL_KIND = "legal-person-person-with-significant-control";
    public static final String SECURE_KIND = "super-secure-person-with-significant-control";
    public static final String INDIVIDUAL_BO_KIND = "individual-beneficial-owner";
    public static final String COMPANY_NUMBER = "companyNumber";
    public static final String NOTIFICATION_ID = "notificationId";
    public static final String X_REQUEST_ID = "654321";


    public static FullRecordCompanyPSCApi buildFullRecordPsc(String kind) {
        return buildFullRecordPsc(kind, false);
    }

    public static FullRecordCompanyPSCApi buildFullRecordPsc(String kind, boolean registerView) {
        FullRecordCompanyPSCApi output  = new FullRecordCompanyPSCApi();
        ExternalData externalData = new ExternalData();
        Data data = new Data();

        externalData.setPscId("pscId");
        externalData.setCompanyNumber("1234567");

        InternalData internalData = new InternalData();
        internalData.setDeltaAt(OffsetDateTime.parse("2022-01-12T00:00:00Z"));
        internalData.setUpdatedBy("user");
        output.setInternalData(internalData);

        data.setKind(kind);
        data.setEtag("etag");
        data.setName("wholeName");
        ItemLinkTypes links = new ItemLinkTypes();
        links.setSelf("self");
        links.setStatements("linkStatements");
        data.setLinks(List.of(links));
        data.serviceAddressSameAsRegisteredOfficeAddress(true);
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
            if(registerView){
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

    public static PscDocument buildPscDocument(String kind) {
        return buildPscDocument(kind, false);
    }

    public static PscDocument buildPscDocument(String kind, boolean registerView) {
        PscDocument output = new PscDocument();
        PscData data = new PscData();

        output.setId("pscId");
        output.setNotificationId("pscId");
        output.setPscId("pscId");
        output.setCompanyNumber("1234567");
        output.setDeltaAt("20220112000000000000");
        output.setUpdated(new Updated().setAt(LocalDate.now()));
        output.setUpdatedBy("user");

        data.setKind(kind);
        data.setEtag("etag");
        data.setName("wholeName");
        Links links = new Links();
        links.setSelf("self");
        links.setStatements("linkStatements");
        data.setLinks(links);
        data.setServiceAddressIsSameAsRegisteredOfficeAddress(true);
        data.setNaturesOfControl(List.of("part-right-to-share-surplus-assets-75-to-100-percent",
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
            data.setAddress(address);
        }
        if (kind.contains("beneficial")){
            data.setSanctioned(true);
        }

        if(kind.contains("individual")) {
            PscSensitiveData sensitiveData = new PscSensitiveData();
            DateOfBirth dateOfBirth = new DateOfBirth();
            if(registerView){
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
            output.setIdentification(new PscIdentification(identification));
        } else if(kind.contains("legal")) {
            Identification identification = new Identification();
            identification.setLegalForm("Form");
            identification.setLegalAuthority("Authority");
            output.setIdentification(new PscIdentification(identification));
        } else if(kind.contains("secure")) {
            data.setCeasedOn(LocalDate.parse("2022-01-14"));
            data.setCeased(true);
            data.setDescription("description");
        }

        output.setData(data);
        return output;
    }

    public static String createJsonPayload() throws IOException {
        InputStreamReader exampleJsonPayload = new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream("psc_payload.json"));

        return FileCopyUtils.copyToString(exampleJsonPayload);
    }

    public PscList createPscList() {
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

    private ListSummary createListSummary() {
        ListSummary listSummary = new ListSummary();
        listSummary.setEtag("string");
        listSummary.setName("string");
        NameElements nameElements = new NameElements();
        nameElements.setTitle("Mr");
        nameElements.setForename("Forname");
        nameElements.setMiddleName("Middle");
        nameElements.setSurname("Surname");
        listSummary.setNameElements(nameElements);
        uk.gov.companieshouse.api.psc.Address address = new uk.gov.companieshouse.api.psc.Address();
        address.setAddressLine1("1 street");
        address.setAddressLine2("2 street");
        address.setCountry("uk");
        address.setRegion("south");
        address.setPremises("prem");
        address.setPoBox("po");
        address.setLocality("Local");
        address.setCareOf("care");
        address.setPostalCode("post");
        listSummary.setAddress(address);
        listSummary.setCeasedOn(LocalDate.now());
        listSummary.setIsSanctioned(true);
        listSummary.setNationality("British");
        listSummary.setCountryOfResidence("Uk");
        listSummary.setDescription(ListSummary.DescriptionEnum
                .SUPER_SECURE_PERSONS_WITH_SIGNIFICANT_CONTROL);
        listSummary.setPrincipalOfficeAddress(address);
        return listSummary;
    }

    private Links createLinks() {
        Links links = new Links();
        links.setSelf(String.format("/company/%s/persons-with-significant-control", "1234567"));
        return links;
    }

    public MetricsApi createMetrics() {
        MetricsApi metrics = new MetricsApi();
        CountsApi counts = new CountsApi();
        PscApi pscs = new PscApi();
        pscs.setActiveStatementsCount(1);
        pscs.setWithdrawnStatementsCount(1);
        pscs.setStatementsCount(2);
        counts.setPersonsWithSignificantControl(pscs);
        metrics.setCounts(counts);
        return metrics;
    }

    public PscList createPscListWithNoMetrics() {
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

    public PscList createPscListRegisterView() {
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
        pscList.setCeasedCount(0);
        pscList.setTotalResults(1);
        pscList.setStartIndex(0);
        pscList.setItemsPerPage(25);
        pscList.setLinks(createLinks());
        return pscList;
    }

    public MetricsApi createMetricsRegisterView() {
        MetricsApi metrics = new MetricsApi();
        CountsApi counts = new CountsApi();
        PscApi pscs = new PscApi();
        pscs.setActiveStatementsCount(1);
        pscs.setWithdrawnStatementsCount(0);
        pscs.setStatementsCount(1);
        counts.setPersonsWithSignificantControl(pscs);
        metrics.setCounts(counts);

        RegistersApi registers = new RegistersApi();
        RegisterApi pscStatements = new RegisterApi();
        pscStatements.setRegisterMovedTo("public-register");
        String date = "2020-12-20T06:00:00Z";
        OffsetDateTime dt = OffsetDateTime.parse(date);
        pscStatements.setMovedOn(dt);
        registers.setPersonsWithSignificantControl(pscStatements);
        metrics.setRegisters(registers);
        return metrics;
    }
}
