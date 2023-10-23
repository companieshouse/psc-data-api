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
import uk.gov.companieshouse.pscdataapi.models.DateOfBirth;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.models.PscSensitiveData;
import uk.gov.companieshouse.pscdataapi.models.Updated;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

import javax.xml.transform.TransformerException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class CompanyPscTransformerTest {

    @Mock
    private Logger logger;

    private static final String NOTIFICATION_ID = "pscId";

    private static final String MOCK_COMPANY_NUMBER = "1234567";

    private static final String INDIVIDUAL_KIND = "individual-person-with-significant-control";
    private static final String SECURE_KIND = "super-secure-person-with-significant-control";
    private static final String CORPORATE_KIND = "corporate-entity-person-with-significant-control";

    private static final Boolean REGISTER_VIEW_TRUE = true;

    private static final Boolean REGISTER_VIEW_FALSE = false;

    @InjectMocks
    private CompanyPscTransformer pscTransformer;
    private FullRecordCompanyPSCApi fullRecordCompanyPSCApi;

    private FullRecordCompanyPSCApi request;

    private PscDocument document;

    private String dateString;

    @BeforeEach
    public void setUp() {
        OffsetDateTime date = OffsetDateTime.now();
        request = new FullRecordCompanyPSCApi();
        InternalData internal = new InternalData();
        ExternalData external = new ExternalData();
        Data data = new Data();
        external.setNotificationId(NOTIFICATION_ID);
        external.setData(data);
        data.setKind("individual-person-with-significant-control");
        internal.setDeltaAt(date);
        request.setInternalData(internal);
        request.setExternalData(external);
        document = new PscDocument();
        document.setUpdated(new Updated().setAt(LocalDate.now()));
        final DateTimeFormatter dateTimeFormatter =
                DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS");
        dateString = date.format(dateTimeFormatter);
        document.setCompanyNumber(MOCK_COMPANY_NUMBER);
        document.setPscId("1234");
        PscData pscData = new PscData();
        pscData.setKind("individual-person-with-significant-control");
        document.setNotificationId(MOCK_COMPANY_NUMBER);
        document.setData(pscData);
        uk.gov.companieshouse.pscdataapi.models.DateOfBirth dob = new DateOfBirth();
        dob.setDay(21);
        dob.setMonth(12);
        dob.setYear(1943);
        PscSensitiveData sensitiveData = new PscSensitiveData();
        sensitiveData.setDateOfBirth(dob);
        document.setSensitiveData(sensitiveData);
        Identification identification = new Identification();
        identification.setCountryRegistered("x");
        identification.setLegalForm("x");
        identification.setPlaceRegistered("x");
        identification.setLegalAuthority("x");
        identification.setRegistrationNumber("x");
        document.setIdentification(identification);


    }

    @Test
    void testIndividualPscIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        fullRecordCompanyPSCApi = TestHelper.buildFullRecordPsc(INDIVIDUAL_KIND);
        PscDocument expectedDocument = TestHelper.buildPscDocument(INDIVIDUAL_KIND, true);
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
        PscDocument expectedDocument = TestHelper.buildPscDocument(CORPORATE_KIND, REGISTER_VIEW_TRUE);
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
        PscDocument expectedDocument = TestHelper.buildPscDocument(SECURE_KIND, REGISTER_VIEW_TRUE);
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
    void testIndividualPscIsTransformedSuccessfullyWhenRegisterViewIsFalse() throws FailedToTransformException, TransformerException {
        // given
        PscDocument expectedDocument = TestHelper.buildPscDocument(INDIVIDUAL_KIND, REGISTER_VIEW_FALSE);
        // when
        Individual result = pscTransformer.transformPscDocToIndividual(Optional.of(document), REGISTER_VIEW_FALSE);
        // then
        assertThat(result.getDateOfBirth(), is(expectedDocument.getSensitiveData().getDateOfBirth()));
    }

    @Test
    void testIndividualPscIsTransformedSuccessfullyWhenRegisterViewIsTrue() throws FailedToTransformException, TransformerException {
        // given
        PscDocument expectedDocument = TestHelper.buildPscDocument(INDIVIDUAL_KIND, REGISTER_VIEW_TRUE);
        // when
        Individual result = pscTransformer.transformPscDocToIndividual(Optional.of(document), REGISTER_VIEW_TRUE);
        // then
        assertThat(result.getDateOfBirth(), is(expectedDocument.getSensitiveData().getDateOfBirth()));
    }
}
