package it.gov.pagopa.message.service;


import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.event.producer.MessageProducer;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MessageProducerServiceImpl implements MessageProducerService {

    private final MessageProducer messageProducer;

    public MessageProducerServiceImpl(MessageProducer messageProducer){
        this.messageProducer = messageProducer;
    }

    @Override
    public void enqueueMessage(MessageDTO messageDTO) {
        Message<MessageDTO> message = createMessage(messageDTO);
        messageProducer.sendToMessageQueue(message);
    }

    @NotNull
    private static Message<MessageDTO> createMessage(MessageDTO messageDTO) {
        return MessageBuilder
                .withPayload(messageDTO)
                .build();

    }


}
