package it.gov.pagopa.message.core.service;


import it.gov.pagopa.message.core.dto.MessageDTO;
import org.springframework.messaging.Message;


public interface MessageErrorConsumerService {
    void processCommand(Message<MessageDTO> messageDTO);
}
