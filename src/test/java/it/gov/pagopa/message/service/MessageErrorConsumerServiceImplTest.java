package it.gov.pagopa.message.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.faker.MessageDTOFaker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import static it.gov.pagopa.message.constants.MessageCoreConstants.MessageHeader.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {
        MessageConsumerServiceImpl.class,
        ObjectMapper.class
})
@TestPropertySource(properties = {
        "app.retry.max-retry=5",
        "spring.application.name=test",
        "spring.cloud.stream.kafka.bindings.consumerCommands-in-0.consumer.ackTime=500",
        "app.message-core.build-delay-duration=PT1S"
})
class MessageErrorConsumerServiceImplTest {

    @MockBean
    SendMessageServiceImpl sendMessageServiceImpl;

    @MockBean
    ObjectMapper objectMapper;
    @Autowired
    MessageConsumerServiceImpl messageErrorConsumerServiceImpl;

    @Test
    void processCommand_RetryOk(){
        MessageDTO messageDTO = MessageDTOFaker.mockInstance();
        String messageUrl = "messegaUrl";
        String authenticationUrl = "authenticationUrl";
        String entityId = "entityId";
        long retry = 1;
        Message<String> message = MessageBuilder
                .withPayload(messageDTO.toString())
                .setHeader(ERROR_MSG_HEADER_RETRY, retry)
                .setHeader(ERROR_MSG_AUTH_URL, authenticationUrl)
                .setHeader(ERROR_MSG_MESSAGE_URL, messageUrl)
                .setHeader(ERROR_MSG_ENTITY_ID, entityId)
                .build();
        Mockito.when(sendMessageServiceImpl.sendMessage(any(MessageDTO.class),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyLong()))
                .thenReturn(Mono.empty());
        retry += 1;
        messageErrorConsumerServiceImpl.execute(messageDTO,message,null);
        Mockito.verify(sendMessageServiceImpl,times(1)).sendMessage(messageDTO, messageUrl, authenticationUrl, entityId, retry);
    }

    @Test
    void processCommand_RetryKo(){
        MessageDTO messageDTO = MessageDTOFaker.mockInstance();
        String messageUrl = "messegaUrl";
        String authenticationUrl = "authenticationUrl";
        String entityId = "entityId";
        long retry = 10;
        Message<String> message = MessageBuilder
                .withPayload(messageDTO.toString())
                .setHeader(ERROR_MSG_HEADER_RETRY, retry)
                .setHeader(ERROR_MSG_AUTH_URL, authenticationUrl)
                .setHeader(ERROR_MSG_MESSAGE_URL, messageUrl)
                .setHeader(ERROR_MSG_ENTITY_ID, entityId)
                .build();
        messageErrorConsumerServiceImpl.execute(messageDTO,message,null);
        Mockito.verify(sendMessageServiceImpl,times(0)).sendMessage(messageDTO, messageUrl, authenticationUrl,entityId, retry);

    }
}
