package uk.gov.companieshouse.pscdataapi.controller;

import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.psc.CorporateEntity;
import uk.gov.companieshouse.api.psc.CorporateEntityBeneficialOwner;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.Individual;
import uk.gov.companieshouse.api.psc.IndividualBeneficialOwner;
import uk.gov.companieshouse.api.psc.LegalPerson;
import uk.gov.companieshouse.api.psc.LegalPersonBeneficialOwner;
import uk.gov.companieshouse.api.psc.PscList;
import uk.gov.companieshouse.api.psc.SuperSecure;
import uk.gov.companieshouse.api.psc.SuperSecureBeneficialOwner;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscdataapi.models.PscDeleteRequest;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;

@RestController
public class CompanyPscController {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final CompanyPscService pscService;

    public CompanyPscController(CompanyPscService pscService) {
        this.pscService = pscService;
    }

    @PutMapping("/company/{company_number}/persons-with-significant-control/{notification_id}/full_record")
    public ResponseEntity<Void> submitPscData(@PathVariable("company_number") final String companyNumber,
            @PathVariable("notification_id") final String notificationId,
            @RequestBody FullRecordCompanyPSCApi request) {

        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);

        LOGGER.info("PUT request received", DataMapHolder.getLogMap());

        pscService.insertPscRecord(request);

