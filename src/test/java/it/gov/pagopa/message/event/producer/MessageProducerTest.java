package it.gov.pagopa.message.event.producer;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.MessageDeliveryException;

import static it.gov.pagopa.message.utils.TestUtils.QUEUE_MESSAGE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith({MockitoExtension.class})
class MessageProducerTest {

    @Mock
    private StreamBridge streamBridge;
    @InjectMocks
    private MessageProducer messageProducer;

    @Test
     void testStreamBridgeSendCalled() {
        when(streamBridge.send(eq("messageSender-out-0"), any(), eq(QUEUE_MESSAGE))).thenReturn(true);

        messageProducer.scheduleMessage(QUEUE_MESSAGE);

        verify(streamBridge, times(1)).send(eq("messageSender-out-0"), any(), eq(QUEUE_MESSAGE));
    }

    @Test
    void scheduleMessage_brokerRejects_throws() {
        // DURABILITA': se il broker non accetta il messaggio, deve essere segnalato un errore
        // cosi' il chiamante non risponde 200 OK per un messaggio perso.
        when(streamBridge.send(eq("messageSender-out-0"), any(), eq(QUEUE_MESSAGE))).thenReturn(false);

        Assertions.assertThrows(MessageDeliveryException.class,
                () -> messageProducer.scheduleMessage(QUEUE_MESSAGE));
    }
}

