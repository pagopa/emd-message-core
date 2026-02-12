package it.gov.pagopa.message.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.common.web.dto.ErrorDTO;
import it.gov.pagopa.message.dto.SendResponseDTO;
import org.apache.kafka.common.serialization.StringDeserializer;
import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.enums.Channel;
import it.gov.pagopa.message.enums.WorkflowType;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * Integration test for the complete message processing flow and message enqueuing.
 */
public class MessageCoreFlowIT extends BaseIT {

  private static final Logger log = LoggerFactory.getLogger(MessageCoreFlowIT.class);

  @Container
  static MockServerContainer mockServer = new MockServerContainer(
      DockerImageName.parse("mockserver/mockserver:latest")
  );

  private MockServerClient mockServerClient;


  @Autowired
  private ObjectMapper objectMapper;

  private static final String TEST_FISCAL_CODE = "RSSMRA80A01H501U";
  private static final String TEST_MESSAGE_ID = "MSG001";

  @DynamicPropertySource
  static void registerMockServerProperties(DynamicPropertyRegistry registry) {
    String mockServerUrl = "http://" + mockServer.getHost() + ":" + mockServer.getServerPort();
    registry.add("rest-client.citizen.baseUrl", () -> mockServerUrl);
  }

  @BeforeEach
  void setUp() {
    mockServerClient = new MockServerClient(
        mockServer.getHost(),
        mockServer.getServerPort()
    );
    mockServerClient.reset();
  }

  @Test
  void shouldPublishMessageToKafka_WhenCitizenConnectorReturnsOk() throws Exception {
    setupCitizenConnectorMock(TEST_FISCAL_CODE, "OK");

    // Create consumer BEFORE sending the message with unique group
    String uniqueGroupId = "test-group-ok-" + UUID.randomUUID();
    try (KafkaConsumer<String, String> consumer = createTestConsumer("test-courtesy-message", uniqueGroupId)) {

      MessageDTO messageDTO = createTestMessageDTO(TEST_MESSAGE_ID, TEST_FISCAL_CODE, Channel.SEND);

      webTestClient.post()
          .uri("/emd/message-core/sendMessage")
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
          .bodyValue(messageDTO)
          .exchange()
          .expectStatus().is2xxSuccessful()
          .expectBody(SendResponseDTO.class)
          .consumeWith(response -> {
            SendResponseDTO responseBody = response.getResponseBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getOutcome()).isEqualTo("OK");
          });

      ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
      assertThat(records.isEmpty()).isFalse();

      ConsumerRecord<String, String> record = records.iterator().next();
      MessageDTO publishedMessage = objectMapper.readValue(record.value(), MessageDTO.class);

      // Verify all fields
      assertThat(publishedMessage.getMessageId()).isEqualTo(TEST_MESSAGE_ID);
      assertThat(publishedMessage.getRecipientId()).isEqualTo(TEST_FISCAL_CODE);
      assertThat(publishedMessage.getSenderDescription()).isEqualTo(messageDTO.getSenderDescription());
      assertThat(publishedMessage.getMessageUrl()).isEqualTo(messageDTO.getMessageUrl());
      assertThat(publishedMessage.getOriginId()).isEqualTo(messageDTO.getOriginId());
      assertThat(publishedMessage.getContent()).isEqualTo(messageDTO.getContent());
      assertThat(publishedMessage.getAssociatedPayment()).isTrue();
      assertThat(publishedMessage.getChannel()).isEqualTo(Channel.SEND);
    }

    mockServerClient.verify(
        request()
            .withPath("/emd/citizen/filter/" + TEST_FISCAL_CODE)
            .withMethod("GET")
    );
  }

  @Test
  void shouldReturn202_WhenCitizenHasNoChannelsEnabled() throws Exception {
    setupCitizenConnectorMock(TEST_FISCAL_CODE, "NO CHANNELS ENABLED");

    String uniqueGroupId = "test-group-no-channels-" + UUID.randomUUID();
    try (KafkaConsumer<String, String> consumer = createTestConsumer("test-courtesy-message", uniqueGroupId)) {

      MessageDTO messageDTO = createTestMessageDTO(TEST_MESSAGE_ID, TEST_FISCAL_CODE, Channel.SEND);

      webTestClient.post()
          .uri("/emd/message-core/sendMessage")
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
          .bodyValue(messageDTO)
          .exchange()
          .expectStatus().isAccepted()
          .expectBody(SendResponseDTO.class)
          .consumeWith(response -> {
            SendResponseDTO responseBody = response.getResponseBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getOutcome()).isEqualTo("NO_CHANNELS_ENABLED");
          });

      mockServerClient.verify(
          request()
              .withPath("/emd/citizen/filter/" + TEST_FISCAL_CODE)
              .withMethod("GET")
      );

      // Poll to verify that NO messages are present
      ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));

      assertThat(records.isEmpty())
          .as("No messages should be enqueued when citizen has no channels enabled")
          .isTrue();
    }

    log.info("Test completed: message was not enqueued as expected when NO CHANNELS ENABLED");
  }

  @Test
  void shouldHandleError_WhenCitizenConnectorFails() throws Exception {
    mockServerClient
        .when(request()
            .withPath("/emd/citizen/filter/" + TEST_FISCAL_CODE)
            .withMethod("GET"))
        .respond(response()
            .withStatusCode(500));

    MessageDTO messageDTO = createTestMessageDTO(TEST_MESSAGE_ID, TEST_FISCAL_CODE, Channel.SEND);

    webTestClient.post()
        .uri("/emd/message-core/sendMessage")
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .bodyValue(messageDTO)
        .exchange()
        .expectStatus().is5xxServerError()
        .expectHeader().contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .expectBody(ErrorDTO.class)
        .consumeWith(response -> {
          ErrorDTO errorDTO = response.getResponseBody();
          assertThat(errorDTO).isNotNull();
          assertThat(errorDTO.getCode()).isNotEmpty();
          assertThat(errorDTO.getMessage()).isNotEmpty();
          log.info("Error response: code={}, message={}",
              errorDTO.getCode(), errorDTO.getMessage());
        });
  }



  // ============ MOCK SETUP METHODS ============

  /**
   * Setup mock for Citizen Connector
   */
  private void setupCitizenConnectorMock(String fiscalCode, String response) {
    mockServerClient
        .when(request()
            .withPath("/emd/citizen/filter/" + fiscalCode )
            .withMethod("GET"))
        .respond(response()
            .withStatusCode(200)
            .withContentType(MediaType.APPLICATION_JSON)
            .withBody(response));
  }


  // ============ HELPER METHODS ============

  private MessageDTO createTestMessageDTO(String messageId, String recipientId, Channel channel) {
    return MessageDTO.builder()
        .messageId(messageId)
        .recipientId(recipientId)
        .channel(channel)
        .triggerDateTime(Instant.now().toString())
        .senderDescription("Test Sender")
        .messageUrl("https://example.com/message/" + messageId)
        .originId("ORIGIN_" + messageId)
        .title("title")
        .content("Test content for " + messageId)
        .associatedPayment(true)
        .workflowType(WorkflowType.DIGITAL)
        .build();
  }

  /**
   * Creates a Kafka consumer for testing message enqueuing
   */
  private KafkaConsumer<String, String> createTestConsumer(String topic, String groupId) {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
    consumer.subscribe(Collections.singletonList(topic));

    // Initial poll for consumer readiness
    consumer.poll(Duration.ofMillis(100));

    return consumer;
  }

}