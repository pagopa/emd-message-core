package it.gov.pagopa.message.core.service;

import it.gov.pagopa.common.utils.Constants;
import it.gov.pagopa.message.core.dto.MessageDTO;
import it.gov.pagopa.message.core.event.producer.MessageErrorProducer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class MessageErrorProducerServiceImpl implements  MessageErrorProducerService {

    private final MessageErrorProducer messageErrorProducer;

    public MessageErrorProducerServiceImpl(MessageErrorProducer messageErrorProducer){
        this.messageErrorProducer = messageErrorProducer;
    }

    //Prima schedulazione del messaggio sulla coda di errore
    @Override
    public void sendError(MessageDTO messageDTO, String messageUrl, String authenticationUrl) {
        Message<MessageDTO> message = createMessage(messageDTO, messageUrl, authenticationUrl, 0);
        messageErrorProducer.sendToMessageErrorQueue(message);
    }

    //Schedulazioni successive al primo errore
    @Override
    public void sendError(MessageDTO messageDTO, String messageUrl, String authenticationUrl, long retry) {
        Message<MessageDTO> message = createMessage(messageDTO, messageUrl, authenticationUrl, retry);
        messageErrorProducer.sendToMessageErrorQueue(message);
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
