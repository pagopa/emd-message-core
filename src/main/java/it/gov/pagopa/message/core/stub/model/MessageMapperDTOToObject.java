package it.gov.pagopa.message.core.stub.model;

import it.gov.pagopa.message.core.dto.MessageDTO;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static it.gov.pagopa.common.utils.Utils.createSHA256;

@Service
public class MessageMapperDTOToObject {

    public Message messageObjectMapper(MessageDTO messageDTO){
        return Message.builder()
                .messageId(messageDTO.getMessageId())
                .hashedFiscalCode(createSHA256(messageDTO.getRecipientId()))
                .originalRegistrationDate(messageDTO.getTriggerDateTime())
                .originalSender(messageDTO.getSenderDescription())
                .originId(messageDTO.getOriginId())
                .messageUrl(messageDTO.getMessageUrl())
                .messageContent(messageDTO.getMessage())
                .elaborationDateTime(LocalDateTime.now())
                .build();
    }
}
