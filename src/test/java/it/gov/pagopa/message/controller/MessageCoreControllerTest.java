package it.gov.pagopa.message.controller;

import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.faker.MessageDTOFaker;
import it.gov.pagopa.message.faker.OutcomeFaker;
import it.gov.pagopa.message.model.Outcome;
import it.gov.pagopa.message.service.MessageCoreServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(MessageCoreControllerImpl.class)
@ContextConfiguration(classes = MessageCoreControllerImpl.class)
class MessageCoreControllerTest {

    @MockBean
    private MessageCoreServiceImpl messageCoreService;

    @Autowired
    private WebTestClient webTestClient;


    @Test
    void sendMessage_Ok() {
        MessageDTO messageDTO = MessageDTOFaker.mockInstance();
        Outcome outcome = OutcomeFaker.mockInstance(true);

        Mockito.when(messageCoreService.sendMessage(messageDTO)).thenReturn(Mono.just(outcome));

        webTestClient.post()
                .uri("/emd/message/send")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(messageDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Outcome.class)
                .consumeWith(response -> {
                    Outcome resultResponse = response.getResponseBody();
                    Assertions.assertNotNull(resultResponse);
                    Assertions.assertEquals(outcome, resultResponse);
                });
    }

    @Test
    void sendMessage_NoChannelEnabled() {
        MessageDTO messageDTO = MessageDTOFaker.mockInstance();
        Outcome outcome = OutcomeFaker.mockInstance(false);

        Mockito.when(messageCoreService.sendMessage(messageDTO)).thenReturn(Mono.just(outcome));

        webTestClient.post()
                .uri("/emd/message/send")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(messageDTO)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody(Outcome.class)
                .consumeWith(response -> {
                    Outcome resultResponse = response.getResponseBody();
                    Assertions.assertNotNull(resultResponse);
                    Assertions.assertEquals(outcome, resultResponse);
                });
    }

}
