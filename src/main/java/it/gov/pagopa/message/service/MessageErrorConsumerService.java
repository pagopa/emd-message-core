package it.gov.pagopa.message.service;


import it.gov.pagopa.message.dto.MessageDTO;
import org.springframework.messaging.Message;

public interface MessageErrorConsumerService {
    void processCommand(Message<MessageDTO> messageDTO);
}
