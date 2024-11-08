package it.gov.pagopa.message.event.producer;


import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.faker.MessageDTOFaker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static it.gov.pagopa.message.constants.MessageCoreConstants.MessageHeader.*;
import static org.mockito.Mockito.*;


@ExtendWith({MockitoExtension.class})
class MessageProducerTest {

    @Mock
    private StreamBridge streamBridge;
    @InjectMocks
    private MessageProducer messageProducer;

    private static final MessageDTO MESSAGE_DTO = MessageDTOFaker.mockInstance();
    private static final String MESSAGE_URL = "messageUrl";
    private static final String AUTHENTICATION_URL = "authenticationUrl";
    private static final long RETRY = 1;

    @Test
     void testStreamBridgeSendCalled() {
        Message<MessageDTO> message = MessageBuilder
                .withPayload(MESSAGE_DTO)
                .setHeader(ERROR_MSG_HEADER_RETRY, RETRY)
                .setHeader(ERROR_MSG_AUTH_URL, AUTHENTICATION_URL)
                .setHeader(ERROR_MSG_MESSAGE_URL, MESSAGE_URL)
                .build();

        messageProducer.sendToMessageQueue(message);
        verify(streamBridge, times(1)).send(eq("messageSender-out-0"), any(), eq(message));

    }
}

