package it.gov.pagopa.message.service;


import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.event.producer.MessageProducer;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static it.gov.pagopa.message.constants.MessageCoreConstants.MessageHeader.ERROR_MSG_HEADER_RETRY;

@Slf4j
@Service
public class MessageProducerServiceImpl implements MessageProducerService {

    private final MessageProducer messageProducer;

    public MessageProducerServiceImpl(MessageProducer messageProducer){
        this.messageProducer = messageProducer;
    }

    @Override
    public Mono<Void> enqueueMessage(MessageDTO messageDTO, String messageId) {
        log.info("[MESSAGE-PRODUCER][ENQUEUE] Enqueuing message with ID: {}", messageId);

        return Mono.fromRunnable(() -> {
                Message<MessageDTO> message = createMessage(messageDTO, messageId);
                log.info("[MESSAGE-PRODUCER][ENQUEUE] Message with ID: {} successfully created. Sending to message queue.", messageId);
                messageProducer.scheduleMessage(message);
        });
    }

    @NotNull
    private static Message<MessageDTO> createMessage(MessageDTO messageDTO, String messageId) {
        log.debug("[MESSAGE-PRODUCER][CREATE-MESSAGE] Creating message with ID: {}", messageId);

        return MessageBuilder
                .withPayload(messageDTO)
                .setHeader(ERROR_MSG_HEADER_RETRY, 0L)
                .build();
    }



}
