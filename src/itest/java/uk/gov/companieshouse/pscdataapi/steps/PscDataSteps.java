package uk.gov.companieshouse.pscdataapi.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.bytebuddy.implementation.bind.annotation.Super;
import org.assertj.core.api.Assertions;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.pscdataapi.config.AbstractMongoConfig.mongoDBContainer;

import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.delta.Psc;
import uk.gov.companieshouse.api.psc.*;
import uk.gov.companieshouse.pscdataapi.api.ChsKafkaApiService;
import uk.gov.companieshouse.pscdataapi.config.CucumberContext;
import uk.gov.companieshouse.pscdataapi.models.*;
import uk.gov.companieshouse.pscdataapi.models.Address;
import uk.gov.companieshouse.pscdataapi.models.DateOfBirth;
import uk.gov.companieshouse.pscdataapi.models.NameElements;
import uk.gov.companieshouse.pscdataapi.transform.CompanyPscTransformer;
import uk.gov.companieshouse.pscdataapi.util.FileReaderUtil;
import uk.gov.companieshouse.pscdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.repository.CompanyPscRepository;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;
import javax.xml.transform.TransformerException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

public class PscDataSteps {

    private String contextId;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CompanyPscRepository companyPscRepository;

    @Autowired
    private ChsKafkaApiService chsKafkaApiService;

    @Autowired
    private CompanyPscTransformer transformer;

    private final String COMPANY_NUMBER = "34777772";
    private final String NOTIFICATION_ID = "ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ";
    @Autowired
    private CompanyPscService companyPscService;

    @Before
    public void dbCleanUp(){
        if (!mongoDBContainer.isRunning()) {
            mongoDBContainer.start();
        }
        companyPscRepository.deleteAll();
        MockitoAnnotations.initMocks(this);
    }

    @Given("Psc data api service is running")
    public void theApplicationRunning() {
        assertThat(restTemplate).isNotNull();
    }

    @Given("a psc data record exists with notification id {string} and delta_at {string}")
    public void psc_record_exists_for_company_and_id_with_delta_at(String notifcationId, String deltaAt) throws IOException {
        String pscDataFile = FileReaderUtil.readFile("src/itest/resources/json/input/psc_data_api.json");
        PscData pscData = objectMapper.readValue(pscDataFile, PscData.class);

        PscDocument document = new PscDocument();
        document.setId(notifcationId);
        document.setCompanyNumber(COMPANY_NUMBER);
        document.setData(pscData);
        document.setDeltaAt(deltaAt);
        mongoTemplate.save(document);
        assertThat(companyPscRepository.findById(notifcationId)).isNotEmpty();
    }


    @And("nothing is persisted to the database")
    public void nothingIsPersistedInTheDatabase() {
        assertThat(companyPscRepository.findAll()).isEmpty();
    }

    @Then("the CHS Kafka API is not invoked")
    public void chs_kafka_api_not_invoked() throws IOException {
        verify(chsKafkaApiService, times(0)).invokeChsKafkaApi(any(), any(), any(), any());
    }

    @And("the CHS Kafka API service is not invoked")
    public void verifyChsKafkaApiNotInvoked(){
        verifyNoInteractions(chsKafkaApiService);
    }



