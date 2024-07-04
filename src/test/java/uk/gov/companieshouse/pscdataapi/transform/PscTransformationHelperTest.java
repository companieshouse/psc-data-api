package uk.gov.companieshouse.pscdataapi.transform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.pscdataapi.util.PscTransformationHelper;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

class PscTransformationHelperTest {

    @Test
    void testIndividualPscResourceKindIsMappedCorrectly() {
        //when
        String validKind = PscTransformationHelper.mapResourceKind(TestHelper.INDIVIDUAL_KIND);
        //then
        assertEquals("company-psc-individual", validKind);
    }

    @Test
    void testSecurePscResourceKindIsMappedCorrectly() {
        //when
        String validKind = PscTransformationHelper.mapResourceKind(TestHelper.SECURE_KIND);
        //then
        assertThat(validKind, is("company-psc-supersecure"));
    }

    @Test
    void testCorporatePscResourceKindIsMappedCorrectly() {
        //when
        String validKind = PscTransformationHelper.mapResourceKind(TestHelper.CORPORATE_KIND);
        //then
        assertThat(validKind, is("company-psc-corporate"));
    }

    @Test
    void testLegalPscResourceKindIsMappedCorrectly() {
        //when
        String validKind = PscTransformationHelper.mapResourceKind(TestHelper.LEGAL_KIND);
        //then
        assertThat(validKind, is("company-psc-legal"));
    }

    @Test
    void testIndividualBOResourceKindIsMappedCorrectly() {
        //when
        String validKind = PscTransformationHelper.mapResourceKind(TestHelper.INDIVIDUAL_BO_KIND);
        //then
        assertThat(validKind, is(TestHelper.INDIVIDUAL_BO_KIND));
    }

    @Test
    void testIndividualBOResourceKindIsNotMappedToAnythingElse() {
        //when
        String validKind = PscTransformationHelper.mapResourceKind(TestHelper.INDIVIDUAL_BO_KIND);
        //then
        assertThat(validKind, is(not(TestHelper.INDIVIDUAL_KIND)));
    }
}
