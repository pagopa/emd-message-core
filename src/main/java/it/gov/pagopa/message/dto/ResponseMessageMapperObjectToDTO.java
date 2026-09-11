package it.gov.pagopa.message.dto;

import org.springframework.stereotype.Service;

import it.gov.pagopa.message.model.Message;

@Service
public class ResponseMessageMapperObjectToDTO {

    public ResponseMessageDTO map(Message message){
        return ResponseMessageDTO.builder()
                .messageId(message.getMessageId())
                .recipientId(message.getRecipientId())
                .entityId(message.getEntityId())
                .triggerDateTime(message.getTriggerDateTime())
                .senderDescription(message.getSenderDescription())
                .messageUrl(message.getMessageUrl())
                .originId(message.getOriginId())
                .title(message.getTitle())
                .content(message.getContent())
                .associatedPayment(message.getAssociatedPayment())
                .idPsp(message.getIdPsp())
                .channel(message.getChannel())
                .analogSchedulingDate(message.getAnalogSchedulingDate())
                .workflowType(message.getWorkflowType())
                .messageState(message.getMessageState())
                .messageRegistrationDate(message.getMessageRegistrationDate())
                .build();

    }

}