    @When("I send a PUT request with payload {string} file for company number {string} with notification id  {string}")
    public void i_send_psc_record_put_request_with_payload(String dataFile, String companyNumber, String notificationId) {
        String data = FileReaderUtil.readFile("src/itest/resources/json/input/" + dataFile + ".json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity request = new HttpEntity(data, headers);
        String uri = String.format("/company/%s/persons-with-significant-control/%s/full_record",companyNumber, notificationId);
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @When("I send a PUT request with payload {string} file with notification id {string}")
    public void i_send_psc_record_put_request_with_payload(String dataFile, String notificationId) {
        String data = FileReaderUtil.readFile("src/itest/resources/json/input/" + dataFile + ".json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity request = new HttpEntity(data, headers);
        String uri = "/company/{company_number}/persons-with-significant-control/{notfication_id}/full_record";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class, COMPANY_NUMBER, notificationId);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @When("I send a PUT request with payload {string} file for record with notification Id {string}")
    public void i_send_psc_data_put_request_with_payload(String dataFile, String notificationId) throws IOException {
        String data = FileReaderUtil.readFile("src/itest/resources/json/input/" + dataFile + ".json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity request = new HttpEntity(data, headers);
        String uri = "/company/{company_number}/persons-with-significant-control/{notification_id}/full_record";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class, COMPANY_NUMBER, notificationId);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @Then("a record exists with id {string}")
    public void statement_exists(String notificationId) {
        Assertions.assertThat(companyPscRepository.existsById(notificationId)).isTrue();
    }

    @Then("I should receive {int} status code")
    public void i_should_receive_status_code(Integer statusCode) {
        int expectedStatusCode = CucumberContext.CONTEXT.get("statusCode");
        Assertions.assertThat(expectedStatusCode).isEqualTo(statusCode);
    }

    @When("a record exists with id {string} and delta_at {string}")
    public void psc_record_exists(String notificationId, String deltaAt) throws NoSuchElementException {
        Assertions.assertThat(companyPscRepository.existsById(notificationId)).isTrue();
        Optional<PscDocument> document = companyPscRepository.findById(notificationId);
        Assertions.assertThat(companyPscRepository.findById(notificationId).get().getDeltaAt()).isEqualTo(deltaAt);
    }

    @When("a DELETE request is sent  for {string} without valid ERIC headers")
    public void aDELETERequestIsSentForWithoutValidERICHeaders(String companyNumber) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);


        HttpEntity request = new HttpEntity(null, headers);
        String uri = "/company/{company_number}/persons-with-significant-control/{notification_id}/full_record";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.DELETE, request, Void.class, companyNumber, NOTIFICATION_ID);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());}


