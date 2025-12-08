package uk.gov.companieshouse.pscdataapi.transform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.psc.CorporateEntity;
import uk.gov.companieshouse.api.psc.CorporateEntityBeneficialOwner;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.ExternalData;
import uk.gov.companieshouse.api.psc.IdentityVerificationDetails;
import uk.gov.companieshouse.api.psc.Individual;
import uk.gov.companieshouse.api.psc.IndividualBeneficialOwner;
import uk.gov.companieshouse.api.psc.LegalPerson;
import uk.gov.companieshouse.api.psc.LegalPersonBeneficialOwner;
import uk.gov.companieshouse.api.psc.ListSummary;
import uk.gov.companieshouse.api.psc.SuperSecure;
import uk.gov.companieshouse.api.psc.SuperSecureBeneficialOwner;
import uk.gov.companieshouse.pscdataapi.exceptions.FailedToTransformException;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.models.PscIdentityVerificationDetails;
import uk.gov.companieshouse.pscdataapi.models.PscSensitiveData;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

@ExtendWith(MockitoExtension.class)
class CompanyPscTransformerTest {

    private static final String NOTIFICATION_ID = "notificationId";
    private static final boolean SHOW_FULL_DOB_TRUE = true;
    private static final boolean SHOW_FULL_DOB_FALSE = false;

    @InjectMocks
    private CompanyPscTransformer pscTransformer;
    private FullRecordCompanyPSCApi fullRecordCompanyPSCApi;

    @Test
    void testInsertEmptyPscTransform() {
        PscDocument pscDocument = pscTransformer
                .transformPscOnInsert(null, new FullRecordCompanyPSCApi());
        Assertions.assertNotNull(pscDocument);
    }

    @Test
    void testInsertIndividualPscWithDateOfBirthIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(TestHelper.INDIVIDUAL_KIND, SHOW_FULL_DOB_TRUE, true);
        PscDocument expectedDocument = TestHelper.buildPscDocument(TestHelper.INDIVIDUAL_KIND, SHOW_FULL_DOB_TRUE, true);
        // when
        PscDocument result = pscTransformer.transformPscOnInsert(NOTIFICATION_ID, fullRecordCompanyPSCApi);
        // then
        Assertions.assertNotNull(result.getData());

        Assertions.assertNotNull(result.getData().getAddress());
        Assertions.assertNotNull(result.getSensitiveData().getDateOfBirth());
        Assertions.assertNotNull(result.getSensitiveData().getUsualResidentialAddress());
        Assertions.assertNotNull(result.getSensitiveData().getResidentialAddressIsSameAsServiceAddress());
        Assertions.assertNotNull(result.getData().getNameElements().getForename());
        Assertions.assertNotNull(result.getData().getName());
        Assertions.assertNotNull(result.getData().getNationality());
        Assertions.assertNotNull(result.getData().getCountryOfResidence());

