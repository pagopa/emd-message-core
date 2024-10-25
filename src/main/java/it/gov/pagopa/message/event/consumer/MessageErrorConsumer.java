package it.gov.pagopa.message.event.consumer;


import it.gov.pagopa.message.service.MessageErrorConsumerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;


@Configuration
public class MessageErrorConsumer {


    @Bean
    public Consumer<Flux<Message<String>>> consumerCommands(MessageErrorConsumerService consumerService) {
        return consumerService::execute;
    }

}
