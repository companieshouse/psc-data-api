package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.PscDataApiApplication;

@Controller
public class PscNotificationsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PscDataApiApplication.APPLICATION_NAME_SPACE);

    private final PscNotificationsService service;

    PscNotificationsController(PscNotificationsService service) {
        this.service = service;
    }

    @GetMapping(path = "/persons-with-significant-control/{psc_id}/notifications")
    public ResponseEntity<NotificationList> getPscNotifications(){

    }
}
