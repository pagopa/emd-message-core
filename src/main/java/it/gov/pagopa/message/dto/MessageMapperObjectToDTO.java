package it.gov.pagopa.message.dto;

import java.util.Set;

import org.springframework.stereotype.Service;

import it.gov.pagopa.message.constants.MessageCoreConstants.SearchFields;
import it.gov.pagopa.message.model.Message;

@Service
public class MessageMapperObjectToDTO {

    public MessageDTO map(Message message){
        return MessageDTO.builder()
                .messageId(message.getMessageId())
                .recipientId(message.getRecipientId())
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
                .build();

    }

    /**
     * Maps a {@link Message} domain object to a {@link MessageDTO}, populating only
     * the requested {@code fields}. 
     * Il {@code messageId} viene sempre incluso come identificatore univoco.
     *
     * @param message the domain entity to selectively map
     * @param fields  the set of field names to populate
     * @return a new {@link MessageDTO} instance containing only the requested fields
     */
    public MessageDTO map(Message message, Set<String> fields) {
        MessageDTO.MessageDTOBuilder builder = MessageDTO.builder()
            .messageId(message.getMessageId());

        if (fields.contains(SearchFields.RECIPIENT_ID)) builder.recipientId(message.getRecipientId());
        if (fields.contains(SearchFields.TRIGGER_DATE_TIME)) builder.triggerDateTime(message.getTriggerDateTime());
        if (fields.contains(SearchFields.SENDER_DESCRIPTION)) builder.senderDescription(message.getSenderDescription());
        if (fields.contains(SearchFields.MESSAGE_URL)) builder.messageUrl(message.getMessageUrl());
        if (fields.contains(SearchFields.ORIGIN_ID)) builder.originId(message.getOriginId());
        if (fields.contains(SearchFields.TITLE)) builder.title(message.getTitle());
        if (fields.contains(SearchFields.CONTENT)) builder.content(message.getContent());
        if (fields.contains(SearchFields.ASSOCIATED_PAYMENT)) builder.associatedPayment(message.getAssociatedPayment());
        if (fields.contains(SearchFields.ID_PSP)) builder.idPsp(message.getIdPsp());
        if (fields.contains(SearchFields.CHANNEL)) builder.channel(message.getChannel());
        if (fields.contains(SearchFields.ANALOG_SCHEDULING_DATE)) builder.analogSchedulingDate(message.getAnalogSchedulingDate());
        if (fields.contains(SearchFields.WORKFLOW_TYPE)) builder.workflowType(message.getWorkflowType());
        if (fields.contains(SearchFields.ID_PSP)) builder.idPsp(message.getIdPsp());

        return builder.build();
    }
}
