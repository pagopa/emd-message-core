package it.gov.pagopa.message.service;

import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.event.producer.MessageErrorProducer;
import it.gov.pagopa.message.faker.MessageDTOFaker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {
        MessageErrorProducerServiceImpl.class
})
 class MessageErrorProducerServiceTest {

    @Autowired
    MessageErrorProducerServiceImpl messageErrorProducerService;
    @MockBean
    MessageErrorProducer messageErrorProducer;

    private final static MessageDTO messegeDTO = MessageDTOFaker.mockInstance();
    private final static String messegaUrl = "messegaUrl";
    private final static String authenticationUrl = "authenticationUrl";
    private final static long retry = 1;
    private final static String entityId = "entityId";
    @Test
    void sendError1_OK(){
        messageErrorProducerService.sendError(messegeDTO,messegaUrl,authenticationUrl,entityId);
        Mockito.verify(messageErrorProducer,times(1)).sendToMessageErrorQueue(any());
    }

    @Test
    void sendError2_OK(){
        messageErrorProducerService.sendError(messegeDTO,messegaUrl,authenticationUrl,entityId, retry);
        Mockito.verify(messageErrorProducer,times(1)).sendToMessageErrorQueue(any());
    }
}
