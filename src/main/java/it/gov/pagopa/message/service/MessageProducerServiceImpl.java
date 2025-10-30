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

/**
 * <p>Implementation of {@link MessageProducerService}.</p>
 *
 * <p>Builds Spring {@link Message} wrappers and delegates to {@link MessageProducer} for broker submission.</p>
 */
@Slf4j
@Service
public class MessageProducerServiceImpl implements MessageProducerService {

    private final MessageProducer messageProducer;

    public MessageProducerServiceImpl(MessageProducer messageProducer){
        this.messageProducer = messageProducer;
    }

    /**
     * <p>Wraps DTO in Spring Message and enqueues to broker.</p>
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Create {@link Message} wrapper with retry header initialized to 0.</li>
     *   <li>Delegate to {@link MessageProducer#scheduleMessage(Message)}.</li>
     * </ol>
     *
     *
     * @param messageDTO the message to be enqueued
     * @param messageId the unique identifier of the message
     * @return {@code Mono<Void>} completes when the message has been scheduled
     */
    @Override
    public Mono<Void> enqueueMessage(MessageDTO messageDTO, String messageId) {
        log.info("[MESSAGE-PRODUCER][ENQUEUE] Enqueuing message with ID: {}", messageId);

        return Mono.fromRunnable(() -> {
                Message<MessageDTO> message = createMessage(messageDTO, messageId);
                log.info("[MESSAGE-PRODUCER][ENQUEUE] Message with ID: {} successfully created. Sending to message queue.", messageId);
                messageProducer.scheduleMessage(message);
        });
    }

    /**
     * <p>Builds a Spring {@link Message} wrapper for the DTO.</p>
     *
     * <p>Includes retry header initialized to 0 for error handling logic.</p>
     *
     * @param messageDTO the payload
     * @param messageId the unique identifier (used only for logging)
     * @return {@code Message<MessageDTO>} wrapped message
     */
    @NotNull
    private static Message<MessageDTO> createMessage(MessageDTO messageDTO, String messageId) {
        log.debug("[MESSAGE-PRODUCER][CREATE-MESSAGE] Creating message with ID: {}", messageId);

        return MessageBuilder
                .withPayload(messageDTO)
                .setHeader(ERROR_MSG_HEADER_RETRY, 0L)
                .build();
    }



}
