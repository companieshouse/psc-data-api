package uk.gov.companieshouse.pscdataapi.transform;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
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

    private static final String INDIVIDUAL_KIND = "individual-person-with-significant-control";
    private static final String SECURE_KIND = "super-secure-person-with-significant-control";
    private static final String CORPORATE_KIND = "corporate-entity-person-with-significant-control";

    @InjectMocks
    private CompanyPscTransformer pscTransformer;
    private FullRecordCompanyPSCApi fullRecordCompanyPSCApi;

    @Test
    void testIndividualPscIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(INDIVIDUAL_KIND);
        PscDocument expectedDocument = TestHelper.buildPscDocument(INDIVIDUAL_KIND);
        // when
        PscDocument result = pscTransformer.transformPsc("pscId", fullRecordCompanyPSCApi);

        // then
        assertThat(result.getData(), is(expectedDocument.getData()));
        assertThat(result.getData().getNameElements().getSurname(),
                is(expectedDocument.getData().getNameElements().getSurname()));
        assertThat(result.getDeltaAt(), is(expectedDocument.getDeltaAt()));
        assertThat(result.getUpdated().getAt(), is(expectedDocument.getUpdated().getAt()));
        assertThat(result.getUpdatedBy(), is(expectedDocument.getUpdatedBy()));
    }

    @Test
    void testCorporatePscIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(CORPORATE_KIND);
        PscDocument expectedDocument = TestHelper.buildPscDocument(CORPORATE_KIND);
        // when
        PscDocument result = pscTransformer.transformPsc("pscId", fullRecordCompanyPSCApi);

        // then
        assertThat(result.getData(), is(expectedDocument.getData()));
        assertThat(result.getDeltaAt(), is(expectedDocument.getDeltaAt()));
        assertThat(result.getUpdated().getAt(), is(expectedDocument.getUpdated().getAt()));
        assertThat(result.getUpdatedBy(), is(expectedDocument.getUpdatedBy()));
    }

    @Test
    void testSecurePscIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(SECURE_KIND);
        PscDocument expectedDocument = TestHelper.buildPscDocument(SECURE_KIND);
        // when
        PscDocument result = pscTransformer.transformPsc("pscId", fullRecordCompanyPSCApi);

        // then
        assertThat(result.getData(), is(expectedDocument.getData()));
        assertThat(result.getData().getCeased(), is(expectedDocument.getData().getCeased()));
        assertThat(result.getDeltaAt(), is(expectedDocument.getDeltaAt()));
        assertThat(result.getUpdated().getAt(), is(expectedDocument.getUpdated().getAt()));
        assertThat(result.getUpdatedBy(), is(expectedDocument.getUpdatedBy()));
    }
}
