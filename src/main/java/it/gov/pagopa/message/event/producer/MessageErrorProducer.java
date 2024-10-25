package it.gov.pagopa.message.event.producer;


import it.gov.pagopa.message.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;


import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class MessageErrorProducer {

  private final String binder;
  private final StreamBridge streamBridge;

  public MessageErrorProducer(StreamBridge streamBridge,
                              @Value("${spring.cloud.stream.bindings.messageSender-out-0.binder}")String binder) {
    this.streamBridge = streamBridge;
    this.binder = binder;
  }

  public void sendToMessageErrorQueue(Message<MessageDTO> message) {
    log.info("[EMD-MESSAGE-CORE][SEND] Scheduling message {} to queue",message.getPayload().getMessageId());
    streamBridge.send("messageSender-out-0", binder, message);
    }
}



