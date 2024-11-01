package uk.gov.companieshouse.pscdataapi.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
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
import uk.gov.companieshouse.pscdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.pscdataapi.exceptions.ResourceNotFoundException;
import uk.gov.companieshouse.pscdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;


@RestController
@RequestMapping(path = "/company/{company_number}/persons-with-significant-control",
        produces = "application/json")
public class CompanyPscController {

    private static final Logger LOGGER = LoggerFactory.getLogger("psc-data-api");
    private static final String GETTING_PSC_DATA_WITH_COMPANY_NUMBER = "Getting PSC data with company number %s";

    private final CompanyPscService pscService;

    public CompanyPscController(CompanyPscService pscService) {
        this.pscService = pscService;
    }

    /**
     * PUT endpoint for PSC record
     *
     * @param contextId contextId taken from header.
     * @param request   request payload.
     * @return response.
     */
    @PutMapping(path = "/{notification_id}/full_record", consumes = "application/json")
    public ResponseEntity<Void> submitPscData(@RequestHeader("x-request-id") String contextId,
                                              @RequestBody final FullRecordCompanyPSCApi request) {
        try {
            DataMapHolder.get()
                    .companyNumber(request.getExternalData().getCompanyNumber())
                    .itemId(request.getExternalData().getPscId());
        } catch (Exception ex) {
            throw new BadRequestException("Basic fields not provided");
        }
        LOGGER.infoContext(contextId, String.format("PUT request received with company number %s",
                request.getExternalData().getCompanyNumber()), DataMapHolder.getLogMap());
        try {
            pscService.insertPscRecord(contextId, request);
            LOGGER.infoContext(contextId, "Successfully inserted PSC",
                    DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ServiceUnavailableException ex) {
            LOGGER.errorContext(contextId, ex, DataMapHolder.getLogMap());
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        } catch (BadRequestException ex) {
            LOGGER.errorContext(contextId, ex, DataMapHolder.getLogMap());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Delete the data object for a company profile number.
     *
     * @param companyNumber The number of the company
     * @return ResponseEntity
     */
    @DeleteMapping(path = "/{notification_id}/full_record")
    // TODO Add header for X-PSC-KIND
    public ResponseEntity<Void> deletePscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId,
            @RequestHeader("x-request-id") String contextId) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);
        LOGGER.info(String.format("Deleting PSC data with company number %s", companyNumber),
                DataMapHolder.getLogMap());
        try {
            // TODO Add X-PSC-KIND to arguments
            pscService.deletePsc(companyNumber, notificationId, contextId);
            LOGGER.info(String.format("Successfully deleted PSC with company number %s",
                    companyNumber), DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ServiceUnavailableException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get the data object for a company profile number for Individual PSC.
     *
     * @param companyNumber The number of the company
     * @return ResponseEntity
     */
    @GetMapping("/individual/{notification_id}")
    public ResponseEntity<Individual> getIndividualPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId,
            @RequestParam(required = false, name = "register_view",
                    defaultValue = "false") Boolean registerView) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);
        LOGGER.info(String.format(GETTING_PSC_DATA_WITH_COMPANY_NUMBER, companyNumber),
                DataMapHolder.getLogMap());
        try {
            Individual individual = pscService
                    .getIndividualPsc(companyNumber, notificationId, registerView);
            return new ResponseEntity<>(individual, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get the data object for a company profile number for Individual Beneficial Owner PSC.
     *
     * @param companyNumber The number of the company
     * @return ResponseEntity
     */
    @GetMapping("/individual-beneficial-owner/{notification_id}")
    public ResponseEntity<IndividualBeneficialOwner> getIndividualBeneficialOwnerPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId,
            @RequestParam(required = false, name = "register_view",
                    defaultValue = "false") Boolean registerView) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);
        LOGGER.info(String.format(GETTING_PSC_DATA_WITH_COMPANY_NUMBER, companyNumber),
                DataMapHolder.getLogMap());
        try {
            IndividualBeneficialOwner individualBeneficialOwner =
                    pscService.getIndividualBeneficialOwnerPsc(
                            companyNumber, notificationId, registerView);
            return new ResponseEntity<>(individualBeneficialOwner, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get the data object for a company profile number for Corporate Entity PSC.
     *
     * @param companyNumber The number of the company
     * @return ResponseEntity
     */
    @GetMapping("/corporate-entity/{notification_id}")
    public ResponseEntity<CorporateEntity> getCorporateEntityPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);
        LOGGER.info(String.format(GETTING_PSC_DATA_WITH_COMPANY_NUMBER, companyNumber),
                DataMapHolder.getLogMap());
        try {
            CorporateEntity corporateEntity =
                    pscService.getCorporateEntityPsc(companyNumber, notificationId);
            return new ResponseEntity<>(corporateEntity, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get the data object for a company profile number for Corporate Entity Beneficial Owner PSC.
     *
     * @param companyNumber The number of the company
     * @return ResponseEntity
     */
    @GetMapping("/corporate-entity-beneficial-owner/{notification_id}")
    public ResponseEntity<CorporateEntityBeneficialOwner> getCorporateEntityBeneficialOwnerPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);
        LOGGER.info(String.format(GETTING_PSC_DATA_WITH_COMPANY_NUMBER, companyNumber),
                DataMapHolder.getLogMap());
        try {
            CorporateEntityBeneficialOwner corporateEntityBeneficialOwner =
                    pscService.getCorporateEntityBeneficialOwnerPsc(companyNumber, notificationId);
            return new ResponseEntity<>(corporateEntityBeneficialOwner, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get the data object for a company profile number for Legal Person PSC.
     *
     * @param companyNumber The number of the company
     * @return ResponseEntity
     */
    @GetMapping("/legal-person/{notification_id}")
    public ResponseEntity<LegalPerson> getLegalPersonPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);
        LOGGER.info(String.format(GETTING_PSC_DATA_WITH_COMPANY_NUMBER, companyNumber),
                DataMapHolder.getLogMap());
        try {
            LegalPerson legalPerson =
                    pscService.getLegalPersonPsc(companyNumber, notificationId);
            return new ResponseEntity<>(legalPerson, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get the data object for a company profile number for Legal Person Beneficial Owner PSC.
     *
     * @param companyNumber The number of the company
     * @return ResponseEntity
     */
    @GetMapping("/legal-person-beneficial-owner/{notification_id}")
    public ResponseEntity<LegalPersonBeneficialOwner> getLegalPersonBeneficialOwnerPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);
        LOGGER.info(String.format(GETTING_PSC_DATA_WITH_COMPANY_NUMBER, companyNumber),
                DataMapHolder.getLogMap());
        try {
            LegalPersonBeneficialOwner legalPersonBeneficialOwner =
                    pscService.getLegalPersonBeneficialOwnerPsc(companyNumber, notificationId);
            return new ResponseEntity<>(legalPersonBeneficialOwner, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get the data object for a company profile number for Super Secure PSC.
     *
     * @param companyNumber The number of the company
     * @return ResponseEntity
     */
    @GetMapping("/super-secure/{notification_id}")
    public ResponseEntity<SuperSecure> getSuperSecurePscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);
        LOGGER.info(String.format(GETTING_PSC_DATA_WITH_COMPANY_NUMBER, companyNumber),
                DataMapHolder.getLogMap());
        try {
            SuperSecure superSecure =
                    pscService.getSuperSecurePsc(companyNumber, notificationId);
            return new ResponseEntity<>(superSecure, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get the data object for a company profile number for Super Secure Beneficial Owner PSC.
     *
     * @param companyNumber The number of the company
     * @return ResponseEntity
     */
    @GetMapping("/super-secure-beneficial-owner/{notification_id}")
    public ResponseEntity<SuperSecureBeneficialOwner> getSuperSecureBeneficialOwnerPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);
        LOGGER.info(String.format(GETTING_PSC_DATA_WITH_COMPANY_NUMBER, companyNumber),
                DataMapHolder.getLogMap());
        try {
            SuperSecureBeneficialOwner superSecureBeneficialOwner =
                    pscService.getSuperSecureBeneficialOwnerPsc(companyNumber, notificationId);
            return new ResponseEntity<>(superSecureBeneficialOwner, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get the PSC List Summary for a company profile number.
     *
     * @param companyNumber The number of the company
     * @return ResponseEntity
     */
    @GetMapping("")
    public ResponseEntity<PscList> searchPscListSummary(
            @PathVariable("company_number") String companyNumber,
            @RequestParam(value = "items_per_page",
                    required = false, defaultValue = "25") Integer itemsPerPage,
            @RequestParam(value = "start_index",
                    required = false, defaultValue = "0") final Integer startIndex,
            @RequestParam(
                    value = "register_view", required = false) Boolean registerView) {
        DataMapHolder.get()
                .companyNumber(companyNumber);
        itemsPerPage = Math.min(itemsPerPage, 100);
        try {
            LOGGER.info(String.format("Retrieving psc list data for company number %s",
                    companyNumber), DataMapHolder.getLogMap());
            PscList pscList = pscService.retrievePscListSummaryFromDb(
                    companyNumber, startIndex, registerView, itemsPerPage);
            return new ResponseEntity<>(pscList, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

}
