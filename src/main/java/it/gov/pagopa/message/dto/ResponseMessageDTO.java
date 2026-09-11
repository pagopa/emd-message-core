package it.gov.pagopa.message.dto;

import it.gov.pagopa.message.enums.Channel;
import it.gov.pagopa.message.enums.MessageState;
import it.gov.pagopa.message.enums.WorkflowType;
import it.gov.pagopa.message.validator.ValidAnalogScheduling;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO with all the information needed to send a notification message
 * through TTP applications to citizens.
 */
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ValidAnalogScheduling
public class ResponseMessageDTO {


    private String messageId;

    private String recipientId;

    private String entityId;

    private String triggerDateTime;

    private String senderDescription;

    private String messageUrl;

    private String originId;

    private String title;

    private String content;

    private Boolean associatedPayment;

    private String analogSchedulingDate;

    private Channel channel;

    private WorkflowType workflowType;

    private String idPsp;
    
    private String messageRegistrationDate;

    private MessageState messageState;
}