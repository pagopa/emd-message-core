package it.gov.pagopa.message.dto;

import it.gov.pagopa.message.model.Message;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static it.gov.pagopa.common.utils.CommonUtilities.createSHA256;

@Service
public class MessageMapperDTOToObject {

    public Message map(MessageDTO messageDTO, String entityId){
        return Message.builder()
                .messageId(messageDTO.getMessageId())
                .hashedFiscalCode(createSHA256(messageDTO.getRecipientId()))
                .triggerDateTime(messageDTO.getTriggerDateTime())
                .messageUrl(messageDTO.getMessageUrl())
                .content(messageDTO.getContent())
                .originId(messageDTO.getOriginId())
                .elaborationDateTime(LocalDateTime.now())
                .entityId(entityId)
                .build();
    }
}
