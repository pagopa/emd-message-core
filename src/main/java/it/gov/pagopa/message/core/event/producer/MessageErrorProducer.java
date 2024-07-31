package it.gov.pagopa.message.core.event.producer;

import it.gov.pagopa.message.core.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static it.gov.pagopa.common.utils.Utils.logInfo;

@Component
public class MessageErrorProducer {

  private final String binder;
  private final StreamBridge streamBridge;
  private final ScheduledExecutorService scheduler;

  public MessageErrorProducer(StreamBridge streamBridge,
                              ScheduledExecutorService scheduler,
                              @Value("${spring.cloud.stream.bindings.messageSender-out-0.binder}")String binder) {
    this.streamBridge = streamBridge;
    this.scheduler = scheduler;
    this.binder = binder;
  }

  public void sendToMessageErrorQueue(Message<MessageDTO> message){
    logInfo("Scheduling message to queue");
      scheduler.schedule(
              () -> streamBridge.send("messageSender-out-0", binder, message),
              5,
              TimeUnit.SECONDS);
    }

}