    @When("a DELETE request is sent for {string}")
    public void aDELETERequestIsSentFor(String companyNumber) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity request = new HttpEntity(null, headers);
        String uri = "/company/{company_number}/persons-with-significant-control/{notfication_id}/full_record";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.DELETE, request, Void.class, companyNumber, NOTIFICATION_ID);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @Given("a PSC does not exist for {string}")
    public void aPSCDoesNotExistFor(String companyNumber) {
        assertThat(companyPscRepository.existsById(companyNumber)).isFalse();
    }

    @And("the database is down")
    public void theDatabaseIsDown() {
        mongoDBContainer.stop();
    }

    @When("the chs kafka api is not available")
    public void theChsKafkaApiIsNotAvailable() {
        doThrow(ServiceUnavailableException.class).when(chsKafkaApiService).invokeChsKafkaApi(any(), any(), any(), any());
    }

    @And("a PSC exists for {string} and delta_at \"<deltaAt>")
    public void aPSCExistsForAndDelta_atDeltaAt(String deltaAt) throws Throwable {
        String pscDataFile = FileReaderUtil.readFile("src/itest/resources/json/input/34777772.json");
        PscData pscData = objectMapper.readValue(pscDataFile, PscData.class);

        PscDocument document = new PscDocument();
        document.setId(NOTIFICATION_ID);
        document.setCompanyNumber(COMPANY_NUMBER);
        document.setData(pscData);
        document.setDeltaAt(deltaAt);
        mongoTemplate.save(document);
        assertThat(companyPscRepository.findById(NOTIFICATION_ID)).isNotEmpty();
    }

    @And("a PSC exists for {string} for Super Secure")
    public void aPSCExistsForForSuperSecure(String companyNumber) throws JsonProcessingException {
        String pscDataFile = FileReaderUtil.readFile("src/itest/resources/json/input/"+companyNumber+".json");
        PscData pscData = objectMapper.readValue(pscDataFile, PscData.class);
        PscDocument document = new PscDocument();

        document.setId("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX");
        document.setCompanyNumber(companyNumber);
        document.setPscId("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX");
        document.setDeltaAt("20231120084745378000");
        pscData.setEtag("string");
        pscData.setKind("super-secure-person-with-significant-control");
        pscData.setDescription("super-secure-persons-with-significant-control");
        Links links = new Links();
        links.setSelf("/company/34777777/persons-with-significant-control/super-secure/ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ");
        links.setStatements("string");
        pscData.setLinks(links);
        pscData.setCeased(false);


        document.setData(pscData);

        mongoTemplate.save(document);
        assertThat(companyPscRepository.getPscByCompanyNumberAndId(companyNumber,"ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX")).isNotEmpty();
    }

    @When("a Get request is sent for {string} and {string} for Super Secure")
    public void aGetRequestIsSentForAndForSuperSecure(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri =
                String.format("/company/%s/persons-with-significant-control/super-secure/%s",companyNumber,notification_id);
        ResponseEntity<SuperSecure> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, SuperSecure.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @And("the Get call response body should match {string} file for Super Secure")
    public void theGetCallResponseBodyShouldMatchFileForSuperSecure(String result) throws IOException {
        String data = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream("src/itest/resources/json/output/" + result + ".json")));
        SuperSecure expected = objectMapper.readValue(data, SuperSecure.class);

        SuperSecure actual = CucumberContext.CONTEXT.get("getResponseBody");

        assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
        assertThat(actual.getCeased()).isEqualTo(expected.getCeased());
        assertThat(actual.getKind()).isEqualTo(expected.getKind());

    }

    @When("a Get request is sent for {string} and {string} without ERIC headers for Super Secure")
    public void aGetRequestIsSentForAndWithoutERICHeadersForSuperSecure(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);


        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri =
                String.format("/company/%s/persons-with-significant-control/super-secure/%s",companyNumber,notification_id);
        ResponseEntity<SuperSecure> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, SuperSecure.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @When("a Get request has been sent for {string} and {string} for Super Secure")
    public void aGetRequestHasBeenSentForAndForSuperSecure(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri =
                String.format("/company/%s/persons-with-significant-control/super-secure/%s",companyNumber,notification_id);
        ResponseEntity<SuperSecure> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, SuperSecure.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @And("a PSC exists for {string} for Super Secure Beneficial Owner")
    public void aPSCExistsForForSuperSecureBeneficialOwner(String companyNumber) throws JsonProcessingException {
        String pscDataFile = FileReaderUtil.readFile("src/itest/resources/json/input/"+companyNumber+".json");
        PscData pscData = objectMapper.readValue(pscDataFile, PscData.class);
        PscDocument document = new PscDocument();

        document.setId("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX");
        document.setCompanyNumber(companyNumber);
        document.setPscId("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX");
        document.setDeltaAt("20231120084745378000");
        pscData.setEtag("string");
        pscData.setKind("super-secure-beneficial-owner");
        pscData.setDescription("super-secure-beneficial-owner");
        Links links = new Links();
        links.setSelf("/company/34777776/persons-with-significant-control/super-secure-beneficial-owner/ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ");
        links.setStatements("string");
        pscData.setLinks(links);
        pscData.setCeased(false);


        document.setData(pscData);

        mongoTemplate.save(document);
        assertThat(companyPscRepository.getPscByCompanyNumberAndId(companyNumber,"ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX")).isNotEmpty();
    }

    @When("a Get request is sent for {string} and {string} for Super Secure Beneficial Owner")
    public void aGetRequestIsSentForAndForSuperSecureBeneficialOwner(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri =
                String.format("/company/%s/persons-with-significant-control/super-secure-beneficial-owner/%s",companyNumber,notification_id);
        ResponseEntity<SuperSecureBeneficialOwner> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, SuperSecureBeneficialOwner.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @And("the Get call response body should match {string} file for Super Secure Beneficial Owner")
    public void theGetCallResponseBodyShouldMatchFileForSuperSecureBeneficialOwner(String result) throws IOException {
        String data = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream("src/itest/resources/json/output/" + result + ".json")));
        SuperSecureBeneficialOwner expected = objectMapper.readValue(data, SuperSecureBeneficialOwner.class);

        SuperSecureBeneficialOwner actual = CucumberContext.CONTEXT.get("getResponseBody");

        assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
        assertThat(actual.getCeased()).isEqualTo(expected.getCeased());
        assertThat(actual.getKind()).isEqualTo(expected.getKind());

    }

    @When("a Get request is sent for {string} and {string} without ERIC headers for Super Secure Beneficial Owner")
    public void aGetRequestIsSentForAndWithoutERICHeadersForSuperSecureBeneficialOwner(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);


        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri =
                String.format("/company/%s/persons-with-significant-control/super-secure-beneficial-owner/%s",companyNumber,notification_id);
        ResponseEntity<SuperSecureBeneficialOwner> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, SuperSecureBeneficialOwner.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @When("a Get request has been sent for {string} and {string} for Super Secure Beneficial Owner")
    public void aGetRequestHasBeenSentForAndForSuperSecureBeneficialOwner(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri =
                String.format("/company/%s/persons-with-significant-control/super-secure-beneficial-owner/%s",companyNumber,notification_id);
        ResponseEntity<SuperSecureBeneficialOwner> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, SuperSecureBeneficialOwner.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @And("a PSC exists for {string} for Corporate Entity")
    public void aPSCExistsForCorporateEntity(String companyNumber) throws JsonProcessingException {
        String pscDataFile = FileReaderUtil.readFile("src/itest/resources/json/input/"+companyNumber+".json");
        PscData pscData = objectMapper.readValue(pscDataFile, PscData.class);
        PscDocument document = new PscDocument();

        document.setId("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX");
        document.setCompanyNumber(companyNumber);
        document.setPscId("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX");
        pscData.setEtag("string");
        pscData.setName("string");
        pscData.setNationality("British");
        pscData.setSanctioned(true);
        pscData.setKind("corporate-entity-person-with-significant-control");
        Links links = new Links();
        links.setSelf("/company/34777772/persons-with-significant-control/corporate-entity/ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ");
        links.setStatements("string");
        pscData.setLinks(links);
        Address address = new Address();
        address.setAddressLine1("ura_line1");
        address.setAddressLine2("ura_line2");
        address.setCareOf("ura_care_of");
        address.setCountry("United Kingdom");
        address.setLocality("Cardiff");
        address.setPoBox("ura_po");
        address.setPostalCode("CF2 1B6");
        address.setPremises("URA");
        address.setRegion("ura_region");
        pscData.setAddress(address);
        List<String> list = new ArrayList<>();
        list.add("part-right-to-share-surplus-assets-75-to-100-percent");
        pscData.setNaturesOfControl(list);
        PscIdentification identification = new PscIdentification();
        identification.setRegistrationNumber("123456");
        identification.setPlaceRegistered("Cardiff");
        identification.setCountryRegistered("United Kingdom");
        identification.setLegalAuthority("Cardiff");
        identification.setLegalForm("PSCX");
        pscData.setIdentification(identification);

        document.setData(pscData);

        mongoTemplate.save(document);
        assertThat(companyPscRepository.getPscByCompanyNumberAndId(companyNumber,"ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX")).isNotEmpty();
    }

    @When("a Get request is sent for {string} and {string} for Corporate Entity")
    public void aGetRequestIsSentForAndForCorporateEntity(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri =
                String.format("/company/%s/persons-with-significant-control/corporate-entity/%s",companyNumber,notification_id);
        ResponseEntity<CorporateEntity> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, CorporateEntity.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @And("the Get call response body should match {string} file for Corporate Entity")
    public void theGetCallResponseBodyShouldMatchFileForCorporateEntity(String result) throws IOException {
        String data = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream("src/itest/resources/json/output/" + result + ".json")));
        CorporateEntity expected = objectMapper.readValue(data, CorporateEntity.class);

        CorporateEntity actual = CucumberContext.CONTEXT.get("getResponseBody");

        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getIdentification()).isEqualTo(expected.getIdentification());
        assertThat(actual.getKind()).isEqualTo(expected.getKind());

    }

    @When("a Get request has been sent for {string} and {string} for Corporate Entity")
    public void aGetRequestHasBeenSentForAndForCorporateEntity(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri =
                String.format("/company/%s/persons-with-significant-control/corporate-entity/%s",companyNumber,notification_id);
        ResponseEntity<CorporateEntity> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, CorporateEntity.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @When("a Get request is sent for {string} and {string} without ERIC headers for Corporate Entity")
    public void aGetRequestIsSentForAndWithoutERICHeadersForCorporateEntity(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);


        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri =
                String.format("/company/%s/persons-with-significant-control/corporate-entity/%s",companyNumber,notification_id);
        ResponseEntity<CorporateEntity> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, CorporateEntity.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @And("a PSC exists for {string}")
    public void aPSCExistsFor(String companyNumber) throws JsonProcessingException {
        String pscDataFile = FileReaderUtil.readFile("src/itest/resources/json/input/"+companyNumber+".json");
        PscData pscData = objectMapper.readValue(pscDataFile, PscData.class);
        PscSensitiveData pscSensitiveData = objectMapper.readValue(pscDataFile, PscSensitiveData.class);
        PscDocument document = new PscDocument();


        document.setId(NOTIFICATION_ID);
        document.setCompanyNumber(companyNumber);
        document.setPscId("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ");
        document.setDeltaAt("20231120084745378000");
        pscData.setEtag("string");
        pscData.setCeasedOn(LocalDate.from(LocalDateTime.now()));
        pscData.setKind("individual-person-with-significant-control");
        pscData.setCountryOfResidence("United Kingdom");
        pscData.setName("34777772");
        NameElements nameElements = new NameElements();
        nameElements.setTitle("Mr");
        nameElements.setForename("PHIL");
        nameElements.setMiddleName("tom");
        nameElements.setSurname("JONES");
        pscData.setNameElements(nameElements);
        DateOfBirth dateOfBirth = new DateOfBirth();
        dateOfBirth.setDay(2);
        dateOfBirth.setMonth(03);
        dateOfBirth.setYear(1994);
        pscSensitiveData.setDateOfBirth(dateOfBirth);
        document.setSensitiveData(pscSensitiveData);
        Links links = new Links();
        links.setSelf("/company/34777772/persons-with-significant-control/individual/ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ");
        links.setStatements("string");
        pscData.setLinks(links);
        pscData.setNationality("British");
        Address address = new Address();
        address.setAddressLine1("ura_line1");
        address.setAddressLine2("ura_line2");
        address.setCareOf("ura_care_of");
        address.setCountry("United Kingdom");
        address.setLocality("Cardiff");
        address.setPoBox("ura_po");
        address.setPostalCode("CF2 1B6");
        address.setPremises("URA");
        address.setRegion("ura_region");
        pscData.setAddress(address);
        List<String> list = new ArrayList<>();
        list.add("part-right-to-share-surplus-assets-75-to-100-percent");
        pscData.setNaturesOfControl(list);
        document.setData(pscData);

        mongoTemplate.save(document);
        assertThat(companyPscRepository.findById(NOTIFICATION_ID)).isNotEmpty();
    }

    @When("a Get request is sent for {string} and {string}")
    public void aGetRequestIsSentForAnd(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri = "/company/{company_number}/persons-with-significant-control/individual/{notification_id}";
        ResponseEntity<Individual> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, Individual.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());

    }

    @And("the Get call response body should match {string} file")
    public void theGetCallResponseBodyShouldMatchFile(String result) throws IOException, TransformerException {
        String data = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream("src/itest/resources/json/output/" + result + ".json")));
        Individual expected = objectMapper.readValue(data, Individual.class);

        Individual actual = CucumberContext.CONTEXT.get("getResponseBody");

        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getCountryOfResidence()).isEqualTo(expected.getCountryOfResidence());
        assertThat(actual.getNaturesOfControl()).isEqualTo(expected.getNaturesOfControl());
    }

    @After
    public void dbStop(){
        mongoDBContainer.stop();
    }

    @When("a Get request is sent for {string} and {string} without ERIC headers")
    public void aGetRequestIsSentForAndWithoutERICHeaders(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);


        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri = "/company/{company_number}/persons-with-significant-control/individual/{notification_id}";
        ResponseEntity<Individual> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, Individual.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());

    }

    @When("a Get request has been sent for {string} and {string}")
    public void aGetRequestHasBeenSentForAnd(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri = "/company/{company_number}/persons-with-significant-control/individual/{notification_id}";
        ResponseEntity<Individual> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, Individual.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @And("a PSC exists for {string} for Individual Beneficial Owner")
    public void aPSCExistsForForIndividualBeneficialOwner(String companyNumber) throws JsonProcessingException {
        String pscDataFile = FileReaderUtil.readFile("src/itest/resources/json/input/"+companyNumber+".json");
        PscData pscData = objectMapper.readValue(pscDataFile, PscData.class);
        PscSensitiveData pscSensitiveData = objectMapper.readValue(pscDataFile, PscSensitiveData.class);
        PscDocument document = new PscDocument();

        document.setId("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX");
        document.setCompanyNumber(companyNumber);
        document.setPscId("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX");
        document.setDeltaAt("20231120084745378000");
        pscData.setEtag("string");
        pscData.setName("string");
        pscData.setNationality("British");
        pscData.setSanctioned(true);
        pscData.setKind("individual-beneficial-owner");
        DateOfBirth dateOfBirth = new DateOfBirth();
        dateOfBirth.setDay(2);
        dateOfBirth.setMonth(03);
        dateOfBirth.setYear(1994);
        pscSensitiveData.setDateOfBirth(dateOfBirth);
        document.setSensitiveData(pscSensitiveData);


        document.setData(pscData);

        mongoTemplate.save(document);
        assertThat(companyPscRepository.findById("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX")).isNotEmpty();
    }

    @When("a Get request is sent for {string} and {string} for Individual Beneficial Owner")
    public void aGetRequestIsSentForAndForIndividualBeneficialOwner(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri =
                "/company/{company_number}/persons-with-significant-control/individual-beneficial-owner/{notification_id}";
        ResponseEntity<IndividualBeneficialOwner> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, IndividualBeneficialOwner.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @And("the Get call response body should match {string} file for Individual Beneficial Owner")
    public void theGetCallResponseBodyShouldMatchFileForIndividualBeneficialOwner(String result) throws IOException {
        String data = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream("src/itest/resources/json/output/" + result + ".json")));
        IndividualBeneficialOwner expected = objectMapper.readValue(data, IndividualBeneficialOwner.class);

        IndividualBeneficialOwner actual = CucumberContext.CONTEXT.get("getResponseBody");

        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getIsSanctioned()).isEqualTo(expected.getIsSanctioned());
        assertThat(actual.getNationality()).isEqualTo(expected.getNationality());

    }

    @When("a Get request has been sent for {string} and {string} for Individual Beneficial Owner")
    public void aGetRequestHasBeenSentForAndForIndividualBeneficialOwner(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri =
                "/company/{company_number}/persons-with-significant-control/individual-beneficial-owner/{notification_id}";
        ResponseEntity<IndividualBeneficialOwner> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, IndividualBeneficialOwner.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @When("a Get request is sent for {string} and {string} without ERIC headers for Individual Beneficial Owner")
    public void aGetRequestIsSentForAndWithoutERICHeadersForIndividualBeneficialOwner(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);


        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri = "/company/{company_number}/persons-with-significant-control/individual-beneficial-owner/{notification_id}";
        ResponseEntity<IndividualBeneficialOwner> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, IndividualBeneficialOwner.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @And("a PSC exists for {string} for Corporate Entity Beneficial Owner")
    public void aPSCExistsForForCorporateEntityBeneficialOwner(String companyNumber) throws JsonProcessingException {
        String pscDataFile = FileReaderUtil.readFile("src/itest/resources/json/input/"+companyNumber+".json");
        PscData pscData = objectMapper.readValue(pscDataFile, PscData.class);
        PscDocument document = new PscDocument();

        document.setId("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZC");
        document.setCompanyNumber(companyNumber);
        document.setPscId("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZC");
        document.setDeltaAt("20231120084745378000");
        pscData.setEtag("string");
        pscData.setName("string");
        pscData.setNationality("British");
        pscData.setSanctioned(true);
        pscData.setKind("corporate-entity-beneficial-owner");


        document.setData(pscData);

        mongoTemplate.save(document);
        assertThat(companyPscRepository.findById("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZC")).isNotEmpty();
    }

    @When("a Get request is sent for {string} and {string} for Corporate Entity Beneficial Owner")
    public void aGetRequestIsSentForAndForCorporateEntityBeneficialOwner(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri =
                "/company/{company_number}/persons-with-significant-control/corporate-entity-beneficial-owner/{notification_id}";
        ResponseEntity<CorporateEntityBeneficialOwner> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, CorporateEntityBeneficialOwner.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @And("the Get call response body should match {string} file for Corporate Entity Beneficial Owner")
    public void theGetCallResponseBodyShouldMatchFileForCorporateEntityBeneficialOwner(String result) throws IOException {
        String data = FileCopyUtils.copyToString(new InputStreamReader(
                new FileInputStream("src/itest/resources/json/output/" + result + ".json")));
        CorporateEntityBeneficialOwner expected = objectMapper.readValue(data, CorporateEntityBeneficialOwner.class);

        CorporateEntityBeneficialOwner actual = CucumberContext.CONTEXT.get("getResponseBody");

        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getIsSanctioned()).isEqualTo(expected.getIsSanctioned());
    }

    @When("a Get request is sent for {string} and {string} without ERIC headers for Corporate Entity Beneficial Owner")
    public void aGetRequestIsSentForAndWithoutERICHeadersForCorporateEntityBeneficialOwner(
            String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);


        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri = "/company/{company_number}/persons-with-significant-control/corporate-entity-beneficial-owner/{notification_id}";
        ResponseEntity<CorporateEntityBeneficialOwner> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, CorporateEntityBeneficialOwner.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @When("a Get request has been sent for {string} and {string} for Corporate Entity Beneficial Owner")
    public void aGetRequestHasBeenSentForAndForCorporateEntityBeneficialOwner(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri =
                "/company/{company_number}/persons-with-significant-control/corporate-entity-beneficial-owner/{notification_id}";
        ResponseEntity<CorporateEntityBeneficialOwner> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, CorporateEntityBeneficialOwner.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }


    @And("a PSC exists for {string} for Legal Person")
    public void aPSCExistsForForLegalPerson(String companyNumber) throws JsonProcessingException {
        String pscDataFile = FileReaderUtil.readFile("src/itest/resources/json/input/"+companyNumber+".json");
        PscData pscData = objectMapper.readValue(pscDataFile, PscData.class);
        PscDocument document = new PscDocument();

        document.setId("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZV");
        document.setCompanyNumber(companyNumber);
        document.setPscId("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZV");
        document.setDeltaAt("20231120084745378000");
        pscData.setEtag("string");
        pscData.setName("string");
        pscData.setNationality("British");
        pscData.setSanctioned(true);
        pscData.setKind("legal-person-person-with-significant-control");


        document.setData(pscData);

        mongoTemplate.save(document);
        assertThat(companyPscRepository.findById("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZV")).isNotEmpty();
    }

    @When("a Get request is sent for {string} and {string} for Legal Person")
    public void aGetRequestIsSentForAndForLegalPerson(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri =
                "/company/{company_number}/persons-with-significant-control/legal-person/{notification_id}";
        ResponseEntity<LegalPerson> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, LegalPerson.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @And("the Get call response body should match {string} file for Legal Person")
    public void theGetCallResponseBodyShouldMatchFileForLegalPerson(String result) throws IOException {
        String data = FileCopyUtils.copyToString(new InputStreamReader(
                new FileInputStream("src/itest/resources/json/output/" + result + ".json")));
        LegalPerson expected = objectMapper.readValue(data, LegalPerson.class);

        LegalPerson actual = CucumberContext.CONTEXT.get("getResponseBody");

        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getCeasedOn()).isEqualTo(expected.getCeasedOn());
    }

    @When("a Get request is sent for {string} and {string} without ERIC headers for Legal Person")
    public void aGetRequestIsSentForAndWithoutERICHeadersForLegalPerson(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);


        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri = "/company/{company_number}/persons-with-significant-control/legal-person/{notification_id}";
        ResponseEntity<LegalPerson> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, LegalPerson.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @When("a Get request has been sent for {string} and {string} for Legal Person")
    public void aGetRequestHasBeenSentForAndForLegalPerson(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri =
                "/company/{company_number}/persons-with-significant-control/legal-person/{notification_id}";
        ResponseEntity<LegalPerson> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, LegalPerson.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @And("a PSC exists for {string} for Legal Person Beneficial Owner")
    public void aPSCExistsForForLegalPersonBeneficialOwner(String companyNumber) throws JsonProcessingException {
        String pscDataFile = FileReaderUtil.readFile("src/itest/resources/json/input/"+companyNumber+".json");
        PscData pscData = objectMapper.readValue(pscDataFile, PscData.class);
        PscDocument document = new PscDocument();

        document.setId("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZVV");
        document.setCompanyNumber(companyNumber);
        document.setPscId("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZVV");
        document.setDeltaAt("20231120084745378000");
        pscData.setEtag("string");
        pscData.setName("string");
        pscData.setNationality("British");
        pscData.setSanctioned(true);
        pscData.setKind("legal-person-beneficial-owner");


        document.setData(pscData);

        mongoTemplate.save(document);
        assertThat(companyPscRepository.findById("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZVV")).isNotEmpty();
    }

    @When("a Get request is sent for {string} and {string} for Legal Person Beneficial Owner")
    public void aGetRequestIsSentForAndForLegalPersonBeneficialOwner(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri =
                "/company/{company_number}/persons-with-significant-control/legal-person-beneficial-owner/{notification_id}";
        ResponseEntity<LegalPersonBeneficialOwner> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, LegalPersonBeneficialOwner.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @And("the Get call response body should match {string} file for Legal Person Beneficial Owner")
    public void theGetCallResponseBodyShouldMatchFileForLegalPersonBeneficialOwner(String result) throws IOException {
        String data = FileCopyUtils.copyToString(new InputStreamReader(
                new FileInputStream("src/itest/resources/json/output/" + result + ".json")));
        LegalPersonBeneficialOwner expected = objectMapper.readValue(data, LegalPersonBeneficialOwner.class);

        LegalPersonBeneficialOwner actual = CucumberContext.CONTEXT.get("getResponseBody");

        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getCeasedOn()).isEqualTo(expected.getCeasedOn());
        assertThat(actual.getIsSanctioned()).isEqualTo(expected.getIsSanctioned());
    }

    @When("a Get request is sent for {string} and {string} without ERIC headers for Legal Person Beneficial Owner")
    public void aGetRequestIsSentForAndWithoutERICHeadersForLegalPersonBeneficialOwner(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);


        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri = "/company/{company_number}/persons-with-significant-control/legal-person-beneficial-owner/{notification_id}";
        ResponseEntity<LegalPersonBeneficialOwner> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, LegalPersonBeneficialOwner.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @When("a Get request has been sent for {string} and {string} for Legal Person Beneficial Owner")
    public void aGetRequestHasBeenSentForAndForLegalPersonBeneficialOwner(String companyNumber, String notification_id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        String uri =
                "/company/{company_number}/persons-with-significant-control/legal-person-beneficial-owner/{notification_id}";
        ResponseEntity<LegalPersonBeneficialOwner> response = restTemplate.exchange(uri,
                HttpMethod.GET, request, LegalPersonBeneficialOwner.class, companyNumber, notification_id);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }
}

