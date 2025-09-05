package uk.gov.companieshouse.pscdataapi.transform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.psc.CorporateEntity;
import uk.gov.companieshouse.api.psc.CorporateEntityBeneficialOwner;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
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
        assertThat(result.getUpdated().getAt(), is(expectedDocument.getUpdated().getAt()));

        result.setUpdated(expectedDocument.getUpdated()); // Updated objects would be different objects
        assertThat(result, is(expectedDocument));
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
    }

    @Test
    void testEmptyPscIndividualBeneficialOwnerTransform() {
        IndividualBeneficialOwner individualBeneficialOwner = pscTransformer
                .transformPscDocToIndividualBeneficialOwner(new PscDocument(), SHOW_FULL_DOB_TRUE);
        Assertions.assertNotNull(individualBeneficialOwner);
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
