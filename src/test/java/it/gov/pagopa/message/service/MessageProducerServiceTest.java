package it.gov.pagopa.message.service;

import it.gov.pagopa.message.event.producer.MessageProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import static it.gov.pagopa.message.utils.TestUtils.MESSAGE_DTO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {
        MessageProducerServiceImpl.class
})
 class MessageProducerServiceTest {

    @Autowired
    MessageProducerServiceImpl messageProducerService;
    @MockBean
    MessageProducer messageProducer;

    @Test
    void sendMessage_OK(){
        StepVerifier.create(messageProducerService.enqueueMessage(MESSAGE_DTO))
                .verifyComplete();
        Mockito.verify(messageProducer,times(1)).scheduleMessage(any());
    }

}
