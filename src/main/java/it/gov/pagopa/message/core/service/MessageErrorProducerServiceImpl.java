package it.gov.pagopa.message.core.service;

import it.gov.pagopa.common.utils.Constants;
import it.gov.pagopa.message.core.dto.MessageDTO;
import it.gov.pagopa.message.core.event.producer.MessageErrorProducer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class MessageErrorProducerServiceImpl implements  MessageErrorProducerService {

    private final MessageErrorProducer messageErrorProducer;
    private final long maxRetry;
    public MessageErrorProducerServiceImpl(MessageErrorProducer messageErrorProducer,
                                           @Value("${app.retry.max-retry}") long maxRetry
                                           ) {
        this.maxRetry = maxRetry;

        this.messageErrorProducer = messageErrorProducer;
    }

    //Prima schedulazione del messaggio sulla coda di errore
    @Override
    public void sendError(MessageDTO messageDTO, String messageUrl, String authenticationUrl) {
        Message<MessageDTO> message = createMessage(messageDTO, messageUrl, authenticationUrl, 1);
        messageErrorProducer.sendToMessageErrorQueue(message);
    }

    //Schedulazioni successive al primo errore
    @Override
    public void sendError(MessageDTO messageDTO, String messageUrl, String authenticationUrl, long retry) {
        if (maxRetry <= 0 || retry <= maxRetry) {
            Message<MessageDTO> message = createMessage(messageDTO, messageUrl, authenticationUrl, retry);
            messageErrorProducer.sendToMessageErrorQueue(message);
        }
        else  {
            log.info("[ERROR_MESSAGE_HANDLER] Max retry reached for message: {}", messageDTO);
        }
    }

    @NotNull
    private static Message<MessageDTO> createMessage(MessageDTO messageDTO, String messageUrl, String authenticationUrl, long retry) {
        return MessageBuilder
                .withPayload(messageDTO)
                .setHeader(Constants.ERROR_MSG_HEADER_RETRY, retry)
                .setHeader(Constants.ERROR_MSG_AUTH_URL, authenticationUrl)
                .setHeader(Constants.ERROR_MSG_MESSAGE_URL, messageUrl)
                .build();

    }


}
