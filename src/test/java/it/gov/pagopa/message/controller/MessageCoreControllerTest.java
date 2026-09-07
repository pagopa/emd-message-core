package it.gov.pagopa.message.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.gov.pagopa.message.config.JacksonConfig;
import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.dto.MessageSearchResponseDTO;
import it.gov.pagopa.message.enums.Channel;
import it.gov.pagopa.message.enums.WorkflowType;
import it.gov.pagopa.message.service.MessageCoreServiceImpl;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static it.gov.pagopa.message.utils.TestUtils.MESSAGE_DTO;
import static it.gov.pagopa.message.utils.TestUtils.OBJECT_MAPPER;

@WebFluxTest(MessageCoreControllerImpl.class)
@Import(JacksonConfig.class)
class MessageCoreControllerTest {

    @MockitoBean
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
            .messageUrl("https://messageUrl.test")
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
            .messageUrl("https://messageUrl.test")
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


    @ParameterizedTest
    @MethodSource("provideAssociatedPaymentTestCases")
    void sendMessage_AssociatedPayment_DefaultsToFalse(MessageDTO messageDTO, String scenario) {
        // ArgumentCaptor to catch argument passed to service layer
        ArgumentCaptor<MessageDTO> captor = ArgumentCaptor.forClass(MessageDTO.class);
        Mockito.when(messageCoreService.send(Mockito.any(MessageDTO.class)))
            .thenReturn(Mono.just(true));


        webTestClient.post()
            .uri("/emd/message-core/sendMessage")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(messageDTO)
            .exchange()
            .expectStatus().isOk();

        // Assert: check that the service layer received a MessageDTO with associatedPayment set to false
        Mockito.verify(messageCoreService).send(captor.capture());
        MessageDTO capturedDTO = captor.getValue();

        Assertions.assertNotNull(capturedDTO.getAssociatedPayment(),
            "associatedPayment should not be null when " + scenario);
        Assertions.assertEquals(false, capturedDTO.getAssociatedPayment(),
            "associatedPayment should default to false when " + scenario);
    }

