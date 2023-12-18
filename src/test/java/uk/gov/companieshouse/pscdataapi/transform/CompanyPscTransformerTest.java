package uk.gov.companieshouse.pscdataapi.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.psc.*;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscdataapi.exceptions.FailedToTransformException;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.models.PscSensitiveData;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class CompanyPscTransformerTest {

    @Mock
    private Logger logger;

    private static final String NOTIFICATION_ID = "notificationId";

    private static final String INDIVIDUAL_KIND = "individual-person-with-significant-control";
    private static final String CORPORATE_KIND = "corporate-entity-person-with-significant-control";
    public static final String LEGAL_KIND = "legal-person-person-with-significant-control";
    private static final String SECURE_KIND = "super-secure-person-with-significant-control";
    public static final String INDIVIDUAL_BO_KIND = "individual-beneficial-owner";
    public static final String CORPORATE_BO_KIND = "corporate-entity-beneficial-owner";
    public static final String LEGAL_BO_KIND = "legal-person-beneficial-owner";
    public static final String SECURE_BO_KIND = "super-secure-beneficial-owner";

    private static final boolean SHOW_FULL_DOB_TRUE = true;
    private static final boolean SHOW_FULL_DOB_FALSE = false;

    @InjectMocks
    private CompanyPscTransformer pscTransformer;
    private FullRecordCompanyPSCApi fullRecordCompanyPSCApi;

    @Test
    void testInsertEmptyPscTransform(){
        PscDocument pscDocument = pscTransformer
                .transformPscOnInsert(null, new FullRecordCompanyPSCApi());
        Assertions.assertNotNull(pscDocument);
    }

    @Test
    void testInsertIndividualPscWithDateOfBirthIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(INDIVIDUAL_KIND, SHOW_FULL_DOB_TRUE);
        PscDocument expectedDocument = TestHelper.buildPscDocument(INDIVIDUAL_KIND, SHOW_FULL_DOB_TRUE);
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
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(INDIVIDUAL_KIND, SHOW_FULL_DOB_FALSE);
        PscDocument expectedDocument = TestHelper.buildPscDocument(INDIVIDUAL_KIND, SHOW_FULL_DOB_FALSE);
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
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(CORPORATE_KIND);
        PscDocument expectedDocument = TestHelper.buildPscDocument(CORPORATE_KIND);
        // when
        PscDocument result = pscTransformer.transformPscOnInsert(NOTIFICATION_ID, fullRecordCompanyPSCApi);
        // then
        Assertions.assertNotNull(result.getData());

        Assertions.assertNotNull(result.getData().getAddress());
        Assertions.assertNotNull(result.getIdentification().getLegalForm());
        Assertions.assertNotNull(result.getIdentification().getCountryRegistered());

        pscInsertAssertions(expectedDocument, result);
    }

    @Test
    void testInsertLegalPersonPscIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(LEGAL_KIND);
        PscDocument expectedDocument = TestHelper.buildPscDocument(LEGAL_KIND);
        // when
        PscDocument result = pscTransformer.transformPscOnInsert(NOTIFICATION_ID, fullRecordCompanyPSCApi);
        // then
        Assertions.assertNotNull(result.getData());

        Assertions.assertNotNull(result.getData().getAddress());
        Assertions.assertNotNull(result.getIdentification().getLegalForm());
        Assertions.assertNull(result.getIdentification().getCountryRegistered());

        pscInsertAssertions(expectedDocument, result);
    }

    @Test
    void testInsertSuperSecurePscIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(SECURE_KIND);
        PscDocument expectedDocument = TestHelper.buildPscDocument(SECURE_KIND);
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
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(INDIVIDUAL_BO_KIND, SHOW_FULL_DOB_TRUE);
        PscDocument expectedDocument = TestHelper.buildPscDocument(INDIVIDUAL_BO_KIND, SHOW_FULL_DOB_TRUE);
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

        pscInsertAssertions(expectedDocument, result);
    }

    @Test
    void testInsertCorporateEntityBeneficialOwnerPscIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(CORPORATE_BO_KIND);
        PscDocument expectedDocument = TestHelper.buildPscDocument(CORPORATE_BO_KIND);
        // when
        PscDocument result = pscTransformer.transformPscOnInsert(NOTIFICATION_ID, fullRecordCompanyPSCApi);
        // then
        Assertions.assertNotNull(result.getData());

        Assertions.assertTrue(result.getData().getSanctioned());
        Assertions.assertNotNull(result.getData().getAddress());
        Assertions.assertNotNull(result.getIdentification().getLegalForm());
        Assertions.assertNotNull(result.getIdentification().getCountryRegistered());

        pscInsertAssertions(expectedDocument, result);
    }

    @Test
    void testInsertLegalPersonBeneficialOwnerPscIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(LEGAL_BO_KIND);
        PscDocument expectedDocument = TestHelper.buildPscDocument(LEGAL_BO_KIND);
        // when
        PscDocument result = pscTransformer.transformPscOnInsert(NOTIFICATION_ID, fullRecordCompanyPSCApi);
        // then
        Assertions.assertNotNull(result.getData());

        Assertions.assertTrue(result.getData().getSanctioned());
        Assertions.assertNotNull(result.getData().getAddress());
        Assertions.assertNotNull(result.getIdentification().getLegalForm());
        Assertions.assertNull(result.getIdentification().getCountryRegistered());

        pscInsertAssertions(expectedDocument, result);
    }

    @Test
    void testInsertSuperSecureBeneficialOwnerPscIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(SECURE_BO_KIND);
        PscDocument expectedDocument = TestHelper.buildPscDocument(SECURE_BO_KIND);
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

        assertThat(result.getSensitiveData(),is(expectedDocument.getSensitiveData()));
        assertThat(result.getIdentification(), is(expectedDocument.getIdentification()));

        assertThat(result.getDeltaAt(), is(expectedDocument.getDeltaAt()));
        assertThat(result.getUpdatedBy(), is(expectedDocument.getUpdatedBy()));
        assertThat(result.getUpdated().getAt(), is(expectedDocument.getUpdated().getAt()));

        result.setUpdated(expectedDocument.getUpdated()); // Updated objects would be different objects
        assertThat(result, is(expectedDocument));
    }

    @Test
    void testBasicPscDocumentWithNullDataTransform(){
        PscDocument pscDocument = new PscDocument();
        pscDocument.setData(new PscData());
        pscDocument.setDeltaAt("20230102030405000000");
        pscDocument.setSensitiveData(new PscSensitiveData());

        Individual individual = pscTransformer.transformPscDocToIndividual(pscDocument, SHOW_FULL_DOB_TRUE);

        Assertions.assertNotNull(individual);
        Assertions.assertEquals("2023-01-02", individual.getNotifiedOn().toString());
    }

    @Test
    void testEmptyPscIndividualTransform(){
        Individual individual = pscTransformer
                .transformPscDocToIndividual(new PscDocument(), SHOW_FULL_DOB_TRUE);
        Assertions.assertNotNull(individual);
    }

    @Test
    void testEmptyPscIndividualBeneficialOwnerTransform(){
        IndividualBeneficialOwner individualBeneficialOwner = pscTransformer
                .transformPscDocToIndividualBeneficialOwner(new PscDocument(), SHOW_FULL_DOB_TRUE);
        Assertions.assertNotNull(individualBeneficialOwner);
    }

    @Test
    void testEmptyPscCorporateEntityTransform(){
        CorporateEntity corporateEntity = pscTransformer
                .transformPscDocToCorporateEntity(new PscDocument());
        Assertions.assertNotNull(corporateEntity);
    }

    @Test
    void testEmptyPscCorporateEntityBeneficialOwnerTransform(){
        CorporateEntityBeneficialOwner corporateEntityBeneficialOwner = pscTransformer
                .transformPscDocToCorporateEntityBeneficialOwner(new PscDocument());
        Assertions.assertNotNull(corporateEntityBeneficialOwner);
    }

    @Test
    void testEmptyPscLegalPersonTransform(){
        LegalPerson legalPerson = pscTransformer
                .transformPscDocToLegalPerson(new PscDocument());
        Assertions.assertNotNull(legalPerson);
    }

    @Test
    void testEmptyPscLegalPersonBeneficialOwnerTransform(){
        LegalPersonBeneficialOwner legalPersonBeneficialOwner = pscTransformer
                .transformPscDocToLegalPersonBeneficialOwner(new PscDocument());
        Assertions.assertNotNull(legalPersonBeneficialOwner);
    }

    @Test
    void testEmptyPscSuperSecureTransform(){
        SuperSecure superSecure = pscTransformer
                .transformPscDocToSuperSecure(new PscDocument());
        Assertions.assertNotNull(superSecure);
    }

    @Test
    void testEmptyPscSuperSecureBeneficialOwnerTransform(){
        SuperSecureBeneficialOwner superSecureBeneficialOwner = pscTransformer
                .transformPscDocToSuperSecureBeneficialOwner(new PscDocument());
        Assertions.assertNotNull(superSecureBeneficialOwner);
    }

    @Test
    void testEmptyListSummaryTransform(){
        ListSummary listSummary = pscTransformer
                .transformPscDocToListSummary(new PscDocument(), false);
        Assertions.assertNotNull(listSummary);
    }

    @Test
    void testBasicListSummaryWithNullDataTransform(){
        PscDocument pscDocument = new PscDocument();
        PscData pscData = new PscData();
        pscData.setKind("individual-person-with-significant-control");
        pscDocument.setData(pscData);
        pscDocument.setDeltaAt("20230102030405000000");
        pscDocument.setSensitiveData(new PscSensitiveData());

        ListSummary listSummary = pscTransformer
                .transformPscDocToListSummary(pscDocument, true);
        Assertions.assertNotNull(listSummary);
    }

}
