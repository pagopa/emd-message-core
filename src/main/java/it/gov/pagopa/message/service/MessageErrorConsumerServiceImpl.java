package it.gov.pagopa.message.service;

import it.gov.pagopa.message.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.message.constants.MessageCoreConstants.MessageHeader.*;


@Service
@Slf4j
public class MessageErrorConsumerServiceImpl implements MessageErrorConsumerService {

    private final SendMessageServiceImpl sendMessageServiceImpl;
    private final long maxRetry;
    public MessageErrorConsumerServiceImpl(SendMessageServiceImpl sendMessageServiceImpl,
                                           @Value("${app.retry.max-retry}") long maxRetry
    ) {
        this.maxRetry = maxRetry;
        this.sendMessageServiceImpl = sendMessageServiceImpl;
    }

    @Override
    public void processCommand(Message<MessageDTO> message) {
        log.info("[EMD-PROCESS-COMMAND] Queue message received: {}",message.getPayload());
        MessageHeaders headers = message.getHeaders();
        long retry = getNextRetry(headers);
        MessageDTO messageDTO = message.getPayload();
        if(retry!=0) {
            String messageUrl = (String) headers.get(ERROR_MSG_MESSAGE_URL);
            String authenticationUrl = (String) headers.get(ERROR_MSG_AUTH_URL);
            String entidyId = (String) headers.get(ERROR_MSG_ENTITY_ID);
            log.info("[EMD-PROCESS-COMMAND] Try {} for message {}",retry,messageDTO.getMessageId());
            sendMessageServiceImpl.sendMessage(messageDTO, messageUrl, authenticationUrl, entidyId, retry).then();
        }
        else
            log.info("[EMD-PROCESS-COMMAND] Message {} not retryable", messageDTO.getMessageId());
    }

    private long getNextRetry(MessageHeaders headers) {
        Long retry = (Long) headers.get(ERROR_MSG_HEADER_RETRY);
        if (retry != null && (retry >=0 && retry<maxRetry))
                return 1 + retry;
        return 0;
    }


}
