package it.gov.pagopa.message.service;

import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.event.producer.MessageProducer;
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
        MessageProducerServiceImpl.class
})
 class MessageProducerServiceTest {

    @Autowired
    MessageProducerServiceImpl messageErrorProducerService;
    @MockBean
    MessageProducer messageProducer;

    private final static MessageDTO messegeDTO = MessageDTOFaker.mockInstance();
    @Test
    void sendError1_OK(){
        messageErrorProducerService.enqueueMessage(messegeDTO);
        Mockito.verify(messageProducer,times(1)).sendToMessageQueue(any());
    }

}
