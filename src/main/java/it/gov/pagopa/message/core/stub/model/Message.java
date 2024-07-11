package it.gov.pagopa.message.core.stub.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;


@Data
@Builder
public class Message {
    private String messageId;
    private String hashedFiscalCode;
    private LocalDateTime originalRegistrationDate;
    private String originalSender;
    private String originId;
    private String messageUrl;
    private String messageContent;

    private LocalDateTime elaborationDateTime;
}
