package it.gov.pagopa.message.event.producer;


import it.gov.pagopa.message.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Value;


/**
 * <p>Producer component for sending messages to the message broker.</p>
 *
 * <p>Uses Spring Cloud Stream {@link StreamBridge} to publish messages to the configured binder.</p>
 */
@Component
@Slf4j
public class MessageProducer {

  private final String binder;
  private final StreamBridge streamBridge;

  public MessageProducer(StreamBridge streamBridge,
                         @Value("${spring.cloud.stream.bindings.messageSender-out-0.binder}")String binder) {
    this.streamBridge = streamBridge;
    this.binder = binder;
  }

  /**
   * <p>Sends a message to the message broker queue.</p>
   *
   * <p>Flow:</p>
   * <ol>
   *   <li>Extract message ID from payload for logging.</li>
   *   <li>Send message to {@code messageSender-out-0} binding via configured binder.</li>
   *   <li>Throw {@link MessageDeliveryException} if the broker did not accept the message,
   *       so the caller can avoid returning a success response for a lost message.</li>
   * </ol>
   *
   * @param message the message to be scheduled and sent
   * @throws MessageDeliveryException if {@code streamBridge.send} returns {@code false}
   */
  public void scheduleMessage(Message<MessageDTO> message) {
    String messageId = message.getPayload().getMessageId();
    log.info("[MESSAGE-CORE][SCHEDULE-MESSAGE] Scheduling message ID: {} to messageSenderQueue", messageId);
    // streamBridge.send() returns false when the message could not be handed to the broker.
    // Combined with producer 'acks=all', a true result means the message was acknowledged
    // by the Kafka leader and required replicas, so it is durably stored.
    boolean sent = streamBridge.send("messageSender-out-0", binder, message);
    if (!sent) {
      log.error("[MESSAGE-CORE][SCHEDULE-MESSAGE] Broker did NOT accept message ID: {}. Failing the request.", messageId);
      throw new MessageDeliveryException(
              "Kafka broker did not accept message ID: " + messageId);
    }
    log.info("[MESSAGE-CORE][SCHEDULE-MESSAGE] Message ID: {} accepted by broker.", messageId);
  }
}



