package it.gov.pagopa.message.core.enums;

import lombok.Getter;

@Getter
public enum MessageStatus {
    SEND("SEND"),
    READ("READ"),
    EXPIRED("EXPIRED");

    private final String status;

    MessageStatus(String status) {
        this.status = status;
    }

}
