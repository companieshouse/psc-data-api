package uk.gov.companieshouse.pscdataapi.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.*;
import uk.gov.companieshouse.pscdataapi.config.CucumberContext;
import uk.gov.companieshouse.pscdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.repository.CompanyPscRepository;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;
import uk.gov.companieshouse.pscdataapi.util.FileReaderUtil;

import java.io.IOException;
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

    private final String COMPANY_NUMBER = "34777772";

    @Autowired
    private CompanyPscService companyPscService;


    @Before
    public void dbCleanUp(){
        if (!mongoDBContainer.isRunning()) {
            mongoDBContainer.start();
        }
        companyPscRepository.deleteAll();
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
        String uri = "/company/{company_number}/persons-with-significant-control/{notfication_id}/full_record";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class, COMPANY_NUMBER, notificationId);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @Then("a record exists with id {string}")
    public void statement_exists(String statementId) {
        Assertions.assertThat(companyPscRepository.existsById(statementId)).isTrue();
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

    @After
    public void dbStop(){
        mongoDBContainer.stop();
    }
}
