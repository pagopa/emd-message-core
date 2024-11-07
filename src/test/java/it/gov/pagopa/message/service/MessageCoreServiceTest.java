package it.gov.pagopa.message.service;

import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.faker.MessageDTOFaker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {
        MessageCoreServiceImpl.class,
})
class MessageCoreServiceTest {
    @MockBean
    BloomFilterServiceImpl bloomFilterServiceImpl;

    @MockBean
    MessageProducerServiceImpl messageProducerService;

    @Autowired
    MessageCoreServiceImpl messageCoreService;


    private final MessageDTO MESSAGE = MessageDTOFaker.mockInstance();
    private final String FISCAL_CODE = MESSAGE.getRecipientId();
    @Test
    void sendMessage_Ok()  {

        when(bloomFilterServiceImpl.mightContain(FISCAL_CODE)).thenReturn(true);
        doNothing().when(messageProducerService).enqueueMessage(MESSAGE);
        Boolean result = messageCoreService.sendMessage(MESSAGE).block();
        Assertions.assertEquals(true, result);

    }

    @Test
    void sendMessage_Ko()  {
        when(bloomFilterServiceImpl.mightContain(FISCAL_CODE)).thenReturn(false);
        Boolean result = messageCoreService.sendMessage(MESSAGE).block();
        Assertions.assertEquals(false,result);
    }

}
