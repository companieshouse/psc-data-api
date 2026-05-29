package uk.gov.companieshouse.pscdataapi.pscnotifications.mappers;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.psc_notifications.PscNotificationSummary;
import uk.gov.companieshouse.pscdataapi.models.NameElements;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.models.PscIdentification;
import uk.gov.companieshouse.pscdataapi.models.PscIdentityVerificationDetails;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ItemsMapperTest {

    private final AddressMapper addressMapper = new AddressMapper();
    private final IdentificationMapper idMapper = new IdentificationMapper();
    private final IdentityVerificationDetailsMapper ivdMapper = new IdentityVerificationDetailsMapper();
    private final NameElementsMapper nameElementsMapper = new NameElementsMapper();
    private final ItemsMapper mapper = new ItemsMapper(
            addressMapper, idMapper, ivdMapper, nameElementsMapper
    );

    @Test
    void testItemsMapperMapsItemsWhenGivenValidInput() {
        PscData data = new PscData();
        data.setCountryOfResidence("UK");
        data.setEtag("etag");
        data.setKind("individual-person-with-significant-control");
        data.setName("Steve Madden");
        data.setNationality("British");
        data.setNaturesOfControl(List.of("ownership-of-shares-25-to-50-percent"));
        data.setNotifiedOn(LocalDate.of(2024, 1, 15));
        data.setSanctioned(Boolean.FALSE);
        data.setIdentification(new PscIdentification());

        NameElements nameElements = new NameElements();
        nameElements.setForename("Steve");
        nameElements.setSurname("Madden");
        data.setNameElements(nameElements);

        data.setIdentityVerificationDetails(new PscIdentityVerificationDetails());

        PscDocument document = new PscDocument();
        document.setCompanyNumber("12345");
        document.setData(data);

        List<PscNotificationSummary> result = mapper.map(List.of(document));

        assertEquals(1, result.size());
        PscNotificationSummary summary = result.getFirst();

        assertEquals("UK", summary.getCountryOfResidence());
        assertEquals("etag", summary.getEtag());
        assertEquals(PscNotificationSummary.KindEnum.INDIVIDUAL_PERSON_WITH_SIGNIFICANT_CONTROL, summary.getKind());
        assertEquals("Steve Madden", summary.getName());
        assertEquals("British", summary.getNationality());
        assertEquals("2024-01-15", summary.getNotifiedOn());
        assertNotNull(summary.getNotifiedTo());
        assertEquals("12345", summary.getNotifiedTo().getCompanyNumber());

        assertNotNull(summary.getIdentification());
        assertNotNull(summary.getIdentityVerificationDetails());
        assertNotNull(summary.getNameElements());
    }

    @Test
    void testItemsMapperReturnsNullWhenGivenNullInput() {
        PscDocument document = new PscDocument();
        document.setCompanyNumber("12345");
        document.setData(null);

        List<PscNotificationSummary> result = mapper.map(List.of(document));

        assertEquals(1, result.size());
        PscNotificationSummary summary = result.getFirst();

        assertNull(summary.getName());
        assertNull(summary.getKind());
        assertNull(summary.getNotifiedTo());
        assertNull(summary.getNotifiedOn());
    }
}
