package it.gov.pagopa.message.controller;

import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.enums.Channel;
import it.gov.pagopa.message.enums.WorkflowType;
import it.gov.pagopa.message.service.MessageCoreServiceImpl;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static it.gov.pagopa.message.utils.TestUtils.MESSAGE_DTO;

@WebFluxTest(MessageCoreControllerImpl.class)
class MessageCoreControllerTest {

    @MockBean
    private MessageCoreServiceImpl messageCoreService;

    @Autowired
    private WebTestClient webTestClient;

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
                    Assertions.assertEquals("{\"outcome\":\"OK\"}", resultResponse);
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
                    Assertions.assertEquals("{\"outcome\":\"NO_CHANNELS_ENABLED\"}",resultResponse);
                });
    }


  @Test
  void sendMessage_Ko_Digital_With_AnalogDate_test() {
    MessageDTO messageDTO = MessageDTO.builder()
        .messageId("messageId")
        .recipientId("recipientId")
        .triggerDateTime("date")
        .senderDescription("sender")
        .messageUrl("messageUrl")
        .originId("originId")
        .content("message")
        .associatedPayment(true)
        .idPsp("originId")
        .analogSchedulingDate("date")
        .workflowType(WorkflowType.valueOf("DIGITAL"))
        .channel(Channel.valueOf("SEND"))
        .build();

    webTestClient.post()
        .uri("/emd/message-core/sendMessage")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(messageDTO)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .consumeWith((response) -> {
          String resultResponse = new String(Objects.requireNonNull(response.getResponseBody()));
          Assertions.assertNotNull(resultResponse);
          Assertions.assertTrue(resultResponse.contains(
              "{\"code\":\"INVALID_REQUEST\",\"message\":\"[analogSchedulingDate]: analogSchedulingDate must be null or empty when workflowType is DIGITAL\"}"));
        });
  }

  @Test
  void sendMessage_Ko_Analog_Without_AnalogDate_test() {
    MessageDTO messageDTO = MessageDTO.builder()
        .messageId("messageId")
        .recipientId("recipientId")
        .triggerDateTime("date")
        .senderDescription("sender")
        .messageUrl("messageUrl")
        .originId("originId")
        .content("message")
        .associatedPayment(true)
        .idPsp("originId")
        .analogSchedulingDate(null)
        .workflowType(WorkflowType.valueOf("ANALOG"))
        .channel(Channel.valueOf("SEND"))
        .build();

    webTestClient.post()
        .uri("/emd/message-core/sendMessage")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(messageDTO)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .consumeWith((response) -> {
          String resultResponse = new String(Objects.requireNonNull(response.getResponseBody()));
          Assertions.assertNotNull(resultResponse);
          Assertions.assertTrue(resultResponse.contains(
              "{\"code\":\"INVALID_REQUEST\",\"message\":\"[analogSchedulingDate]: analogSchedulingDate is required when workflowType is ANALOG\"}"));
        });
  }

}
