package uk.gov.companieshouse.pscdataapi.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.psc.VerificationState;

@Service
public class VerificationStateService {

    public VerificationState retrieveVerificationState(Long internalId) {

        // Using the internalId, this will call the Oracle API to retrieve the verification state in CHIPS
        VerificationState verificationState = new VerificationState();
        verificationState.setVerificationStatus(VerificationState.VerificationStatusEnum.VERIFIED);
        LocalDate date = LocalDate.now();
        verificationState.setVerificationStartDate(date.minusDays(7));
        verificationState.setVerificationStatementDueDate(date.plusDays(7));

        return  verificationState;
    }
}
