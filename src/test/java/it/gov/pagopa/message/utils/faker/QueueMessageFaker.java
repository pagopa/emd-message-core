package it.gov.pagopa.message.utils.faker;

import it.gov.pagopa.message.dto.MessageDTO;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static it.gov.pagopa.message.constants.MessageCoreConstants.MessageHeader.*;
import static it.gov.pagopa.message.utils.TestUtils.*;
import static it.gov.pagopa.message.utils.TestUtils.MESSAGE_URL;

public class QueueMessageFaker {

    public QueueMessageFaker(){}
    public static Message<MessageDTO> mockInstance(MessageDTO messageDTO) {
        return MessageBuilder
                .withPayload(messageDTO)
                .setHeader(ERROR_MSG_HEADER_RETRY, RETRY)
                .setHeader(ERROR_MSG_AUTH_URL, AUTHENTICATION_URL)
                .setHeader(ERROR_MSG_MESSAGE_URL, MESSAGE_URL)
                .build();
    }
}
