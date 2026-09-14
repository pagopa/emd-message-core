package it.gov.pagopa.message.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import it.gov.pagopa.message.dto.BaseMessage;
import it.gov.pagopa.message.enums.Channel;
import it.gov.pagopa.message.enums.MessageState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Message extends BaseMessage {

    @JsonAlias("_id")
    private String id;
    private String entityId;
    private Channel channel;
    private String messageRegistrationDate;
    private MessageState messageState;

}