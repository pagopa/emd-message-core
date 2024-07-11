package it.gov.pagopa.message.core.service;

import it.gov.pagopa.message.core.dto.MessageDTO;
import it.gov.pagopa.message.core.event.producer.MessageErrorProducer;
import it.gov.pagopa.message.core.faker.MessageDTOFaker;
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
    @Test
    void sendError1_OK(){
        MessageDTO messegeDTO = MessageDTOFaker.mockInstance();
        String messegaUrl = "messegaUrl";
        String authenticationUrl = "authenticationUrl";

        messageErrorProducerService.sendError(messegeDTO,messegaUrl,authenticationUrl);
        Mockito.verify(messageErrorProducer,times(1)).sendToMessageErrorQueue(any());
    }

    @Test
    void sendError2_OK(){
        MessageDTO messegeDTO = MessageDTOFaker.mockInstance();
        String messegaUrl = "messegaUrl";
        String authenticationUrl = "authenticationUrl";
        long retry = 1;
        messageErrorProducerService.sendError(messegeDTO,messegaUrl,authenticationUrl,retry);
        Mockito.verify(messageErrorProducer,times(1)).sendToMessageErrorQueue(any());
    }
}
