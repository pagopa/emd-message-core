package it.gov.pagopa.message.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import it.gov.pagopa.common.reactive.kafka.consumer.BaseKafkaConsumer;
import it.gov.pagopa.message.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static it.gov.pagopa.message.constants.MessageCoreConstants.MessageHeader.*;


@Service
@Slf4j
public class MessageErrorConsumerServiceImpl extends BaseKafkaConsumer<MessageDTO,String> implements MessageErrorConsumerService{

    private final Duration commitDelay;
    private final Duration delayMinusCommit;
    private final ObjectReader objectReader;
    private final SendMessageServiceImpl sendMessageServiceImpl;
    private final long maxRetry;
    public MessageErrorConsumerServiceImpl(ObjectMapper objectMapper,
                                           SendMessageServiceImpl sendMessageServiceImpl,
                                           @Value("${app.retry.max-retry}") long maxRetry,
                                           @Value("${spring.application.name}") String applicationName,
                                           @Value("${spring.cloud.stream.kafka.bindings.consumerCommands-in-0.consumer.ackTime}") long commitMillis,
                                           @Value("${app.message-core.build-delay-duration}") String delayMinusCommit
    ) {
        super(applicationName);
        this.commitDelay = Duration.ofMillis(commitMillis);
        Duration buildDelayDuration = Duration.parse(delayMinusCommit).minusMillis(commitMillis);
        Duration defaultDurationDelay = Duration.ofMillis(2L);
        this.delayMinusCommit = defaultDurationDelay.compareTo(buildDelayDuration) >= 0 ? defaultDurationDelay : buildDelayDuration;
        this.objectReader = objectMapper.readerFor(MessageDTO.class);
        this.maxRetry = maxRetry;
        this.sendMessageServiceImpl = sendMessageServiceImpl;
    }

    @Override
    protected Duration getCommitDelay() {
        return commitDelay;
    }

    @Override
    protected void subscribeAfterCommits(Flux<List<String>> afterCommits2subscribe) {
        afterCommits2subscribe
                .buffer(delayMinusCommit)
                .subscribe(r -> log.info("[MESSAGE-CORE-COMMANDS] Processed offsets committed successfully"));
    }

    @Override
    protected ObjectReader getObjectReader() {
        return objectReader;
    }

//    @Override
//    protected Consumer<Throwable> onDeserializationError(Message<String> message) {
//        return null;
//    }
//
//    @Override
//    protected void notifyError(Message<String> message, Throwable e) {
//
//    }

    @Override
    protected Mono<String> execute(MessageDTO messageDTO, Message<String> message, Map<String, Object> ctx) {
        log.info("[EMD-PROCESS-COMMAND] Queue message received: {}",message.getPayload());
        MessageHeaders headers = message.getHeaders();
        long retry = getNextRetry(headers);
        if(retry!=0) {
            String messageUrl = (String) headers.get(ERROR_MSG_MESSAGE_URL);
            String authenticationUrl = (String) headers.get(ERROR_MSG_AUTH_URL);
            String entidyId = (String) headers.get(ERROR_MSG_ENTITY_ID);
            log.info("[EMD-PROCESS-COMMAND] Try {} for message {}",retry,messageDTO.getMessageId());
            sendMessageServiceImpl.sendMessage(messageDTO, messageUrl, authenticationUrl, entidyId,retry)
                    .subscribe();
        }
        else
            log.info("[EMD-PROCESS-COMMAND] Message {} not retryable", messageDTO.getMessageId());
        return Mono.empty();
    }

    private long getNextRetry(MessageHeaders headers) {
        Long retry = (Long) headers.get(ERROR_MSG_HEADER_RETRY);
        if (retry != null && (retry >=0 && retry<maxRetry))
            return 1 + retry;
        return 0;
    }

}
