package it.gov.pagopa.message.core.event.consumer;

import it.gov.pagopa.common.utils.Constants;
import it.gov.pagopa.message.core.dto.MessageDTO;
import it.gov.pagopa.message.core.faker.MessageDTOFaker;
import it.gov.pagopa.message.core.service.MessageErrorConsumerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Consumer;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageErrorConsumerTest {

    @Mock
    MessageErrorConsumerService messageErrorConsumerService;
    @InjectMocks
    MessageErrorConsumer messageErrorConsumer;
    private Consumer<Message<MessageDTO>> consumerCommands;
    @BeforeEach
    public void setUp(){
        consumerCommands = messageErrorConsumer.consumerCommands(messageErrorConsumerService);
    }


    @Test
    void consumerCommands(){
        MessageDTO messageDTO = MessageDTOFaker.mockInstance();
        String messageUrl = "messegaUrl";
        String authenticationUrl = "authenticationUrl";
        long retry = 1;
        Message<MessageDTO> message = MessageBuilder
                .withPayload(messageDTO)
                .setHeader(Constants.ERROR_MSG_HEADER_RETRY, retry)
                .setHeader(Constants.ERROR_MSG_AUTH_URL, authenticationUrl)
                .setHeader(Constants.ERROR_MSG_MESSAGE_URL, messageUrl)
                .build();
        consumerCommands.accept(message);
        verify(messageErrorConsumerService).processCommand(message);
    }


}
