package it.gov.pagopa.message.model;

import it.gov.pagopa.message.dto.MessageDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static it.gov.pagopa.common.utils.Utils.createSHA256;

@Service
public class MessageMapperObjectToDTO {

    public MessageDTO map(Message message,String fiscalCode){
        return MessageDTO.builder()
                .recipientId(fiscalCode)
                .messageId(message.getMessageId())
                .senderDescription(message.getSenderDescription())
                .entityId(message.getEntityId())
                .triggerDateTime(message.getTriggerDateTime())
                .messageUrl(message.getMessageUrl())
                .content(message.getContent())
                .originId(message.getOriginId())
                .build();

    }
}
