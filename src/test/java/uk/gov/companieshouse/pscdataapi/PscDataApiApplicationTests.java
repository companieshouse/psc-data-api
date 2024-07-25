package uk.gov.companieshouse.pscdataapi;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.companieshouse.api.api.CompanyExemptionsApiService;

@SpringBootTest
class PscDataApiApplicationTests {

    @MockBean
    CompanyExemptionsApiService companyExemptionsApiService;

    @Test
    void contextLoads() {
    }

}