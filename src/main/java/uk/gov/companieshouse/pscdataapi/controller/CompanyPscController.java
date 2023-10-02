package uk.gov.companieshouse.pscdataapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.api.psc.CorporateEntity;
import uk.gov.companieshouse.api.psc.CorporateEntityBeneficialOwner;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.Individual;
import uk.gov.companieshouse.api.psc.IndividualBeneficialOwner;
import uk.gov.companieshouse.api.psc.LegalPerson;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.pscdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;


@RestController
@RequestMapping(
        path = "/company/{company_number}/persons-with-significant-control/",
        produces = "application/json")
public class CompanyPscController {

    @Autowired
    CompanyPscService pscService;

    private static final Logger LOGGER = LoggerFactory.getLogger("psc-data-api");

    /**
     * Get the data object for given company profile number.
     *
     * @param companyNumber The number of the company
     * @return ResponseEntity
     */
    @GetMapping("corporate-entity/{notification_id}")
    public ResponseEntity<CorporateEntity> getCorporateEntityPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        LOGGER.info(String.format("Getting PSC data with company number %s", companyNumber));
        try {
            LOGGER.info(String.format("Retrieving PSC with company number %s", companyNumber));
            CorporateEntity corporateEntity =
                    pscService.getCorporateEntityPsc(companyNumber, notificationId);
            return new ResponseEntity<>(corporateEntity, HttpStatus.OK);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            LOGGER.error(resourceNotFoundException.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException exception) {
            LOGGER.error(exception.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * PUT endpoint for PSC record
     * @param contextId context Id taken from header.
     * @param request request payload.
     * @return response.
     * */
    @PutMapping(path = "{notification_id}/full_record", consumes = "application/json")
    public ResponseEntity<Void> submitPscData(@RequestHeader("x-request-id") String contextId,
                                              @RequestBody final FullRecordCompanyPSCApi request) {
        try {
            LOGGER.info("Payload received, inserting record...");
            pscService.insertPscRecord(contextId, request);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ServiceUnavailableException exception) {
            LOGGER.info(exception.getMessage());
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        } catch (BadRequestException exception) {
            LOGGER.info(exception.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Delete the data object for given company profile number.
     *
     * @param companyNumber The number of the company
     * @return ResponseEntity
     */
    @DeleteMapping(path = "{notification_id}/full_record")
    public ResponseEntity<Void> deletePscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        LOGGER.info(String.format("Deleting PSC data with company number %s", companyNumber));
        try {
            pscService.deletePsc(companyNumber,notificationId);
            LOGGER.info(String.format(
                    "Successfully deleted PSC with company number %s",
                    companyNumber));
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (ResourceNotFoundException resourceNotFoundException) {
            LOGGER.error(resourceNotFoundException.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ServiceUnavailableException exception) {
            LOGGER.error(exception.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get the data object for given company profile number.
     *
     * @param companyNumber The number of the company
     * @return ResponseEntity
     */
    @GetMapping("individual/{notification_id}")
    public ResponseEntity<Individual> getIndividualPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        LOGGER.info(String.format("Getting PSC data with company number %s", companyNumber));
        try {

            LOGGER.info(String.format(
                    "Retrieving PSC with company number %s",
                    companyNumber));
            Individual individual = pscService.getIndividualPsc(companyNumber , notificationId);
            return new ResponseEntity<>(individual, HttpStatus.OK);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            LOGGER.error(resourceNotFoundException.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException exception) {
            LOGGER.error(exception.getMessage());
            return ResponseEntity.internalServerError().build();
        }

    }

    /**
     * Get the data object for given company profile number.
     *
     * @param companyNumber The number of the company
     * @return ResponseEntity
     */
    @GetMapping("individual-beneficial-owner/{notification_id}")
    public ResponseEntity<IndividualBeneficialOwner> getIndividualBeneficialOwnerPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        LOGGER.info(String.format("Getting PSC data with company number %s", companyNumber));
        try {
            LOGGER.info(String.format("Retrieving PSC with company number %s", companyNumber));
            IndividualBeneficialOwner individualBeneficialOwner =
                    pscService.getIndividualBeneficialOwnerPsc(companyNumber, notificationId);
            return new ResponseEntity<>(individualBeneficialOwner, HttpStatus.OK);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            LOGGER.error(resourceNotFoundException.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException exception) {
            LOGGER.error(exception.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get the data object for given company profile number.
     *
     * @param companyNumber The number of the company
     * @return ResponseEntity
     */
    @GetMapping("corporate-entity-beneficial-owner/{notification_id}")
    public ResponseEntity<CorporateEntityBeneficialOwner> getCorporateEntityBeneficialOwnerPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        LOGGER.info(String.format("Getting PSC data with company number %s", companyNumber));
        try {
            LOGGER.info(String.format("Retrieving PSC with company number %s", companyNumber));
            CorporateEntityBeneficialOwner corporateEntityBeneficialOwner =
                    pscService.getCorporateEntityBeneficialOwnerPsc(companyNumber, notificationId);
            return new ResponseEntity<>(corporateEntityBeneficialOwner, HttpStatus.OK);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            LOGGER.error(resourceNotFoundException.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException exception) {
            LOGGER.error(exception.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get the data object for given company profile number.
     *
     * @param companyNumber The number of the company
     * @return ResponseEntity
     */
    @GetMapping("legal-person/{notification_id}")
    public ResponseEntity<LegalPerson> getLegalPersonPscData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        LOGGER.info(String.format("Getting PSC data with company number %s", companyNumber));
        try {
            LOGGER.info(String.format("Retrieving PSC with company number %s", companyNumber));
            LegalPerson legalPerson =
                    pscService.getLegalPersonPsc(companyNumber, notificationId);
            return new ResponseEntity<>(legalPerson, HttpStatus.OK);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            LOGGER.error(resourceNotFoundException.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException exception) {
            LOGGER.error(exception.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

}
