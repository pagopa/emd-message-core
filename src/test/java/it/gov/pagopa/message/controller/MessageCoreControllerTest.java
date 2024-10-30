package it.gov.pagopa.message.controller;

import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.faker.MessageDTOFaker;
import it.gov.pagopa.message.service.MessageCoreServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(MessageCoreControllerImpl.class)
class MessageCoreControllerTest {

    @MockBean
    private MessageCoreServiceImpl messageCoreService;

    @Autowired
    private WebTestClient webTestClient;


    @Test
    void sendMessage_Ok() {
        MessageDTO messageDTO = MessageDTOFaker.mockInstance();

        Mockito.when(messageCoreService.sendMessage(messageDTO)).thenReturn(Mono.just(true));

        webTestClient.post()
                .uri("/emd/message-core/sendMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(messageDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String resultResponse = response.getResponseBody();
                    Assertions.assertNotNull(resultResponse);
                    Assertions.assertEquals("OK", resultResponse);
                });
    }

    @Test
    void sendMessage_Ko() {
        MessageDTO messageDTO = MessageDTOFaker.mockInstance();

        Mockito.when(messageCoreService.sendMessage(messageDTO)).thenReturn(Mono.just(false));

        webTestClient.post()
                .uri("/emd/message-core/sendMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(messageDTO)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String resultResponse = response.getResponseBody();
                    Assertions.assertNotNull(resultResponse);
                    Assertions.assertEquals("KO", resultResponse);
                });
    }

}
