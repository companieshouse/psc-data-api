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
import static org.assertj.core.api.InstanceOfAssertFactories.LOCAL_DATE;
import static uk.gov.companieshouse.pscdataapi.config.AbstractMongoConfig.mongoDBContainer;

import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.company.CompanyProfile;
import uk.gov.companieshouse.api.company.Data;
import uk.gov.companieshouse.api.model.CompanyProfileDocument;
import uk.gov.companieshouse.api.psc.Individual;
import uk.gov.companieshouse.api.psc.StatementList;
import uk.gov.companieshouse.pscdataapi.config.CucumberContext;
import uk.gov.companieshouse.pscdataapi.models.*;
import uk.gov.companieshouse.pscdataapi.transform.CompanyPscTransformer;
import uk.gov.companieshouse.pscdataapi.util.FileReaderUtil;
import uk.gov.companieshouse.pscdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.repository.CompanyPscRepository;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;
import uk.gov.companieshouse.pscdataapi.util.FileReaderUtil;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.*;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.pscdataapi.config.AbstractMongoConfig.mongoDBContainer;

public class PscDataSteps {

    private static WireMockServer wireMockServer;
    @Value("${wiremock.server.port}")
    private String port;
    private String contextId;

    //@Autowired
    //private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CompanyPscRepository companyPscRepository;

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

    @Given("the database is down")
    public void the_psc_db_is_down() {
        mongoDBContainer.stop();
    }

    @Then("nothing is persisted in the database")
    public void nothing_persisted_to_database() {
        List<PscDocument> pscDocs = companyPscRepository.findAll();
        Assertions.assertThat(pscDocs).hasSize(0);
    }

    @Then("the CHS Kafka API is not invoked")
    public void chs_kafka_api_not_invoked() throws IOException {
        verify(companyPscService, times(0)).invokeChsKafkaApi(any(), any(), any());
    }

    @When("CHS kafka API service is unavailable")
    public void chs_kafka_service_unavailable() throws IOException {

        doThrow(ServiceUnavailableException.class)
                .when(companyPscService).invokeChsKafkaApiWithDeleteEvent(any(), any(), any());
    }

    private void configureWireMock() {
        wireMockServer = new WireMockServer(Integer.parseInt(port));
        wireMockServer.start();
        configureFor("localhost", Integer.parseInt(port));
    }

    private void stubPutStatement(String companyNumber, String notificationId, int responseCode) {
        stubFor(put(urlEqualTo(
                "/company/" + companyNumber + "/persons-with-significant-control/" + notificationId + "/full_record"))
                .willReturn(aResponse().withStatus(responseCode)));
    }

//    @When("^the consumer receives a message but the data api returns a (\\d*)$")
//    public void theConsumerReceivesMessageButDataApiReturns(String companyNumber, String notificationId, int responseCode) throws Exception{
//        configureWireMock();
//        stubPutStatement(companyNumber,notificationId,responseCode);
//        ChsDelta delta = new ChsDelta(TestData.getStatementDelta(), 1, "1", false);
//        kafkaTemplate.send(mainTopic, delta);
//
//        countDown();
//    }




//    @When("I send a PUT request")
//    public void i_send_psc_statement_put_request(String dataFile, String notificationId) throws IOException {
//        String data = FileReaderUtil.readFile("src/itest/resources/json/input/" + dataFile + ".json");
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//
//        this.contextId = "5234234234";
//        CucumberContext.CONTEXT.set("contextId", this.contextId);
//        headers.set("x-request-id", this.contextId);
//        headers.set("ERIC-Identity", "TEST-IDENTITY");
//        headers.set("ERIC-Identity-Type", "key");
//        headers.set("ERIC-Authorised-Key-Roles", "*");
//
//        HttpEntity request = new HttpEntity(data, headers);
//        String uri = "/company/{company_number}/persons-with-significant-control-statements/{notfication_id}/full_record";
//        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class, COMPANY_NUMBER, notificationId);
//
//        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
//    }

    @When("I send a PUT request with payload {string} file for company number {string} with notification id  {string}")
    public void i_send_psc_record_put_request_with_payload(String dataFile, String companyNumber, String notificationId) {
        String data = FileReaderUtil.readFile("src/itest/resources/json/input/" + dataFile + ".json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

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

    @And("a PSC exists for {string}")
    public void aPSCExistsFor(String companyNumber) throws JsonProcessingException {
        String pscDataFile = FileReaderUtil.readFile("src/itest/resources/json/input/"+companyNumber+".json");
        PscData pscData = objectMapper.readValue(pscDataFile, PscData.class);
        PscSensitiveData pscSensitiveData = objectMapper.readValue(pscDataFile, PscSensitiveData.class);
        PscDocument document = new PscDocument();


        document.setId(NOTIFICATION_ID);
        document.setCompanyNumber(companyNumber);
        document.setPscId("ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ");
        document.setDeltaAt(String.valueOf(LocalDate.now()));
        pscData.setEtag("string");
        pscData.setCeasedOn(LocalDate.now());
        pscData.setKind("individual-person-with-significant-control");
        pscData.setCountryOfResidence("United Kingdom");
        DateOfBirth dateOfBirth = new DateOfBirth();
        dateOfBirth.setDay(21);
        dateOfBirth.setMonth(10);
        dateOfBirth.setYear(1995);
        pscSensitiveData.setDateOfBirth(dateOfBirth);
        pscData.setName("34777772");
        NameElements nameElements = new NameElements();
        nameElements.setTitle("Mr");
        nameElements.setForename("PHIL");
        nameElements.setMiddleName("tom");
        nameElements.setSurname("JONES");
        pscData.setNameElements(nameElements);
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
        document.setSensitiveData(pscSensitiveData);

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
        assertThat(actual.getDateOfBirth()).isEqualTo(expected.getDateOfBirth());
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
}
