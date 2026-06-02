package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.api.psc_notifications.NotificationList;
import uk.gov.companieshouse.pscdataapi.PscDataApiApplication;
import uk.gov.companieshouse.pscdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;

import static uk.gov.companieshouse.pscdataapi.interceptor.AuthenticationHelperImpl.ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER;

@Controller
public class PscNotificationsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PscDataApiApplication.APPLICATION_NAME_SPACE);

    private final PscNotificationsService service;

    PscNotificationsController(PscNotificationsService service) {
        this.service = service;
    }

    @GetMapping(path = "/persons-with-significant-control/{psc_id}/notifications")
    public ResponseEntity<NotificationList> getPscNotifications(
            @PathVariable("psc_id") String pscId,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "start_index", required = false) Integer startIndex,
            @RequestParam(value = "items_per_page", required = false) Integer itemsPerPage,
            @RequestParam(value = ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER, required = false) String authPrivileges) {
        try {

            LOGGER.info("Fetching psc notifications", DataMapHolder.getLogMap());

            PscNotificationsRequest request = PscNotificationsRequest.builder()
                    .pscId(pscId)
                    .filter(filter)
                    .startIndex(startIndex)
                    .itemsPerPage(itemsPerPage)
                    .authPrivileges(authPrivileges)
                    .build();
            return service.getPscNotifications(request)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        LOGGER.info(String.format("No notifications found for psc ID: %s", pscId),
                                DataMapHolder.getLogMap());
                        return ResponseEntity.notFound().build();
                    });
        } catch (BadRequestException ex) {
            LOGGER.info(String.format("Invalid filter parameter supplied: %s, psc ID: %s", filter, pscId),
                    DataMapHolder.getLogMap());
            return ResponseEntity.badRequest().build();
        }
    }
}
