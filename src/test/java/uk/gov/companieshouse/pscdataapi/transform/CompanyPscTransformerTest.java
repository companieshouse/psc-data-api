package uk.gov.companieshouse.pscdataapi.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

    private static final String NOTIFICATION_ID = "pscId";

    private static final String INDIVIDUAL_KIND = "individual-person-with-significant-control";
    private static final String SECURE_KIND = "super-secure-person-with-significant-control";
    private static final String CORPORATE_KIND = "corporate-entity-person-with-significant-control";

    private static final Boolean REGISTER_VIEW_TRUE = true;
    private static final Boolean REGISTER_VIEW_FALSE = false;

    @InjectMocks
    private CompanyPscTransformer pscTransformer;
    private FullRecordCompanyPSCApi fullRecordCompanyPSCApi;

    @BeforeEach
    public void setUp() {

    }

    @Test
    void testIndividualPscIsTransformedSuccessfullyWhenRegisterViewTrue() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(INDIVIDUAL_KIND, REGISTER_VIEW_TRUE);
        PscDocument expectedDocument = TestHelper.buildPscDocument(INDIVIDUAL_KIND, REGISTER_VIEW_TRUE);
        // when
        PscDocument result = pscTransformer.transformPscOnInsert(NOTIFICATION_ID, fullRecordCompanyPSCApi);

        // then
        assertThat(result.getData(), is(expectedDocument.getData()));
        assertThat(result.getData().getNameElements().getSurname(),
                is(expectedDocument.getData().getNameElements().getSurname()));
        assertThat(result.getDeltaAt(), is(expectedDocument.getDeltaAt()));
        assertThat(result.getUpdated().getAt(), is(expectedDocument.getUpdated().getAt()));
        assertThat(result.getUpdatedBy(), is(expectedDocument.getUpdatedBy()));
        assertThat(result.getSensitiveData().getDateOfBirth(),is(expectedDocument.getSensitiveData().getDateOfBirth()));
    }

    @Test
    void testIndividualPscIsTransformedSuccessfullyWhenRegisterViewFalse() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(INDIVIDUAL_KIND, REGISTER_VIEW_FALSE);
        PscDocument expectedDocument = TestHelper.buildPscDocument(INDIVIDUAL_KIND, REGISTER_VIEW_FALSE);
        // when
        PscDocument result = pscTransformer.transformPscOnInsert(NOTIFICATION_ID, fullRecordCompanyPSCApi);

        // then
        assertThat(result.getData(), is(expectedDocument.getData()));
        assertThat(result.getData().getNameElements().getSurname(),
                is(expectedDocument.getData().getNameElements().getSurname()));
        assertThat(result.getDeltaAt(), is(expectedDocument.getDeltaAt()));
        assertThat(result.getUpdated().getAt(), is(expectedDocument.getUpdated().getAt()));
        assertThat(result.getUpdatedBy(), is(expectedDocument.getUpdatedBy()));
        assertThat(result.getSensitiveData().getDateOfBirth(),is(expectedDocument.getSensitiveData().getDateOfBirth()));
    }

    @Test
    void testCorporatePscIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(CORPORATE_KIND, REGISTER_VIEW_TRUE);
        PscDocument expectedDocument = TestHelper.buildPscDocument(CORPORATE_KIND, REGISTER_VIEW_TRUE);
        // when
        PscDocument result = pscTransformer.transformPscOnInsert(NOTIFICATION_ID, fullRecordCompanyPSCApi);

        // then
        assertThat(result.getData(), is(expectedDocument.getData()));
        assertThat(result.getDeltaAt(), is(expectedDocument.getDeltaAt()));
        assertThat(result.getUpdated().getAt(), is(expectedDocument.getUpdated().getAt()));
        assertThat(result.getUpdatedBy(), is(expectedDocument.getUpdatedBy()));
    }

    @Test
    void testSecurePscIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(SECURE_KIND, REGISTER_VIEW_TRUE);
        PscDocument expectedDocument = TestHelper.buildPscDocument(SECURE_KIND, REGISTER_VIEW_TRUE);
        // when
        PscDocument result = pscTransformer.transformPscOnInsert(NOTIFICATION_ID, fullRecordCompanyPSCApi);

        // then
        assertThat(result.getData(), is(expectedDocument.getData()));
        assertThat(result.getData().getCeased(), is(expectedDocument.getData().getCeased()));
        assertThat(result.getDeltaAt(), is(expectedDocument.getDeltaAt()));
        assertThat(result.getUpdated().getAt(), is(expectedDocument.getUpdated().getAt()));
        assertThat(result.getUpdatedBy(), is(expectedDocument.getUpdatedBy()));
    }

    @Test
    void testBasicPscDocumentWithNullDataTransform(){
        PscDocument pscDocument = new PscDocument();
        pscDocument.setData(new PscData());
        pscDocument.setDeltaAt("20230102030405000000");
        pscDocument.setSensitiveData(new PscSensitiveData());

        Individual individual = pscTransformer.transformPscDocToIndividual(pscDocument, true);

        Assertions.assertNotNull(individual);
        Assertions.assertEquals("2023-01-02", individual.getNotifiedOn().toString());
    }

    @Test
    void testEmptyPscIndividualTransform(){
        Individual individual = pscTransformer
                .transformPscDocToIndividual(new PscDocument(), true);
        Assertions.assertNotNull(individual);
    }

    @Test
    void testEmptyPscIndividualBeneficialOwnerTransform(){
        IndividualBeneficialOwner individualBeneficialOwner = pscTransformer
                .transformPscDocToIndividualBeneficialOwner(new PscDocument(), true);
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

    @Test
    void testEmptyPscInsertTransform(){
        PscDocument pscDocument = pscTransformer
                .transformPscOnInsert(null, new FullRecordCompanyPSCApi());
        Assertions.assertNotNull(pscDocument);
    }
}
