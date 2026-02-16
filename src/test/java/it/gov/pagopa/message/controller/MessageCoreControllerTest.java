package it.gov.pagopa.message.controller;

import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.enums.Channel;
import it.gov.pagopa.message.enums.WorkflowType;
import it.gov.pagopa.message.service.MessageCoreServiceImpl;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

    //No channel
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

    //Digital with analog date
    @Test
    void sendMessage_Ko_Digital_With_AnalogDate_test() {
        MessageDTO messageDTO = MessageDTO.builder()
            .messageId("messageId")
            .recipientId("recipientId")
            .triggerDateTime("2023-12-25T10:30:00Z")
            .senderDescription("sender")
            .messageUrl("messageUrl")
            .originId("originId")
            .title("title")
            .content("message")
            .associatedPayment(true)
            .analogSchedulingDate("2023-12-25T10:30:00Z")
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

    //Analog without analog date
    @Test
    void sendMessage_Ko_Analog_Without_AnalogDate_test() {
        MessageDTO messageDTO = MessageDTO.builder()
            .messageId("messageId")
            .recipientId("recipientId")
            .triggerDateTime("2023-12-25T10:30:00Z")
            .senderDescription("sender")
            .messageUrl("messageUrl")
            .originId("originId")
            .title("title")
            .content("message")
            .associatedPayment(true)
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

    // Validation tests for @Size, @Pattern and @NotNull constraints
    @ParameterizedTest
    @MethodSource("provideInvalidMessageDTOs")
    void sendMessage_Ko_ValidationErrors(MessageDTO invalidDTO, String expectedField, String expectedMessagePart) {

        webTestClient.post()
                .uri("/emd/message-core/sendMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(invalidDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .consumeWith(response -> {
                String resultResponse = response.getResponseBody();
                System.out.println("Test case: " + expectedField + " - Response: " + resultResponse);
                
                // Utilizzo di Assertions di Jupiter
                Assertions.assertNotNull(resultResponse);
                Assertions.assertTrue(resultResponse.contains("INVALID_REQUEST"), 
                    "Response should contain 'INVALID_REQUEST'");
                Assertions.assertTrue(resultResponse.contains("[" + expectedField + "]"), 
                    "Response should contain field name: [" + expectedField + "]");
                Assertions.assertTrue(resultResponse.contains(expectedMessagePart), 
                    "Response should contain expected message part: " + expectedMessagePart);
            
            });
    }

    private static Stream<Arguments> provideInvalidMessageDTOs() {
        MessageDTO baseValidDTO = MESSAGE_DTO;

        return Stream.of(
            // ==================== @Size VALIDATIONS ====================
            
            // messageId: @Size(min = 1, max = 100)
            Arguments.of(
                baseValidDTO.toBuilder().messageId("").build(),
                "messageId", "The messageId field must be between 1 and 100"
            ),
            Arguments.of(
                baseValidDTO.toBuilder().messageId("a".repeat(101)).build(), // 101 caratteri
                "messageId", "The messageId field must be between 1 and 100"
            ),
            
            // recipientId: @Size(min = 1, max = 100)
            Arguments.of(
                baseValidDTO.toBuilder().recipientId("").build(),
                "recipientId", "The recipientId field must be between 1 and 100"
            ),
            Arguments.of(
                baseValidDTO.toBuilder().recipientId("b".repeat(101)).build(), // 101 caratteri
                "recipientId", "The recipientId field must be between 1 and 100"
            ),
            
            // senderDescription: @Size(min = 1, max = 250)
            Arguments.of(
                baseValidDTO.toBuilder().senderDescription("").build(),
                "senderDescription", "The senderDescription field must be between 1 and 250"
            ),
            Arguments.of(
                baseValidDTO.toBuilder().senderDescription("c".repeat(251)).build(), // 251 caratteri
                "senderDescription", "The senderDescription field must be between 1 and 250"
            ),
            
            // messageUrl: @Size(min = 1, max = 2048)
            Arguments.of(
                baseValidDTO.toBuilder().messageUrl("").build(),
                "messageUrl", "The messageUrl field must be between 1 and 2048"
            ),
            Arguments.of(
                baseValidDTO.toBuilder().messageUrl("d".repeat(2049)).build(), // 2049 caratteri
                "messageUrl", "The messageUrl field must be between 1 and 2048"
            ),
            
            // originId: @Size(min = 1, max = 100)
            Arguments.of(
                baseValidDTO.toBuilder().originId("").build(),
                "originId", "The originId field must be between 1 and 100"
            ),
            Arguments.of(
                baseValidDTO.toBuilder().originId("e".repeat(101)).build(), // 101 caratteri
                "originId", "The originId field must be between 1 and 100"
            ),
            
            // title: @Size(min = 1, max = 250)
            Arguments.of(
                baseValidDTO.toBuilder().title("").build(),
                "title", "The title field must be between 1 and 250"
            ),
            Arguments.of(
                baseValidDTO.toBuilder().title("f".repeat(251)).build(), // 251 caratteri
                "title", "The title field must be between 1 and 250"
            ),
            
            // content: @Size(min = 1, max = 100000)
            Arguments.of(
                baseValidDTO.toBuilder().content("").build(),
                "content", "The content field must be between 1 and 100000"
            ),
            Arguments.of(
                baseValidDTO.toBuilder().content("g".repeat(100001)).build(), // 100001 caratteri
                "content", "The content field must be between 1 and 100000"
            ),
            
            // ==================== @Pattern VALIDATIONS ====================
            
            // triggerDateTime: pattern validation
            Arguments.of(
                baseValidDTO.toBuilder().triggerDateTime("invalid-date-format").build(),
                "triggerDateTime", "The date format must be ISO 8601 (es. YYYY-MM-DDTHH:mm:ssZ)"
            ), 
            Arguments.of(
                baseValidDTO.toBuilder()
                    .workflowType(WorkflowType.ANALOG).triggerDateTime("2023-99-99T99:99:99Z").build(),
                "triggerDateTime", "The date format must be ISO 8601 (es. YYYY-MM-DDTHH:mm:ssZ)"
            ),
            
            // analogSchedulingDate: pattern validation (quando presente)
            Arguments.of(
                baseValidDTO.toBuilder().workflowType(WorkflowType.ANALOG)
                    .analogSchedulingDate("invalid-date-format").build(),
                "analogSchedulingDate", "The date format must be ISO 8601 (es. YYYY-MM-DDTHH:mm:ssZ)"
            ),
            Arguments.of(
                baseValidDTO.toBuilder()
                    .workflowType(WorkflowType.ANALOG)
                    .analogSchedulingDate("2023-99-99T99:99:99Z").build(),
                "analogSchedulingDate", "The date format must be ISO 8601 (es. YYYY-MM-DDTHH:mm:ssZ)"
            ),
            
            // ==================== @NotNull VALIDATIONS ====================

            Arguments.of(
                baseValidDTO.toBuilder().messageId(null).build(),
                "messageId", "The messageId field is required"
            ),
            Arguments.of(
                baseValidDTO.toBuilder().recipientId(null).build(),
                "recipientId", "The recipientId field is required"
            ),
            Arguments.of(
                baseValidDTO.toBuilder().triggerDateTime(null).build(),
                "triggerDateTime", "The triggerDateTime field is required"
            ),
            Arguments.of(
                baseValidDTO.toBuilder().senderDescription(null).build(),
                "senderDescription", "The senderDescription field is required"
            ),
            Arguments.of(
                baseValidDTO.toBuilder().messageUrl(null).build(),
                "messageUrl", "The messageUrl field is required"
            ),  
            Arguments.of(
                baseValidDTO.toBuilder().originId(null).build(),
                "originId", "The originId field is required"
            ),
            Arguments.of(
                baseValidDTO.toBuilder().title(null).build(),
                "title", "The title field is required"
            ),
            Arguments.of(
                baseValidDTO.toBuilder().content(null).build(),
                "content", "The content field is required"
            ),Arguments.of(
                baseValidDTO.toBuilder().workflowType(null).build(),
                "workflowType", "The workflowType field is required"
            ),

            // ==================== @NotBlank VALIDATIONS ====================
            Arguments.of(baseValidDTO.toBuilder().messageId(" ").build(),
                "messageId", "The messageId field is required"),
            Arguments.of(baseValidDTO.toBuilder().recipientId(" ").build(),
                "recipientId", "The recipientId field is required"),
            Arguments.of(baseValidDTO.toBuilder().triggerDateTime(" ").build(),
                "triggerDateTime", "The triggerDateTime field is required"),
            Arguments.of(baseValidDTO.toBuilder().senderDescription(" ").build(),
                "senderDescription", "The senderDescription field is required"),
            Arguments.of(baseValidDTO.toBuilder().messageUrl(" ").build(),
                "messageUrl", "The messageUrl field is required"),
            Arguments.of(baseValidDTO.toBuilder().originId(" ").build(),
                "originId", "The originId field is required"),
            Arguments.of(baseValidDTO.toBuilder().title(" ").build(),
                "title", "The title field is required"),
            Arguments.of(baseValidDTO.toBuilder().content(" ").build(),
                "content", "The content field is required")
        );

    }

}
