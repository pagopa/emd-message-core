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

    private static final MessageDTO MESSAGE_DTO = MessageDTOFaker.mockInstance();

    @Test
    void sendMessage_Ok() {
        Mockito.when(messageCoreService.send(MESSAGE_DTO)).thenReturn(Mono.just(true));

        webTestClient.post()
                .uri("/emd/message-core/sendMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(MESSAGE_DTO)
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
        Mockito.when(messageCoreService.send(MESSAGE_DTO)).thenReturn(Mono.just(false));

        webTestClient.post()
                .uri("/emd/message-core/sendMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(MESSAGE_DTO)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String resultResponse = response.getResponseBody();
                    Assertions.assertNotNull(resultResponse);
                    Assertions.assertEquals("NO CHANNELS ENABLED", resultResponse);
                });
    }

}
