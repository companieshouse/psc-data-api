package uk.gov.companieshouse.pscdataapi.util;

import java.io.InputStreamReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.psc.Data;
import uk.gov.companieshouse.api.psc.ExternalData;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.Identification;
import uk.gov.companieshouse.api.psc.InternalData;
import uk.gov.companieshouse.api.psc.ItemLinkTypes;
import uk.gov.companieshouse.api.psc.SensitiveData;
import uk.gov.companieshouse.api.psc.UsualResidentialAddress;
import uk.gov.companieshouse.pscdataapi.models.*;

public class TestHelper {
    public static final String INDIVIDUAL_KIND = "individual-person-with-significant-control";
    public static final String SECURE_KIND = "super-secure-person-with-significant-control";
    public static final String CORPORATE_KIND = "corporate-entity-person-with-significant-control";
    public static final String LEGAL_KIND = "legal-person-person-with-significant-control";
    public static final String INDIVIDUAL_BO_KIND = "individual-beneficial-owner";
    public static final String COMPANY_NUMBER = "companyNumber";
    public static final String NOTIFICATION_ID = "notificationId";
    public static final String X_REQUEST_ID = "654321";


    public static FullRecordCompanyPSCApi buildFullRecordPsc(String kind, Boolean registerView) {
        FullRecordCompanyPSCApi output  = new FullRecordCompanyPSCApi();

        ExternalData externalData = new ExternalData();
        Data data = new Data();
        data.setName("forename");
        data.setCompanyNumber("companyNumber");
        data.setKind(kind);
        data.setLinks(List.of(new ItemLinkTypes()));
        data.setServiceAddress(new uk.gov.companieshouse.api.psc.Address());

        Identification identification = new Identification();
        data.setIdentification(identification);
        if(kind.contains("corporate") || kind.contains("legal")) {
            identification.setLegalForm("Form");
            identification.setLegalAuthority("Authority");
            if(kind.contains("corporate")){
                identification.setCountryRegistered("Wales");
                identification.setPlaceRegistered("Cardiff");
                identification.setRegistrationNumber("16102009");
            }
        }
        
        SensitiveData sensitiveData = new SensitiveData();
        uk.gov.companieshouse.api.psc.DateOfBirth dateOfBirth = new uk.gov.companieshouse.api.psc.DateOfBirth();
        if(registerView == false){
            dateOfBirth.setDay(null);
        }
        else{
            dateOfBirth.setDay(21);
        }
        dateOfBirth.setMonth(12);
        dateOfBirth.setYear(1943);
        sensitiveData.setDateOfBirth(dateOfBirth);
        externalData.setData(data);
        externalData.setSensitiveData(sensitiveData);
        externalData.setNotificationId("id");
        externalData.setCompanyNumber("companyNumber");
        externalData.setPscId("pscId");

        InternalData internalData = new InternalData();
        internalData.setDeltaAt(OffsetDateTime.parse("2022-01-12T00:00:00Z"));
        internalData.setUpdatedAt(LocalDate.parse("2022-01-12"));

        if(kind.contains("individual")) {
            UsualResidentialAddress address = new UsualResidentialAddress();
            sensitiveData.setUsualResidentialAddress(address);
            uk.gov.companieshouse.api.psc.NameElements nameElements =
                    new uk.gov.companieshouse.api.psc.NameElements();
            nameElements.setSurname("surname");
            data.setNameElements(nameElements);
        } else if(kind.contains("secure")) {
            data.setCeasedOn(LocalDate.now());
        }

        output.setExternalData(externalData);
        output.setInternalData(internalData);
        return output;
    }

    public static PscDocument buildPscDocument(String kind, Boolean registerView) {
        PscDocument output = new PscDocument();

        PscData data = new PscData();
        data.setKind(kind);
        data.setName("forename");
        data.setLinks(new Links());
        if (!kind.contains("secure")) {
            data.setAddress(new Address());
        }

        output.setNotificationId("id");
        output.setData(data);
        output.setPscId("pscId");
        output.setCompanyNumber("1234567");
        output.setDeltaAt("20220112000000000000");
        Updated updated = new Updated();
        updated.setAt(LocalDate.now());
        output.setUpdated(updated);

        if(kind.contains("individual")) {
            PscSensitiveData sensitiveData = new PscSensitiveData();
            DateOfBirth dateOfBirth = new DateOfBirth();
            if(registerView == false){
                dateOfBirth.setDay(null);
            }
            else {
                dateOfBirth.setDay(21);
            }
            dateOfBirth.setMonth(12);
            dateOfBirth.setYear(1943);
            sensitiveData.setDateOfBirth(dateOfBirth);
            sensitiveData.setUsualResidentialAddress(new Address());
            output.setSensitiveData(sensitiveData);
            NameElements nameElements = new NameElements();
            nameElements.setSurname("surname");
            data.setNameElements(nameElements);
        } else if(kind.contains("secure")) {
            data.setCeasedOn(LocalDate.now());
            data.setCeased(true);
        } else if(kind.contains("legal")) {
            Identification identification = new Identification();
            identification.setLegalForm("Form");
            identification.setLegalAuthority("Authority");
            output.setIdentification(new PscIdentification(identification));
        } else if(kind.contains("corporate")) {
            Identification identification = new Identification();
            identification.setLegalForm("Form");
            identification.setLegalAuthority("Authority");
            identification.setCountryRegistered("Wales");
            identification.setPlaceRegistered("Cardiff");
            identification.setRegistrationNumber("16102009");
            output.setIdentification(new PscIdentification(identification));
        }

        return output;
    }

    public static String createJsonPayload() throws IOException {
        InputStreamReader exampleJsonPayload = new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream("psc_payload.json"));

        return FileCopyUtils.copyToString(exampleJsonPayload);
    }
}
