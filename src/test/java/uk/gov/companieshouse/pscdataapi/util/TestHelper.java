package uk.gov.companieshouse.pscdataapi.util;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import uk.gov.companieshouse.api.psc.Data;
import uk.gov.companieshouse.api.psc.ExternalData;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.InternalData;
import uk.gov.companieshouse.api.psc.ItemLinkTypes;
import uk.gov.companieshouse.api.psc.SensitiveData;
import uk.gov.companieshouse.api.psc.UsualResidentialAddress;
import uk.gov.companieshouse.pscdataapi.models.Address;
import uk.gov.companieshouse.pscdataapi.models.DateOfBirth;
import uk.gov.companieshouse.pscdataapi.models.Links;
import uk.gov.companieshouse.pscdataapi.models.NameElements;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.models.PscSensitiveData;
import uk.gov.companieshouse.pscdataapi.models.Updated;

public class TestHelper {

    public static FullRecordCompanyPSCApi buildFullRecordPsc(String kind) {
        FullRecordCompanyPSCApi output  = new FullRecordCompanyPSCApi();

        ExternalData externalData = new ExternalData();
        Data data = new Data();
        data.setName("forename");
        data.setCompanyNumber("companyNumber");
        data.setKind(kind);
        data.setLinks(List.of(new ItemLinkTypes()));
        data.setServiceAddress(new uk.gov.companieshouse.api.psc.Address());
        SensitiveData sensitiveData = new SensitiveData();
        sensitiveData.setDateOfBirth(new uk.gov.companieshouse.api.psc.DateOfBirth());
        externalData.setData(data);
        externalData.setSensitiveData(sensitiveData);
        externalData.setNotificationId("id");
        externalData.setCompanyNumber("companyNumber");
        externalData.setPscId("pscId");
        InternalData internalData = new InternalData();
        internalData.setDeltaAt(OffsetDateTime.parse("2022-01-12T00:00:00Z"));
        internalData.setCreatedAt("2022-01-12T00:00:00");
        internalData.setUpdatedAt(LocalDate.parse("2022-01-12"));

        if(kind.contains("individual")) {
            UsualResidentialAddress address = new UsualResidentialAddress();
            sensitiveData.setUsualResidentialAddress(address);
            data.setSurname("surname");
        } else if(kind.contains("secure")) {
            data.setCeasedOn(LocalDate.now());
        }

        output.setExternalData(externalData);
        output.setInternalData(internalData);
        return output;
    }

    public static PscDocument buildPscDocument(String kind) {
        PscDocument output = new PscDocument();

        PscData data = new PscData();
        data.setKind(kind);
        data.setName("forename");
        data.setLinks(new Links());
        data.setAddress(new Address());

        output.setNotificationId("id");
        output.setData(data);
        output.setPscId("pscId");
        output.setCompanyNumber("1234567");
        output.setDeltaAt("20220112000000000000");
        Updated updated = new Updated();
        updated.setAt(LocalDate.parse("2022-01-12"));
        output.setUpdated(updated);

        if(kind.contains("individual")) {
            PscSensitiveData sensitiveData = new PscSensitiveData();
            sensitiveData.setDateOfBirth(new DateOfBirth());
            sensitiveData.setUsualResidentialAddress(new Address());
            output.setSensitiveData(sensitiveData);
            NameElements nameElements = new NameElements();
            nameElements.setSurname("surname");
            data.setNameElements(nameElements);
        } else if(kind.contains("secure")) {
            data.setCeasedOn(LocalDate.now());
            data.setCeased(true);
        }

        return output;
    }
}
