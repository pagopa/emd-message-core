package it.gov.pagopa.message.core.service;

import it.gov.pagopa.common.utils.Constants;
import it.gov.pagopa.message.core.dto.MessageDTO;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

import org.springframework.messaging.Message;

import static it.gov.pagopa.common.utils.Constants.ERROR_MSG_AUTH_URL;
import static it.gov.pagopa.common.utils.Constants.ERROR_MSG_MESSAGE_URL;
import static it.gov.pagopa.common.utils.Utils.logInfo;


@Service
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
        logInfo("[EMD-PROCESS-COMMAND] Queue message received: %s".formatted(message.getPayload()));
        MessageHeaders headers = message.getHeaders();
        long retry = getNextRetry(headers);
        if(retry!=0) {
            logInfo("[EMD-PROCESS-COMMAND] Try: %s".formatted(retry));
            MessageDTO messageDTO = message.getPayload();
            String messageUrl = (String) headers.get(ERROR_MSG_MESSAGE_URL);
            String authenticationUrl = (String) headers.get(ERROR_MSG_AUTH_URL);
            sendMessageServiceImpl.sendMessage(messageDTO, messageUrl, authenticationUrl, retry);
        }
        else
            logInfo("[EMD-PROCESS-COMMAND] Not retryable");
    }

    private long getNextRetry(MessageHeaders headers) {
        Long retry = (Long) headers.get(Constants.ERROR_MSG_HEADER_RETRY);
        if (retry != null && (retry >=0 && retry<maxRetry))
                return 1 + retry;
        return 0;
    }


}
