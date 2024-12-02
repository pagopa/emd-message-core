package it.gov.pagopa.message.enums;

import lombok.Getter;

@Getter
public enum Channel {

        SEND("SEND");

    private final String status;

    Channel(String status) {
        this.status = status;
    }

}
