package it.gov.pagopa.message.core.faker;

import it.gov.pagopa.message.core.dto.Outcome;
import it.gov.pagopa.message.core.enums.OutcomeStatus;

public class OutcomeFaker {

    private OutcomeFaker(){}
    public static Outcome mockInstance(Boolean bias) {
        return Outcome.builder()
                .outcomeStatus(bias ? OutcomeStatus.OK : OutcomeStatus.NO_CHANNELS_ENABLED)
                .build();

    }
}
