package it.gov.pagopa.message.event.producer;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import static it.gov.pagopa.message.utils.TestUtils.QUEUE_MESSAGE;
import static org.mockito.Mockito.*;


@ExtendWith({MockitoExtension.class})
class MessageProducerTest {

    @Mock
    private StreamBridge streamBridge;
    @InjectMocks
    private MessageProducer messageProducer;



    @Test
     void testStreamBridgeSendCalled() {
        messageProducer.sendToMessageQueue(QUEUE_MESSAGE);
        verify(streamBridge, times(1)).send(eq("messageSender-out-0"), any(), eq(QUEUE_MESSAGE));

    }
}

