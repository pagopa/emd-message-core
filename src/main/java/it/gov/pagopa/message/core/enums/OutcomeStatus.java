package it.gov.pagopa.message.core.enums;

import lombok.Getter;

@Getter
public enum OutcomeStatus {
    OK("OK"),
    NO_CHANNELS_ENABLED("NO_CHANNELS_ENABLED");

    private final String status;

    OutcomeStatus(String status) {
        this.status = status;
    }

}
