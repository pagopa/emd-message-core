package it.gov.pagopa.message.core.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import it.gov.pagopa.message.core.enums.MessageStatus;
import lombok.Data;
import java.time.LocalDateTime;


@Data
public class Message {
    @JsonAlias("_id")
    private String id;
    private String originalSender;
    private String sender;
    private  String fiscalCode;
    private LocalDateTime originalRegistrationDate;
    private LocalDateTime elaborationDateTime;
    private MessageStatus messageStatus;
}
