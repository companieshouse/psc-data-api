package uk.gov.companieshouse.pscdataapi.transform;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.psc.Data;
import uk.gov.companieshouse.api.psc.DateOfBirth;
import uk.gov.companieshouse.api.psc.ExternalData;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.InternalData;
import uk.gov.companieshouse.api.psc.SensitiveData;
import uk.gov.companieshouse.pscdataapi.exceptions.FailedToTransformException;
import uk.gov.companieshouse.pscdataapi.models.NameElements;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.models.Updated;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class CompanyPscTransformerTest {

    private static final String INDIVIDUAL_KIND = "individual-person-with-significant-control";
    private static final String SECURE_KIND = "super-secure-person-with-significant-control";
    private static final String CORPORATE_KIND = "corporate-entity-person-with-significant-control";

    private CompanyPscTransformer pscTransformer = new CompanyPscTransformer();
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

    @Test
    void testApiThrowsExceptionWhenTransformFails() {
        FullRecordCompanyPSCApi api = new FullRecordCompanyPSCApi();
        try {
            pscTransformer.transformPsc("id", api);
            Assert.fail("Expected a FailedToTransformException to be thrown");
        } catch (FailedToTransformException e) {
            assert(e.getMessage().contains("Failed to transform API payload:"));
        }
    }



}
