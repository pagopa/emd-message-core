package it.gov.pagopa.message.core.event.consumer;

import it.gov.pagopa.message.core.dto.MessageDTO;


import it.gov.pagopa.message.core.service.MessageErrorConsumerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;


@Configuration
public class MessageErrorConsumer {


    @Bean
    public Consumer<Message<MessageDTO>> consumerCommands(MessageErrorConsumerService consumerService) {
        return consumerService::processCommand;
    }

}