    private static Stream<Arguments> provideAssociatedPaymentTestCases() {
        MessageDTO baseDTOWithNull = MessageDTO.builder()
            .messageId("messageId")
            .recipientId("recipientId")
            .triggerDateTime("2023-12-25T10:30:00Z")
            .senderDescription("sender")
            .messageUrl("https://messageUrl.test")
            .originId("originId")
            .title("title")
            .content("message")
            .associatedPayment(null)  // explicitly null
            .workflowType(WorkflowType.DIGITAL)
            .channel(Channel.SEND)
            .build();

        MessageDTO baseDTOOmitted = MessageDTO.builder()
            .messageId("messageId")
            .recipientId("recipientId")
            .triggerDateTime("2023-12-25T10:30:00Z")
            .senderDescription("sender")
            .messageUrl("https://messageUrl.test")
            .originId("originId")
            .title("title")
            .content("message")
            // associatedPayment omitted
            .workflowType(WorkflowType.DIGITAL)
            .channel(Channel.SEND)
            .build();

        return Stream.of(
            Arguments.of(baseDTOWithNull, "set to null"),
            Arguments.of(baseDTOOmitted, "omitted")
        );
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
                baseValidDTO.toBuilder().messageUrl("https://test.test"+"d".repeat(2049)).build(), // 2049 caratteri
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

            Arguments.of(
                baseValidDTO.toBuilder()
                    .messageUrl("test.it").build(),
                "messageUrl", "The messageUrl field must be a valid URL"
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

            // ==================== @NotBlankUnicode VALIDATIONS ====================
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
                "content", "The content field is required"),

            // ==================== @NotBlankUnicode VALIDATIONS for unicode spaces ====================
            Arguments.of(baseValidDTO.toBuilder().messageId("      ").build(),
            "messageId", "The messageId field is required"),
            Arguments.of(baseValidDTO.toBuilder().recipientId("      ").build(),
                "recipientId", "The recipientId field is required"),
            Arguments.of(baseValidDTO.toBuilder().triggerDateTime("      ").build(),
                "triggerDateTime", "The triggerDateTime field is required"),
            Arguments.of(baseValidDTO.toBuilder().senderDescription("      ").build(),
                "senderDescription", "The senderDescription field is required"),
            Arguments.of(baseValidDTO.toBuilder().messageUrl("      ").build(),
                "messageUrl", "The messageUrl field is required"),
            Arguments.of(baseValidDTO.toBuilder().originId("      ").build(),
                "originId", "The originId field is required"),
            Arguments.of(baseValidDTO.toBuilder().title("      ").build(),
                "title", "The title field is required"),
            Arguments.of(baseValidDTO.toBuilder().content("      ").build(),
                "content", "The content field is required")
        );

    }

    @ParameterizedTest
    @MethodSource("provideStringFieldsForCoercionTest")
    void sendMessage_Ko_NumericValueOnStringField_test(String fieldName) throws JsonProcessingException {
        Map<String, Object> payload = buildPayloadWithOverride(fieldName, 1412412412L);

        String rawJsonPayload = OBJECT_MAPPER.writeValueAsString(payload);

        webTestClient.post()
            .uri("/emd/message-core/sendMessage")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(rawJsonPayload)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(String.class)
            .consumeWith(response -> {
                String resultResponse = response.getResponseBody();
                Assertions.assertNotNull(resultResponse);
                Assertions.assertTrue(resultResponse.contains("[" + fieldName + "]"),
                    "Response should contain field name: [" + fieldName + "]");
                Assertions.assertTrue(resultResponse.contains("invalid value for type String"),
                    "Response should indicate invalid type for String field");
            });
    }

    @ParameterizedTest
    @MethodSource("provideStringFieldsForCoercionTest")
    void sendMessage_Ko_BooleanValueOnStringField_test(String fieldName) throws JsonProcessingException {
        Map<String, Object> payload = buildPayloadWithOverride(fieldName, true);

        String rawJsonPayload = OBJECT_MAPPER.writeValueAsString(payload);

        webTestClient.post()
            .uri("/emd/message-core/sendMessage")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(rawJsonPayload)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(String.class)
            .consumeWith(response -> {
                String resultResponse = response.getResponseBody();
                Assertions.assertNotNull(resultResponse);
                Assertions.assertTrue(resultResponse.contains("[" + fieldName + "]"),
                    "Response should contain field name: [" + fieldName + "]");
                Assertions.assertTrue(resultResponse.contains("invalid value for type String"),
                    "Response should indicate invalid type for String field");
            });
    }

    private static Map<String, Object> buildPayloadWithOverride(String fieldName, Object overrideValue) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messageId", "messageId");
        payload.put("recipientId", "recipientId");
        payload.put("triggerDateTime", "2023-12-25T10:30:00Z");
        payload.put("senderDescription", "sender");
        payload.put("messageUrl", "https://messageUrl.test");
        payload.put("originId", "originId");
        payload.put("title", "title");
        payload.put("content", "message");
        payload.put("associatedPayment", true);
        payload.put("workflowType", "DIGITAL");
        payload.put("channel", "SEND");

        // Override only the field under test with the given value
        payload.put(fieldName, overrideValue);
        return payload;
    }

    private static Stream<String> provideStringFieldsForCoercionTest() {
        return Stream.of(
            "messageId", "recipientId", "triggerDateTime",
            "senderDescription", "messageUrl", "originId", "title", "content"
        );
    }

    // =========================================================================
    // SEARCH MESSAGES TESTS
    // =========================================================================

    @Test
    void searchMessages_Ok() {
        MessageSearchResponseDTO expectedResponse = MessageSearchResponseDTO.builder()
                .content(java.util.List.of())
                .page(0)
                .size(10)
                .totalElements(0L)
                .totalPages(0)
                .build();

        Mockito.when(messageCoreService.searchMessages(
                Mockito.any(), Mockito.any(), Mockito.any(), 
                Mockito.any(), Mockito.any(), 
                Mockito.anyInt(), Mockito.anyInt(), Mockito.any()))
            .thenReturn(Mono.just(expectedResponse));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/emd/message-core/search")
                        .queryParam("messageId", "MSG123")
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MessageSearchResponseDTO.class)
                .isEqualTo(expectedResponse);
    }

    @Test
    void searchMessages_WithAllFilters_Ok() {
        String messageId = "msgId";
        String recipientId = "recipientId";
        String originId = "originId";
        String startDate = "2023-12-25T10:30:00";
        String endDate = "2023-12-26T10:30:00";

        Mockito.when(messageCoreService.searchMessages(
                Mockito.eq(messageId), Mockito.eq(recipientId), Mockito.eq(originId),
                Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                Mockito.eq(0), Mockito.eq(10), Mockito.any()))
            .thenReturn(Mono.just(MessageSearchResponseDTO.builder().build()));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/emd/message-core/search")
                        .queryParam("messageId", messageId)
                        .queryParam("recipientId", recipientId)
                        .queryParam("originId", originId)
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .queryParam("fields", "messageId,recipientId")
                        .build())
                .exchange()
                .expectStatus().isOk();

        // Verifichiamo che i parametri siano stati passati correttamente al service
        Mockito.verify(messageCoreService).searchMessages(
                Mockito.eq(messageId), 
                Mockito.eq(recipientId), 
                Mockito.eq(originId), 
                Mockito.any(LocalDateTime.class), 
                Mockito.any(LocalDateTime.class), 
                Mockito.eq(0), 
                Mockito.eq(10), 
                Mockito.argThat(list -> list.contains("messageId") && list.contains("recipientId"))
        );
    }

    @Test
    void searchMessages_InvalidDateFormat_BadRequest() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/emd/message-core/search")
                        .queryParam("startDate", "invalid-date")
                        .build())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void searchMessages_Pagination_Ok() {
        int page = 5;
        int size = 20;

        Mockito.when(messageCoreService.searchMessages(
                Mockito.any(), Mockito.any(), Mockito.any(), 
                Mockito.any(), Mockito.any(), 
                Mockito.eq(page), Mockito.eq(size), Mockito.any()))
            .thenReturn(Mono.just(MessageSearchResponseDTO.builder().build()));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/emd/message-core/search")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .exchange()
                .expectStatus().isOk();
    }

}
