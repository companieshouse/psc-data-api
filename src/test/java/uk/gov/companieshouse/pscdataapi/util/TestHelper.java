package uk.gov.companieshouse.pscdataapi.util;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import uk.gov.companieshouse.api.psc.Data;
import uk.gov.companieshouse.api.psc.DateOfBirth;
import uk.gov.companieshouse.api.psc.ExternalData;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.InternalData;
import uk.gov.companieshouse.api.psc.SensitiveData;
import uk.gov.companieshouse.pscdataapi.models.NameElements;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.models.Updated;

public class TestHelper {

    public static FullRecordCompanyPSCApi buildFullRecordPsc(String kind) {
        FullRecordCompanyPSCApi output  = new FullRecordCompanyPSCApi();

        ExternalData externalData = new ExternalData();
        Data data = new Data();
        data.setName("forename");
        data.setCompanyNumber("companyNumber");
        data.setKind(kind);
        SensitiveData sensitiveData = new SensitiveData();
        sensitiveData.setDateOfBirth(new DateOfBirth());
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
        SensitiveData sensitiveData = new SensitiveData();
        sensitiveData.setDateOfBirth(new DateOfBirth());

        output.setNotificationId("id");
        output.setData(data);
        output.setSensitiveData(sensitiveData);
        output.setPscId("pscId");
        output.setCompanyNumber("1234567");
        output.setDeltaAt("20220112000000000000");
        Updated updated = new Updated();
        updated.setAt(LocalDate.parse("2022-01-12"));
        output.setUpdated(updated);

        if(kind.contains("individual")) {
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