        pscInsertAssertions(expectedDocument, result);
    }

    @Test
    void testInsertIndividualPscNoDateOfBirthIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(TestHelper.INDIVIDUAL_KIND, SHOW_FULL_DOB_FALSE, true);
        PscDocument expectedDocument = TestHelper.buildPscDocument(TestHelper.INDIVIDUAL_KIND, SHOW_FULL_DOB_FALSE, true);
        // when
        PscDocument result = pscTransformer.transformPscOnInsert(NOTIFICATION_ID, fullRecordCompanyPSCApi);
        // then
        Assertions.assertNotNull(result.getData());

        Assertions.assertNotNull(result.getData().getAddress());
        Assertions.assertNotNull(result.getSensitiveData().getDateOfBirth());
        Assertions.assertNotNull(result.getSensitiveData().getUsualResidentialAddress());
        Assertions.assertNotNull(result.getSensitiveData().getResidentialAddressIsSameAsServiceAddress());
        Assertions.assertNotNull(result.getData().getNameElements().getForename());
        Assertions.assertNotNull(result.getData().getName());
        Assertions.assertNotNull(result.getData().getNationality());
        Assertions.assertNotNull(result.getData().getCountryOfResidence());

        pscInsertAssertions(expectedDocument, result);
    }

    @Test
    void testInsertCorporateEntityPscIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(TestHelper.CORPORATE_KIND);
        PscDocument expectedDocument = TestHelper.buildPscDocument(TestHelper.CORPORATE_KIND);
        // when
        PscDocument result = pscTransformer.transformPscOnInsert(NOTIFICATION_ID, fullRecordCompanyPSCApi);
        // then
        Assertions.assertNotNull(result.getData());

        Assertions.assertNotNull(result.getData().getAddress());
        Assertions.assertNotNull(result.getData().getIdentification().getLegalForm());
        Assertions.assertNotNull(result.getData().getIdentification().getCountryRegistered());
        Assertions.assertNotNull(result.getData().getCeasedOn());

        pscInsertAssertions(expectedDocument, result);
    }

    @Test
    void testInsertLegalPersonPscIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(TestHelper.LEGAL_KIND);
        PscDocument expectedDocument = TestHelper.buildPscDocument(TestHelper.LEGAL_KIND);
        // when
        PscDocument result = pscTransformer.transformPscOnInsert(NOTIFICATION_ID, fullRecordCompanyPSCApi);
        // then
        Assertions.assertNotNull(result.getData());

        Assertions.assertNotNull(result.getData().getAddress());
        Assertions.assertNotNull(result.getData().getIdentification().getLegalForm());
        Assertions.assertNull(result.getData().getIdentification().getCountryRegistered());
        Assertions.assertNotNull(result.getData().getCeasedOn());

        pscInsertAssertions(expectedDocument, result);
    }

    @Test
    void testInsertSuperSecurePscIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(TestHelper.SECURE_KIND);
        PscDocument expectedDocument = TestHelper.buildPscDocument(TestHelper.SECURE_KIND);
        // when
        PscDocument result = pscTransformer.transformPscOnInsert(NOTIFICATION_ID, fullRecordCompanyPSCApi);
        // then
        Assertions.assertNotNull(result.getData());

        Assertions.assertTrue(result.getData().getCeased());
        Assertions.assertNotNull(result.getData().getCeasedOn());
        Assertions.assertNotNull(result.getData().getDescription());

        pscInsertAssertions(expectedDocument, result);
    }

    @Test
    void testInsertIndividualBeneficialOwnerPscIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(TestHelper.INDIVIDUAL_BO_KIND, SHOW_FULL_DOB_TRUE, true);
        PscDocument expectedDocument = TestHelper.buildPscDocument(TestHelper.INDIVIDUAL_BO_KIND, SHOW_FULL_DOB_TRUE, true);
        // when
        PscDocument result = pscTransformer.transformPscOnInsert(NOTIFICATION_ID, fullRecordCompanyPSCApi);
        // then
        Assertions.assertNotNull(result.getData());

        Assertions.assertTrue(result.getData().getSanctioned());
        Assertions.assertNotNull(result.getData().getAddress());
        Assertions.assertNotNull(result.getSensitiveData().getDateOfBirth());
        Assertions.assertNotNull(result.getSensitiveData().getUsualResidentialAddress());
        Assertions.assertNotNull(result.getSensitiveData().getResidentialAddressIsSameAsServiceAddress());
        Assertions.assertNotNull(result.getData().getNameElements().getForename());
        Assertions.assertNotNull(result.getData().getName());
        Assertions.assertNotNull(result.getData().getNationality());
        Assertions.assertNotNull(result.getData().getCountryOfResidence());
        Assertions.assertNotNull(result.getData().getCeasedOn());

        pscInsertAssertions(expectedDocument, result);
    }

    @Test
    void testInsertIndividualBeneficialOwnerPscIsTransformedSuccessfullyNoPscStatements() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(TestHelper.INDIVIDUAL_BO_KIND, SHOW_FULL_DOB_TRUE, false);
        PscDocument expectedDocument = TestHelper.buildPscDocument(TestHelper.INDIVIDUAL_BO_KIND, SHOW_FULL_DOB_TRUE, false);
        // when
        PscDocument result = pscTransformer.transformPscOnInsert(NOTIFICATION_ID, fullRecordCompanyPSCApi);
        // then
        Assertions.assertNotNull(result.getData());

        Assertions.assertTrue(result.getData().getSanctioned());
        Assertions.assertNotNull(result.getData().getAddress());
        Assertions.assertNotNull(result.getSensitiveData().getDateOfBirth());
        Assertions.assertNotNull(result.getSensitiveData().getUsualResidentialAddress());
        Assertions.assertNotNull(result.getSensitiveData().getResidentialAddressIsSameAsServiceAddress());
        Assertions.assertNotNull(result.getData().getNameElements().getForename());
        Assertions.assertNotNull(result.getData().getName());
        Assertions.assertNotNull(result.getData().getNationality());
        Assertions.assertNotNull(result.getData().getCountryOfResidence());
        Assertions.assertNull(result.getData().getLinks().getStatement());
        Assertions.assertNotNull(result.getData().getCeasedOn());
        pscInsertAssertions(expectedDocument, result);
    }


    @Test
    void testInsertCorporateEntityBeneficialOwnerPscIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(TestHelper.CORPORATE_BO_KIND);
        PscDocument expectedDocument = TestHelper.buildPscDocument(TestHelper.CORPORATE_BO_KIND);
        // when
        PscDocument result = pscTransformer.transformPscOnInsert(NOTIFICATION_ID, fullRecordCompanyPSCApi);
        // then
        Assertions.assertNotNull(result.getData());

        Assertions.assertTrue(result.getData().getSanctioned());
        Assertions.assertNotNull(result.getData().getAddress());
        Assertions.assertNotNull(result.getData().getIdentification().getLegalForm());
        Assertions.assertNotNull(result.getData().getIdentification().getCountryRegistered());
        Assertions.assertNotNull(result.getData().getPrincipalOfficeAddress());
        Assertions.assertNotNull(result.getData().getCeasedOn());

        pscInsertAssertions(expectedDocument, result);
    }

    @Test
    void testInsertLegalPersonBeneficialOwnerPscIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(TestHelper.LEGAL_BO_KIND);
        PscDocument expectedDocument = TestHelper.buildPscDocument(TestHelper.LEGAL_BO_KIND);
        // when
        PscDocument result = pscTransformer.transformPscOnInsert(NOTIFICATION_ID, fullRecordCompanyPSCApi);
        // then
        Assertions.assertNotNull(result.getData());

        Assertions.assertTrue(result.getData().getSanctioned());
        Assertions.assertNotNull(result.getData().getAddress());
        Assertions.assertNotNull(result.getData().getIdentification().getLegalForm());
        Assertions.assertNull(result.getData().getIdentification().getCountryRegistered());
        Assertions.assertNotNull(result.getData().getPrincipalOfficeAddress());
        Assertions.assertNotNull(result.getData().getCeasedOn());

        pscInsertAssertions(expectedDocument, result);
    }

    @Test
    void testInsertSuperSecureBeneficialOwnerPscIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(TestHelper.SECURE_BO_KIND);
        PscDocument expectedDocument = TestHelper.buildPscDocument(TestHelper.SECURE_BO_KIND);
        // when
        PscDocument result = pscTransformer.transformPscOnInsert(NOTIFICATION_ID, fullRecordCompanyPSCApi);
        // then
        Assertions.assertNotNull(result.getData());

        Assertions.assertTrue(result.getData().getSanctioned());
        Assertions.assertTrue(result.getData().getCeased());
        Assertions.assertNotNull(result.getData().getCeasedOn());
        Assertions.assertNotNull(result.getData().getDescription());

        pscInsertAssertions(expectedDocument, result);
    }

    private static void pscInsertAssertions(PscDocument expectedDocument, PscDocument result) {
        assertThat(result.getData().getNameElements(), is(expectedDocument.getData().getNameElements()));
        assertThat(result.getData(), is(expectedDocument.getData()));

        assertThat(result.getSensitiveData(), is(expectedDocument.getSensitiveData()));
        assertThat(result.getData().getIdentification(), is(expectedDocument.getData().getIdentification()));

        assertThat(result.getDeltaAt(), is(expectedDocument.getDeltaAt()));
        assertThat(result.getUpdatedBy(), is(expectedDocument.getUpdatedBy()));
        assertThat(result.getUpdated().getAt(), instanceOf(LocalDateTime.class));

        result.setUpdated(expectedDocument.getUpdated()); // Updated objects would be different objects
        assertThat(result, is(expectedDocument));
    }

    @Test
    void doNotSetSensitiveDataWhenSensitiveDataIsNull() {
        PscDocument pscDocument = new PscDocument();
        ExternalData externalData = new ExternalData();
        externalData.setSensitiveData(null);

        pscTransformer.setSensitiveDataIfPresent(pscDocument, externalData);

        Assertions.assertNull(pscDocument.getSensitiveData());
    }

    @Test
    void transformPscOnInsertWithExternalDataButNullData() {
        FullRecordCompanyPSCApi requestBody = new FullRecordCompanyPSCApi();
        ExternalData externalData = new ExternalData();
        externalData.setData(null);
        externalData.setPscId("pscId123");
        externalData.setCompanyNumber("12345678");
        requestBody.setExternalData(externalData);
        String notificationId = "notif-001";

        PscDocument result = pscTransformer.transformPscOnInsert(notificationId, requestBody);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("notif-001", result.getId());
        Assertions.assertEquals("notif-001", result.getNotificationId());
        Assertions.assertEquals("pscId123", result.getPscId());
        Assertions.assertEquals("12345678", result.getCompanyNumber());
        Assertions.assertNull(result.getData());
    }

    @Test
    void testBasicPscDocumentWithNullDataTransform() {
        PscDocument pscDocument = new PscDocument();
        pscDocument.setData(new PscData());
        pscDocument.setDeltaAt("20230102030405000000");
        pscDocument.setSensitiveData(new PscSensitiveData());

        Individual individual = pscTransformer.transformPscDocToIndividual(pscDocument, SHOW_FULL_DOB_TRUE);

        Assertions.assertNotNull(individual);
    }

    @Test
    void testEmptyPscIndividualTransform() {
        Individual individual = pscTransformer
                .transformPscDocToIndividual(new PscDocument(), SHOW_FULL_DOB_TRUE);
        Assertions.assertNotNull(individual);
    }

    @Test
    void testPscDocumentWithIdentityVerificationDetailsIndividualTransform() {
        // Given
        PscDocument pscDocument = TestHelper.buildPscDocument("individual");
        // When
        Individual individual = pscTransformer
                .transformPscDocToIndividual(pscDocument, SHOW_FULL_DOB_TRUE);
        // Then
        Assertions.assertNotNull(individual);
        Assertions.assertNotNull(individual.getIdentityVerificationDetails());
        Assertions.assertInstanceOf(IdentityVerificationDetails.class, individual.getIdentityVerificationDetails());
    }

    @Test
    void testPscDocumentShouldNotSetIdentityVerificationDetailsWhenEmpty() {
        // Arrange
        PscDocument pscDocument = TestHelper.buildPscDocument("individual");
        PscData pscData = new PscData();
        PscIdentityVerificationDetails emptyIdv = new PscIdentityVerificationDetails();
        pscData.setIdentityVerificationDetails(emptyIdv);
        pscDocument.setData(pscData);
        // Act
        Individual result = pscTransformer.transformPscDocToIndividual(pscDocument, false);
        // Assert
        Assertions.assertNull(result.getIdentityVerificationDetails());
    }

    @Test
    void testPscDocumentShouldSetIdentityVerificationDetailsWhenPopulated() {
        // Arrange
        PscDocument pscDocument = TestHelper.buildPscDocument("individual");
        PscData pscData = new PscData();
        PscIdentityVerificationDetails idv = new PscIdentityVerificationDetails();
        idv.setIdentityVerifiedOn(LocalDate.now());
        pscData.setIdentityVerificationDetails(idv);
        pscDocument.setData(pscData);
        // Act
        Individual result = pscTransformer.transformPscDocToIndividual(pscDocument, false);
        // Assert
        Assertions.assertNotNull(result.getIdentityVerificationDetails());
    }

    @Test
    void testIdvDatesPresentInSuperSecureWhenExistsInPscDocument() {
        // Arrange
        var pscDocument = TestHelper.buildPscDocument("individual");
        var pscData = new PscData();

        PscIdentityVerificationDetails idv = new PscIdentityVerificationDetails();
        idv.setIdentityVerifiedOn(LocalDate.now());
        var startOnDate = LocalDate.of(2023, 1, 1);
        var endOnDate = LocalDate.of(2023, 1, 1);
        idv.setAppointmentVerificationStartOn(startOnDate);
        idv.setAppointmentVerificationEndOn(endOnDate);
        pscData.setIdentityVerificationDetails(idv);
        pscDocument.setData(pscData);
        // Act
        Individual result = pscTransformer.transformPscDocToIndividual(pscDocument, false);
        var resultStartOnDate = result.getIdentityVerificationDetails().getAppointmentVerificationStartOn();
        var resultEndOnDate = result.getIdentityVerificationDetails().getAppointmentVerificationEndOn();
        // Assert
        Assertions.assertNotNull(result.getIdentityVerificationDetails());
        Assertions.assertEquals(startOnDate, resultStartOnDate);
        Assertions.assertEquals(endOnDate, resultEndOnDate);
    }

    @Test
    void testAppointmentVerificationStartOnInSuperSecureWhenExistsInPscDocument() {
        // Arrange
        var pscDocument = TestHelper.buildPscDocument("individual");
        var pscData = new PscData();

        PscIdentityVerificationDetails idv = new PscIdentityVerificationDetails();
        idv.setIdentityVerifiedOn(LocalDate.now());
        var startOnDate = LocalDate.of(2023, 1, 1);
        idv.setAppointmentVerificationStartOn(startOnDate);
        pscData.setIdentityVerificationDetails(idv);
        pscDocument.setData(pscData);
        // Act
        Individual result = pscTransformer.transformPscDocToIndividual(pscDocument, false);
        var resultStartOnDate = result.getIdentityVerificationDetails().getAppointmentVerificationStartOn();
        // Assert
        Assertions.assertNotNull(result.getIdentityVerificationDetails());
        Assertions.assertEquals(startOnDate, resultStartOnDate);
        Assertions.assertNull(result.getIdentityVerificationDetails().getAppointmentVerificationEndOn());
    }

    @Test
    void testAppointmentVerificationEndOnInSuperSecureWhenExistsInPscDocument() {
        // Arrange
        var pscDocument = TestHelper.buildPscDocument("individual");
        var pscData = new PscData();

        PscIdentityVerificationDetails idv = new PscIdentityVerificationDetails();
        idv.setIdentityVerifiedOn(LocalDate.now());
        var endOnDate = LocalDate.of(2023, 1, 1);
        idv.setAppointmentVerificationEndOn(endOnDate);
        pscData.setIdentityVerificationDetails(idv);
        pscDocument.setData(pscData);
        // Act
        Individual result = pscTransformer.transformPscDocToIndividual(pscDocument, false);
        var resultEndOnDate = result.getIdentityVerificationDetails().getAppointmentVerificationEndOn();
        // Assert
        Assertions.assertNotNull(result.getIdentityVerificationDetails());
        Assertions.assertEquals(endOnDate, resultEndOnDate);
        Assertions.assertNull(result.getIdentityVerificationDetails().getAppointmentVerificationStartOn());
    }

    @Test
    void testEmptyPscIndividualBeneficialOwnerTransform() {
        IndividualBeneficialOwner individualBeneficialOwner = pscTransformer
                .transformPscDocToIndividualBeneficialOwner(new PscDocument(), SHOW_FULL_DOB_TRUE);
        Assertions.assertNotNull(individualBeneficialOwner);
    }

    @Test
    void transformPscDocToIndividualBeneficialOwnerWithNullData() {
        PscDocument pscDocument = new PscDocument();

        IndividualBeneficialOwner result = pscTransformer.transformPscDocToIndividualBeneficialOwner(pscDocument, true);

        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getName());
        Assertions.assertNull(result.getDateOfBirth());
        Assertions.assertNull(result.getIsSanctioned());
    }

    @Test
    void transformPscDocToIndividualBeneficialOwnerWithNullSensitiveData() {
        PscDocument pscDocument = TestHelper.buildPscDocument(TestHelper.INDIVIDUAL_BO_KIND);
        pscDocument.setSensitiveData(null);

        IndividualBeneficialOwner result = pscTransformer.transformPscDocToIndividualBeneficialOwner(pscDocument, true);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(pscDocument.getData().getName(), result.getName());
        Assertions.assertNull(result.getDateOfBirth());
    }

    @Test
    void transformPscDocToIndividualBeneficialOwnerWithEmptySensitiveData() {
        PscDocument pscDocument = TestHelper.buildPscDocument(TestHelper.INDIVIDUAL_BO_KIND);
        pscDocument.setSensitiveData(new PscSensitiveData());

        IndividualBeneficialOwner result = pscTransformer.transformPscDocToIndividualBeneficialOwner(pscDocument, true);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(pscDocument.getData().getName(), result.getName());
        Assertions.assertNull(result.getDateOfBirth());
    }

    @Test
    void testEmptyPscCorporateEntityTransform() {
        CorporateEntity corporateEntity = pscTransformer
                .transformPscDocToCorporateEntity(new PscDocument());
        Assertions.assertNotNull(corporateEntity);
    }

    @Test
    void testEmptyPscCorporateEntityBeneficialOwnerTransform() {
        CorporateEntityBeneficialOwner corporateEntityBeneficialOwner = pscTransformer
                .transformPscDocToCorporateEntityBeneficialOwner(new PscDocument());
        Assertions.assertNotNull(corporateEntityBeneficialOwner);
    }

    @Test
    void transformCorporateEntityBeneficialOwnerWithValidData() {
        PscDocument pscDocument = TestHelper.buildPscDocument(TestHelper.CORPORATE_BO_KIND);

        CorporateEntityBeneficialOwner result = pscTransformer.transformPscDocToCorporateEntityBeneficialOwner(pscDocument);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(pscDocument.getData().getEtag(), result.getEtag());
        Assertions.assertEquals(pscDocument.getData().getName(), result.getName());
        Assertions.assertEquals(pscDocument.getData().getAddress().getAddressLine1(), result.getAddress().getAddressLine1());
        Assertions.assertEquals(pscDocument.getData().getNaturesOfControl(), result.getNaturesOfControl());
        Assertions.assertEquals(pscDocument.getData().getLinks(), result.getLinks());
        Assertions.assertEquals(pscDocument.getData().getSanctioned(), result.getIsSanctioned());
        Assertions.assertEquals(pscDocument.getData().getIdentification().getLegalForm(), result.getIdentification().getLegalForm());
        Assertions.assertEquals(pscDocument.getData().getPrincipalOfficeAddress().getAddressLine1(), result.getPrincipalOfficeAddress().getAddressLine1());
        Assertions.assertEquals(pscDocument.getData().getCeasedOn(), result.getCeasedOn());
    }

    @Test
    void transformCorporateEntityBeneficialOwnerWithNullData() {
        PscDocument pscDocument = new PscDocument();

        CorporateEntityBeneficialOwner result = pscTransformer.transformPscDocToCorporateEntityBeneficialOwner(pscDocument);

        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getEtag());
        Assertions.assertNull(result.getName());
        Assertions.assertNull(result.getAddress());
        Assertions.assertNull(result.getNaturesOfControl());
        Assertions.assertNull(result.getLinks());
        Assertions.assertNull(result.getIsSanctioned());
        Assertions.assertNull(result.getIdentification());
        Assertions.assertNull(result.getPrincipalOfficeAddress());
        Assertions.assertNull(result.getCeasedOn());
    }

    @Test
    void transformCorporateEntityBeneficialOwnerWithEmptyData() {
        PscDocument pscDocument = new PscDocument();
        pscDocument.setData(new PscData());

        CorporateEntityBeneficialOwner result = pscTransformer.transformPscDocToCorporateEntityBeneficialOwner(pscDocument);

        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getEtag());
        Assertions.assertNull(result.getName());
        Assertions.assertNull(result.getAddress());
        Assertions.assertNull(result.getNaturesOfControl());
        Assertions.assertNull(result.getLinks());
        Assertions.assertNull(result.getIsSanctioned());
        Assertions.assertNull(result.getIdentification());
        Assertions.assertNull(result.getPrincipalOfficeAddress());
        Assertions.assertNull(result.getCeasedOn());
    }

    @Test
    void transformCorporateEntityBeneficialOwnerWithPartialData() {
        PscDocument pscDocument = new PscDocument();
        PscData pscData = new PscData();
        pscData.setEtag("etag123");
        pscData.setName("Test Corporate Entity Beneficial Owner");
        pscDocument.setData(pscData);

        CorporateEntityBeneficialOwner result = pscTransformer.transformPscDocToCorporateEntityBeneficialOwner(pscDocument);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("etag123", result.getEtag());
        Assertions.assertEquals("Test Corporate Entity Beneficial Owner", result.getName());
        Assertions.assertNull(result.getAddress());
        Assertions.assertNull(result.getNaturesOfControl());
        Assertions.assertNull(result.getLinks());
        Assertions.assertNull(result.getIsSanctioned());
        Assertions.assertNull(result.getIdentification());
        Assertions.assertNull(result.getPrincipalOfficeAddress());
        Assertions.assertNull(result.getCeasedOn());
    }

    @Test
    void transformCorporateEntityWithValidData() {
        PscDocument pscDocument = TestHelper.buildPscDocument(TestHelper.CORPORATE_KIND);

        CorporateEntity result = pscTransformer.transformPscDocToCorporateEntity(pscDocument);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(pscDocument.getData().getEtag(), result.getEtag());
        Assertions.assertEquals(pscDocument.getData().getName(), result.getName());
        Assertions.assertEquals(pscDocument.getData().getCeasedOn(), result.getCeasedOn());
        Assertions.assertEquals(pscDocument.getData().getNotifiedOn(), result.getNotifiedOn());
        Assertions.assertEquals(pscDocument.getData().getLinks(), result.getLinks());
        Assertions.assertEquals(pscDocument.getData().getNaturesOfControl(), result.getNaturesOfControl());
        Assertions.assertNotNull(result.getAddress());
        Assertions.assertNotNull(result.getIdentification());
    }

    @Test
    void transformCorporateEntityWithNullData() {
        PscDocument pscDocument = new PscDocument();

        CorporateEntity result = pscTransformer.transformPscDocToCorporateEntity(pscDocument);

        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getEtag());
        Assertions.assertNull(result.getName());
        Assertions.assertNull(result.getCeasedOn());
        Assertions.assertNull(result.getNotifiedOn());
        Assertions.assertNull(result.getLinks());
        Assertions.assertNull(result.getAddress());
        Assertions.assertNull(result.getIdentification());
    }

    @Test
    void transformCorporateEntityWithEmptyData() {
        PscDocument pscDocument = new PscDocument();
        pscDocument.setData(new PscData());

        CorporateEntity result = pscTransformer.transformPscDocToCorporateEntity(pscDocument);

        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getEtag());
        Assertions.assertNull(result.getName());
        Assertions.assertNull(result.getCeasedOn());
        Assertions.assertNull(result.getNotifiedOn());
        Assertions.assertNull(result.getLinks());
        Assertions.assertNull(result.getNaturesOfControl());
        Assertions.assertNull(result.getAddress());
        Assertions.assertNull(result.getIdentification());
    }

    @Test
    void transformCorporateEntityWithPartialData() {
        PscDocument pscDocument = new PscDocument();
        PscData pscData = new PscData();
        pscData.setEtag("etag123");
        pscData.setName("Test Corporate Entity");
        pscDocument.setData(pscData);

        CorporateEntity result = pscTransformer.transformPscDocToCorporateEntity(pscDocument);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("etag123", result.getEtag());
        Assertions.assertEquals("Test Corporate Entity", result.getName());
        Assertions.assertNull(result.getCeasedOn());
        Assertions.assertNull(result.getNotifiedOn());
        Assertions.assertNull(result.getLinks());
        Assertions.assertNull(result.getNaturesOfControl());
        Assertions.assertNull(result.getAddress());
        Assertions.assertNull(result.getIdentification());
    }

    @Test
    void testEmptyPscLegalPersonTransform() {
        LegalPerson legalPerson = pscTransformer
                .transformPscDocToLegalPerson(new PscDocument());
        Assertions.assertNotNull(legalPerson);
    }

    @Test
    void testEmptyPscLegalPersonBeneficialOwnerTransform() {
        LegalPersonBeneficialOwner legalPersonBeneficialOwner = pscTransformer
                .transformPscDocToLegalPersonBeneficialOwner(new PscDocument());
        Assertions.assertNotNull(legalPersonBeneficialOwner);
    }

    @Test
    void transformLegalPersonBeneficialOwnerWithValidData() {
        PscDocument pscDocument = TestHelper.buildPscDocument(TestHelper.LEGAL_BO_KIND);

        LegalPersonBeneficialOwner result = pscTransformer.transformPscDocToLegalPersonBeneficialOwner(pscDocument);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(pscDocument.getData().getEtag(), result.getEtag());
        Assertions.assertEquals(pscDocument.getData().getName(), result.getName());
        Assertions.assertEquals(pscDocument.getData().getAddress().getAddressLine1(), result.getAddress().getAddressLine1());
        Assertions.assertEquals(pscDocument.getData().getNaturesOfControl(), result.getNaturesOfControl());
        Assertions.assertEquals(pscDocument.getData().getLinks(), result.getLinks());
        Assertions.assertEquals(pscDocument.getData().getCeasedOn(), result.getCeasedOn());
        Assertions.assertEquals(pscDocument.getData().getNotifiedOn(), result.getNotifiedOn());
        Assertions.assertEquals(pscDocument.getData().getSanctioned(), result.getIsSanctioned());
        Assertions.assertEquals(pscDocument.getData().getIdentification().getLegalForm(), result.getIdentification().getLegalForm());
        Assertions.assertEquals(pscDocument.getData().getPrincipalOfficeAddress().getAddressLine1(), result.getPrincipalOfficeAddress().getAddressLine1());
    }

    @Test
    void transformLegalPersonBeneficialOwnerWithNullData() {
        PscDocument pscDocument = new PscDocument();

        LegalPersonBeneficialOwner result = pscTransformer.transformPscDocToLegalPersonBeneficialOwner(pscDocument);

        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getEtag());
        Assertions.assertNull(result.getName());
        Assertions.assertNull(result.getAddress());
        Assertions.assertNull(result.getNaturesOfControl());
        Assertions.assertNull(result.getLinks());
        Assertions.assertNull(result.getCeasedOn());
        Assertions.assertNull(result.getNotifiedOn());
        Assertions.assertNull(result.getIsSanctioned());
        Assertions.assertNull(result.getIdentification());
        Assertions.assertNull(result.getPrincipalOfficeAddress());
    }

    @Test
    void transformLegalPersonBeneficialOwnerWithEmptyData() {
        PscDocument pscDocument = new PscDocument();
        pscDocument.setData(new PscData());

        LegalPersonBeneficialOwner result = pscTransformer.transformPscDocToLegalPersonBeneficialOwner(pscDocument);

        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getEtag());
        Assertions.assertNull(result.getName());
        Assertions.assertNull(result.getAddress());
        Assertions.assertNull(result.getNaturesOfControl());
        Assertions.assertNull(result.getLinks());
        Assertions.assertNull(result.getCeasedOn());
        Assertions.assertNull(result.getNotifiedOn());
        Assertions.assertNull(result.getIsSanctioned());
        Assertions.assertNull(result.getIdentification());
        Assertions.assertNull(result.getPrincipalOfficeAddress());
    }

    @Test
    void transformLegalPersonBeneficialOwnerWithPartialData() {
        PscDocument pscDocument = new PscDocument();
        PscData pscData = new PscData();
        pscData.setEtag("etag123");
        pscData.setName("Test Legal Person Beneficial Owner");
        pscDocument.setData(pscData);

        LegalPersonBeneficialOwner result = pscTransformer.transformPscDocToLegalPersonBeneficialOwner(pscDocument);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("etag123", result.getEtag());
        Assertions.assertEquals("Test Legal Person Beneficial Owner", result.getName());
        Assertions.assertNull(result.getAddress());
        Assertions.assertNull(result.getNaturesOfControl());
        Assertions.assertNull(result.getLinks());
        Assertions.assertNull(result.getCeasedOn());
        Assertions.assertNull(result.getNotifiedOn());
        Assertions.assertNull(result.getIsSanctioned());
        Assertions.assertNull(result.getIdentification());
        Assertions.assertNull(result.getPrincipalOfficeAddress());
    }

    @Test
    void transformLegalPersonWithValidData() {
        PscDocument pscDocument = TestHelper.buildPscDocument(TestHelper.LEGAL_KIND);

        LegalPerson result = pscTransformer.transformPscDocToLegalPerson(pscDocument);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(pscDocument.getData().getEtag(), result.getEtag());
        Assertions.assertEquals(pscDocument.getData().getName(), result.getName());
        Assertions.assertEquals(pscDocument.getData().getAddress().getAddressLine1(), result.getAddress().getAddressLine1());
        Assertions.assertEquals(pscDocument.getData().getNaturesOfControl(), result.getNaturesOfControl());
        Assertions.assertEquals(pscDocument.getData().getLinks(), result.getLinks());
        Assertions.assertEquals(pscDocument.getData().getCeasedOn(), result.getCeasedOn());
        Assertions.assertEquals(pscDocument.getData().getNotifiedOn(), result.getNotifiedOn());
        Assertions.assertEquals(pscDocument.getData().getIdentification().getLegalForm(), result.getIdentification().getLegalForm());
    }

    @Test
    void transformLegalPersonWithNullData() {
        PscDocument pscDocument = new PscDocument();

        LegalPerson result = pscTransformer.transformPscDocToLegalPerson(pscDocument);

        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getEtag());
        Assertions.assertNull(result.getName());
        Assertions.assertNull(result.getAddress());
        Assertions.assertNull(result.getLinks());
        Assertions.assertNull(result.getCeasedOn());
        Assertions.assertNull(result.getNotifiedOn());
        Assertions.assertNull(result.getIdentification());
    }

    @Test
    void transformLegalPersonWithEmptyData() {
        PscDocument pscDocument = new PscDocument();
        pscDocument.setData(new PscData());

        LegalPerson result = pscTransformer.transformPscDocToLegalPerson(pscDocument);

        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getEtag());
        Assertions.assertNull(result.getName());
        Assertions.assertNull(result.getAddress());
        Assertions.assertNull(result.getNaturesOfControl());
        Assertions.assertNull(result.getLinks());
        Assertions.assertNull(result.getCeasedOn());
        Assertions.assertNull(result.getNotifiedOn());
        Assertions.assertNull(result.getIdentification());
    }

    @Test
    void transformLegalPersonWithPartialData() {
        PscDocument pscDocument = new PscDocument();
        PscData pscData = new PscData();
        pscData.setEtag("etag123");
        pscData.setName("Test Legal Person");
        pscDocument.setData(pscData);

        LegalPerson result = pscTransformer.transformPscDocToLegalPerson(pscDocument);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("etag123", result.getEtag());
        Assertions.assertEquals("Test Legal Person", result.getName());
        Assertions.assertNull(result.getAddress());
        Assertions.assertNull(result.getNaturesOfControl());
        Assertions.assertNull(result.getLinks());
        Assertions.assertNull(result.getCeasedOn());
        Assertions.assertNull(result.getNotifiedOn());
        Assertions.assertNull(result.getIdentification());
    }

    @Test
    void testEmptyPscSuperSecureTransform() {
        SuperSecure superSecure = pscTransformer
                .transformPscDocToSuperSecure(new PscDocument());
        Assertions.assertNotNull(superSecure);
    }

    @Test
    void testEmptyPscSuperSecureBeneficialOwnerTransform() {
        SuperSecureBeneficialOwner superSecureBeneficialOwner = pscTransformer
                .transformPscDocToSuperSecureBeneficialOwner(new PscDocument());
        Assertions.assertNotNull(superSecureBeneficialOwner);
    }

    @Test
    void testEmptyListSummaryTransform() {
        ListSummary listSummary = pscTransformer
                .transformPscDocToListSummary(new PscDocument());
        Assertions.assertNotNull(listSummary);
    }

    @Test
    void testBasicListSummaryWithNullDataTransform() {
        PscDocument pscDocument = new PscDocument();
        PscData pscData = new PscData();
        pscData.setKind("individual-person-with-significant-control");
        pscDocument.setData(pscData);
        pscDocument.setDeltaAt("20230102030405000000");
        pscDocument.setSensitiveData(new PscSensitiveData());

        ListSummary listSummary = pscTransformer
                .transformPscDocToListSummary(pscDocument);
        Assertions.assertNotNull(listSummary);
    }

    @Test
    void testListSummaryTransform() {
        PscDocument pscDocument = TestHelper.buildPscDocument(TestHelper.CORPORATE_BO_KIND);

        ListSummary listSummary = pscTransformer
                .transformPscDocToListSummary(pscDocument);
        Assertions.assertNotNull(listSummary);
        Assertions.assertEquals(pscDocument.getData().getAddress().getAddressLine1(),
                listSummary.getAddress().getAddressLine1());
        Assertions.assertEquals(pscDocument.getData().getPrincipalOfficeAddress().getAddressLine1(),
                listSummary.getPrincipalOfficeAddress().getAddressLine1());

    }
}
