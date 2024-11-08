package it.gov.pagopa.message.service;


import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.event.producer.MessageProducer;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.message.constants.MessageCoreConstants.MessageHeader.ERROR_MSG_HEADER_RETRY;

@Slf4j
@Service
public class MessageProducerServiceImpl implements MessageProducerService {

    private final MessageProducer messageProducer;

    public MessageProducerServiceImpl(MessageProducer messageProducer){
        this.messageProducer = messageProducer;
    }

    @Override
    public void enqueueMessage(MessageDTO messageDTO) {
        messageProducer.sendToMessageQueue(createMessage(messageDTO));
    }

    @NotNull
    private static Message<MessageDTO> createMessage(MessageDTO messageDTO) {
        return MessageBuilder
                .withPayload(messageDTO)
                .setHeader(ERROR_MSG_HEADER_RETRY, 0L)
                .build();

    }


}
