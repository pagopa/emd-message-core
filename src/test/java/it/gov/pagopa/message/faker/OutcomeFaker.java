package it.gov.pagopa.message.faker;

import it.gov.pagopa.message.enums.OutcomeStatus;
import it.gov.pagopa.message.model.Outcome;

public class OutcomeFaker {

    private OutcomeFaker(){}
    public static Outcome mockInstance(Boolean bias) {
        return Outcome.builder()
                .outcomeStatus(bias ? OutcomeStatus.OK : OutcomeStatus.NO_CHANNELS_ENABLED)
                .build();

    }
}
