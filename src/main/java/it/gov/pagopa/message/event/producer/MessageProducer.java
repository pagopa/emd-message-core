package it.gov.pagopa.message.event.producer;


import it.gov.pagopa.message.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
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
   * </ol>
   *
   * @param message the message to be scheduled and sent
   */
  public void scheduleMessage(Message<MessageDTO> message) {
    String messageId = message.getPayload().getMessageId();
    log.info("[MESSAGE-CORE][SCHEDULE-MESSAGE] Scheduling message ID: {} to messageSenderQueue", messageId);
    streamBridge.send("messageSender-out-0", binder, message);
  }
}



