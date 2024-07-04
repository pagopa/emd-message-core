package it.gov.pagopa.message.core.service;

import it.gov.pagopa.common.utils.Constants;
import it.gov.pagopa.message.core.dto.MessageDTO;

import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

import org.springframework.messaging.Message;

import static it.gov.pagopa.common.utils.Constants.ERROR_MSG_AUTH_URL;
import static it.gov.pagopa.common.utils.Constants.ERROR_MSG_MESSAGE_URL;


@Service
@Slf4j
public class MessageErrorConsumerServiceImpl implements MessageErrorConsumerService {

    private final SendMessageServiceImpl sendMessageServiceImpl;

    public MessageErrorConsumerServiceImpl(SendMessageServiceImpl sendMessageServiceImpl) {
        this.sendMessageServiceImpl = sendMessageServiceImpl;
    }

    @Override
    public void processCommand(Message<MessageDTO> message) {
        MessageHeaders headers = message.getHeaders();
        long retry = getNextRetry(headers);
        MessageDTO messageDTO = message.getPayload();
        String messageUrl = (String) headers.get(ERROR_MSG_MESSAGE_URL);
        String authenticationUrl = (String) headers.get(ERROR_MSG_AUTH_URL);
        sendMessageServiceImpl.sendMessage(messageDTO, messageUrl, authenticationUrl,retry);
    }

    private static long getNextRetry(MessageHeaders headers) {
        Long retryStr = (Long) headers.get(Constants.ERROR_MSG_HEADER_RETRY);
        if (retryStr != null) {
            try {
                return 1 + retryStr;
            } catch (Exception e) {
                log.info("[ERROR_MESSAGE_HANDLER] RETRY header not usable: {}", retryStr);
            }
        }
        return 0;
    }


}