        LOGGER.info("Successfully processed PUT request", DataMapHolder.getLogMap());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping(path = "/company/{company_number}/persons-with-significant-control/{notification_id}/full_record")
    public ResponseEntity<Void> deletePscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId,
            @RequestHeader("x-request-id") String contextId,
            @RequestHeader("x-kind") String kind,
            @RequestHeader("x-delta-at") String deltaAt) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);

        LOGGER.info("DELETE request received", DataMapHolder.getLogMap());

        pscService.deletePsc(PscDeleteRequest.builder()
                .companyNumber(companyNumber)
                .notificationId(notificationId)
                .contextId(contextId).kind(kind)
                .deltaAt(deltaAt)
                .build());

        LOGGER.info("Successfully processed DELETE request", DataMapHolder.getLogMap());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/company/{company_number}/persons-with-significant-control/individual/{notification_id}")
    public ResponseEntity<Individual> getIndividualPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId,
            @RequestParam(required = false, name = "register_view",
                    defaultValue = "false") Boolean registerView) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);

        LOGGER.info("Individual GET request received", DataMapHolder.getLogMap());
        Individual individual = pscService
                .getIndividualPsc(companyNumber, notificationId, registerView);

        LOGGER.info("Successfully processed individual GET request", DataMapHolder.getLogMap());
        return new ResponseEntity<>(individual, HttpStatus.OK);
    }

    @GetMapping("/company/{company_number}/persons-with-significant-control/individual-beneficial-owner/{notification_id}")
    public ResponseEntity<IndividualBeneficialOwner> getIndividualBeneficialOwnerPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId,
            @RequestParam(required = false, name = "register_view",
                    defaultValue = "false") Boolean registerView) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);

        LOGGER.info("Individual beneficial owner GET request received", DataMapHolder.getLogMap());
        IndividualBeneficialOwner individualBeneficialOwner =
                pscService.getIndividualBeneficialOwnerPsc(
                        companyNumber, notificationId, registerView);

        LOGGER.info("Successfully processed individual beneficial owner GET request", DataMapHolder.getLogMap());
        return new ResponseEntity<>(individualBeneficialOwner, HttpStatus.OK);
    }

    @GetMapping("/company/{company_number}/persons-with-significant-control/corporate-entity/{notification_id}")
    public ResponseEntity<CorporateEntity> getCorporateEntityPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);

        LOGGER.info("Corporate entity GET request received", DataMapHolder.getLogMap());
        CorporateEntity corporateEntity =
                pscService.getCorporateEntityPsc(companyNumber, notificationId);

        LOGGER.info("Successfully processed corporate entity GET request", DataMapHolder.getLogMap());
        return new ResponseEntity<>(corporateEntity, HttpStatus.OK);
    }

    @GetMapping("/company/{company_number}/persons-with-significant-control/corporate-entity-beneficial-owner/{notification_id}")
    public ResponseEntity<CorporateEntityBeneficialOwner> getCorporateEntityBeneficialOwnerPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);

        LOGGER.info("Corporate entity beneficial owner GET request received", DataMapHolder.getLogMap());
        CorporateEntityBeneficialOwner corporateEntityBeneficialOwner =
                pscService.getCorporateEntityBeneficialOwnerPsc(companyNumber, notificationId);

        LOGGER.info("Successfully processed corporate entity beneficial owner GET request", DataMapHolder.getLogMap());
        return new ResponseEntity<>(corporateEntityBeneficialOwner, HttpStatus.OK);
    }

    @GetMapping("/company/{company_number}/persons-with-significant-control/legal-person/{notification_id}")
    public ResponseEntity<LegalPerson> getLegalPersonPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);

        LOGGER.info("Legal person GET request received", DataMapHolder.getLogMap());
        LegalPerson legalPerson =
                pscService.getLegalPersonPsc(companyNumber, notificationId);

        LOGGER.info("Successfully processed legal person GET request", DataMapHolder.getLogMap());
        return new ResponseEntity<>(legalPerson, HttpStatus.OK);
    }

    @GetMapping("/company/{company_number}/persons-with-significant-control/legal-person-beneficial-owner/{notification_id}")
    public ResponseEntity<LegalPersonBeneficialOwner> getLegalPersonBeneficialOwnerPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);

        LOGGER.info("Legal person beneficial owner GET request received", DataMapHolder.getLogMap());
        LegalPersonBeneficialOwner legalPersonBeneficialOwner =
                pscService.getLegalPersonBeneficialOwnerPsc(companyNumber, notificationId);

        LOGGER.info("Successfully processed legal person beneficial owner GET request", DataMapHolder.getLogMap());
        return new ResponseEntity<>(legalPersonBeneficialOwner, HttpStatus.OK);
    }

    @GetMapping("/company/{company_number}/persons-with-significant-control/super-secure/{notification_id}")
    public ResponseEntity<SuperSecure> getSuperSecurePscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);

        LOGGER.info("Super secure GET request received", DataMapHolder.getLogMap());
        SuperSecure superSecure =
                pscService.getSuperSecurePsc(companyNumber, notificationId);

        LOGGER.info("Successfully processed super secure GET request", DataMapHolder.getLogMap());
        return new ResponseEntity<>(superSecure, HttpStatus.OK);
    }

    @GetMapping("/company/{company_number}/persons-with-significant-control/super-secure-beneficial-owner/{notification_id}")
    public ResponseEntity<SuperSecureBeneficialOwner> getSuperSecureBeneficialOwnerPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);

        LOGGER.info("Super secure beneficial owner GET request received", DataMapHolder.getLogMap());
        SuperSecureBeneficialOwner superSecureBeneficialOwner =
                pscService.getSuperSecureBeneficialOwnerPsc(companyNumber, notificationId);

        LOGGER.info("Successfully processed super secure beneficial owner GET request", DataMapHolder.getLogMap());
        return new ResponseEntity<>(superSecureBeneficialOwner, HttpStatus.OK);
    }

    @GetMapping("/company/{company_number}/persons-with-significant-control")
    public ResponseEntity<PscList> searchPscListSummary(
            @PathVariable("company_number") String companyNumber,
            @RequestParam(value = "items_per_page", required = false, defaultValue = "25") Integer itemsPerPage,
            @RequestParam(value = "start_index", required = false, defaultValue = "0") final Integer startIndex,
            @RequestParam(value = "register_view", required = false, defaultValue = "false") Boolean registerView) {
        DataMapHolder.get().companyNumber(companyNumber);

        itemsPerPage = Math.min(itemsPerPage, 100);

        LOGGER.info("PSC list GET request received", DataMapHolder.getLogMap());
        PscList pscList = pscService.retrievePscListSummaryFromDb(
                companyNumber, startIndex, registerView, itemsPerPage);

        LOGGER.info("Successfully processed PSC list GET request", DataMapHolder.getLogMap());
        return new ResponseEntity<>(pscList, HttpStatus.OK);
    }
}