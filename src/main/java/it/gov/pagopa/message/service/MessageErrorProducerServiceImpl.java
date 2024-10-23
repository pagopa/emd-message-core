package it.gov.pagopa.message.service;


import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.event.producer.MessageErrorProducer;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.message.constants.MessageCoreConstants.MessageHeader.*;

@Slf4j
@Service
public class MessageErrorProducerServiceImpl implements  MessageErrorProducerService {

    private final MessageErrorProducer messageErrorProducer;

    public MessageErrorProducerServiceImpl(MessageErrorProducer messageErrorProducer){
        this.messageErrorProducer = messageErrorProducer;
    }

     @Override
    public void sendError(MessageDTO messageDTO, String messageUrl, String authenticationUrl, String entityId) {
        Message<MessageDTO> message = createMessage(messageDTO, messageUrl, authenticationUrl, entityId,0);
        messageErrorProducer.sendToMessageErrorQueue(message);
    }


    @Override
    public void sendError(MessageDTO messageDTO, String messageUrl, String authenticationUrl, String entityId, long retry) {
        Message<MessageDTO> message = createMessage(messageDTO, messageUrl, authenticationUrl, entityId, retry);
        messageErrorProducer.sendToMessageErrorQueue(message);
    }

    @NotNull
    private static Message<MessageDTO> createMessage(MessageDTO messageDTO, String messageUrl, String authenticationUrl, String entidyId, long retry) {
        return MessageBuilder
                .withPayload(messageDTO)
                .setHeader(ERROR_MSG_HEADER_RETRY, retry)
                .setHeader(ERROR_MSG_AUTH_URL, authenticationUrl)
                .setHeader(ERROR_MSG_MESSAGE_URL, messageUrl)
                .setHeader(ERROR_MSG_ENTITY_ID, entidyId)
                .build();

    }


}