package uk.gov.companieshouse.pscdataapi.controller;

import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;

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
import uk.gov.companieshouse.pscdataapi.models.PscDeleteRequest;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;

@RestController
@RequestMapping(path = "/company/{company_number}/persons-with-significant-control",
        produces = "application/json")
public class CompanyPscController {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

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
        DataMapHolder.get()
                .companyNumber(request.getExternalData().getCompanyNumber())
                .itemId(request.getExternalData().getInternalId());

        LOGGER.info("PUT request received", DataMapHolder.getLogMap());
        try {
            pscService.insertPscRecord(contextId, request);
            LOGGER.info("Successfully processed PUT request", DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ServiceUnavailableException ex) {
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        } catch (BadRequestException ex) {
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
        try {
            pscService.deletePsc(PscDeleteRequest.builder()
                    .companyNumber(companyNumber)
                    .notificationId(notificationId)
                    .contextId(contextId).kind(kind)
                    .deltaAt(deltaAt)
                    .build());

            LOGGER.info("Successfully processed DELETE request", DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ServiceUnavailableException ex) {
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

        LOGGER.info("Individual GET request received", DataMapHolder.getLogMap());
        try {
            Individual individual = pscService
                    .getIndividualPsc(companyNumber, notificationId, registerView);

            LOGGER.info("Successfully processed individual GET request", DataMapHolder.getLogMap());
            return new ResponseEntity<>(individual, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
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

        LOGGER.info("Individual beneficial owner GET request received", DataMapHolder.getLogMap());
        try {
            IndividualBeneficialOwner individualBeneficialOwner =
                    pscService.getIndividualBeneficialOwnerPsc(
                            companyNumber, notificationId, registerView);

            LOGGER.info("Successfully processed individual beneficial owner GET request", DataMapHolder.getLogMap());
            return new ResponseEntity<>(individualBeneficialOwner, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
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

        LOGGER.info("Corporate entity GET request received", DataMapHolder.getLogMap());
        try {
            CorporateEntity corporateEntity =
                    pscService.getCorporateEntityPsc(companyNumber, notificationId);

            LOGGER.info("Successfully processed corporate entity GET request", DataMapHolder.getLogMap());
            return new ResponseEntity<>(corporateEntity, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
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

        LOGGER.info("Corporate entity beneficial owner GET request received", DataMapHolder.getLogMap());
        try {
            CorporateEntityBeneficialOwner corporateEntityBeneficialOwner =
                    pscService.getCorporateEntityBeneficialOwnerPsc(companyNumber, notificationId);

            LOGGER.info("Successfully processed corporate entity beneficial owner GET request", DataMapHolder.getLogMap());
            return new ResponseEntity<>(corporateEntityBeneficialOwner, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
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

        LOGGER.info("Legal person GET request received", DataMapHolder.getLogMap());
        try {
            LegalPerson legalPerson =
                    pscService.getLegalPersonPsc(companyNumber, notificationId);

            LOGGER.info("Successfully processed legal person GET request", DataMapHolder.getLogMap());
            return new ResponseEntity<>(legalPerson, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
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

        LOGGER.info("Legal person beneficial owner GET request received", DataMapHolder.getLogMap());
        try {
            LegalPersonBeneficialOwner legalPersonBeneficialOwner =
                    pscService.getLegalPersonBeneficialOwnerPsc(companyNumber, notificationId);

            LOGGER.info("Successfully processed legal person beneficial owner GET request", DataMapHolder.getLogMap());
            return new ResponseEntity<>(legalPersonBeneficialOwner, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
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

        LOGGER.info("Super secure GET request received", DataMapHolder.getLogMap());
        try {
            SuperSecure superSecure =
                    pscService.getSuperSecurePsc(companyNumber, notificationId);

            LOGGER.info("Successfully processed super secure GET request", DataMapHolder.getLogMap());
            return new ResponseEntity<>(superSecure, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
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

        LOGGER.info("Super secure beneficial owner GET request received", DataMapHolder.getLogMap());
        try {
            SuperSecureBeneficialOwner superSecureBeneficialOwner =
                    pscService.getSuperSecureBeneficialOwnerPsc(companyNumber, notificationId);

            LOGGER.info("Successfully processed super secure beneficial owner GET request", DataMapHolder.getLogMap());
            return new ResponseEntity<>(superSecureBeneficialOwner, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
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
        DataMapHolder.get().companyNumber(companyNumber);

        itemsPerPage = Math.min(itemsPerPage, 100);

        LOGGER.info("PSC list GET request received", DataMapHolder.getLogMap());
        try {
            PscList pscList = pscService.retrievePscListSummaryFromDb(
                    companyNumber, startIndex, registerView, itemsPerPage);

            LOGGER.info("Successfully processed PSC list GET request", DataMapHolder.getLogMap());
            return new ResponseEntity<>(pscList, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}