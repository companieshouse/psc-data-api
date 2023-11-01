package uk.gov.companieshouse.pscdataapi.transform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.psc.*;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscdataapi.exceptions.FailedToTransformException;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
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
        PscDocument result = pscTransformer.transformPsc(NOTIFICATION_ID, fullRecordCompanyPSCApi);

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
        PscDocument result = pscTransformer.transformPsc(NOTIFICATION_ID, fullRecordCompanyPSCApi);

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
        PscDocument result = pscTransformer.transformPsc(NOTIFICATION_ID, fullRecordCompanyPSCApi);

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
        PscDocument result = pscTransformer.transformPsc(NOTIFICATION_ID, fullRecordCompanyPSCApi);

        // then
        assertThat(result.getData(), is(expectedDocument.getData()));
        assertThat(result.getData().getCeased(), is(expectedDocument.getData().getCeased()));
        assertThat(result.getDeltaAt(), is(expectedDocument.getDeltaAt()));
        assertThat(result.getUpdated().getAt(), is(expectedDocument.getUpdated().getAt()));
        assertThat(result.getUpdatedBy(), is(expectedDocument.getUpdatedBy()));
    }
}
