package it.gov.pagopa.message.core.event.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import it.gov.pagopa.message.core.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageErrorProducer {

  @Value("${spring.cloud.stream.bindings.messageSender-out-0.binder}")
  private final String binder;

  private final StreamBridge streamBridge;

  public MessageErrorProducer( @Value("${spring.cloud.stream.bindings.messageSender-out-0.binder}")String messageErrorOut0,
                               StreamBridge streamBridge) {
    this.binder = messageErrorOut0;
    this.streamBridge = streamBridge;
  }

  public void sendToMessageErrorQueue(Message<MessageDTO> message){
    log.debug("Sending messsage to queue");
    streamBridge.send("messageSender-out-0",binder, message);
  }


}
