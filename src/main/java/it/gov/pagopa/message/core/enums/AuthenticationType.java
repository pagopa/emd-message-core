package it.gov.pagopa.message.core.enums;

import lombok.Getter;

@Getter
public enum AuthenticationType {
    OUATH2("OUATH2");


    private final String status;

    AuthenticationType(String status) {
        this.status = status;
    }

}
