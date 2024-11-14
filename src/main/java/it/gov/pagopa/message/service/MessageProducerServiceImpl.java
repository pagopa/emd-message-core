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
    public Mono<Void> enqueueMessage(MessageDTO messageDTO) {
        log.info("[MESSAGE-PRODUCER][ENQUEUE] Enqueuing message with ID: {}", messageDTO.getMessageId());

        return Mono.fromRunnable(() -> {
            try {
                Message<MessageDTO> message = createMessage(messageDTO);
                log.info("[MESSAGE-PRODUCER][ENQUEUE] Message with ID: {} successfully created. Sending to message queue.", messageDTO.getMessageId());
                messageProducer.scheduleMessage(message);
            } catch (Exception e) {
                log.error("[MESSAGE-PRODUCER][ENQUEUE] Error while creating or sending message with ID: {}. Error: {}", messageDTO.getMessageId(), e.getMessage());
            }
        });
    }

    @NotNull
    private static Message<MessageDTO> createMessage(MessageDTO messageDTO) {
        log.debug("[MESSAGE-PRODUCER][CREATE-MESSAGE] Creating message with ID: {}", messageDTO.getMessageId());

        return MessageBuilder
                .withPayload(messageDTO)
                .setHeader(ERROR_MSG_HEADER_RETRY, 0L)
                .build();
    }



}
