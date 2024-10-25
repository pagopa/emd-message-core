package it.gov.pagopa.message.event.consumer;

import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.faker.MessageDTOFaker;
import it.gov.pagopa.message.service.MessageErrorConsumerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

import static it.gov.pagopa.message.constants.MessageCoreConstants.MessageHeader.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageErrorConsumerTest {

    @Mock
    MessageErrorConsumerService messageErrorConsumerService;
    @InjectMocks
    MessageErrorConsumer messageErrorConsumer;
    private Consumer<Flux<Message<String>>> consumerCommands;
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
        Message<String> message = MessageBuilder
                .withPayload(messageDTO.toString())
                .setHeader(ERROR_MSG_HEADER_RETRY, retry)
                .setHeader(ERROR_MSG_AUTH_URL, authenticationUrl)
                .setHeader(ERROR_MSG_MESSAGE_URL, messageUrl)
                .build();
        Flux<Message<String>> flux = Flux.just(message);
        consumerCommands.accept(flux);
        verify(messageErrorConsumerService).execute(flux);
    }


}
