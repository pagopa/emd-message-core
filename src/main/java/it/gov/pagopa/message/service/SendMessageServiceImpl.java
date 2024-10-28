package it.gov.pagopa.message.service;

import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.dto.TokenDTO;
import it.gov.pagopa.message.model.Message;
import it.gov.pagopa.message.dto.MessageMapperDTOToObject;
import it.gov.pagopa.message.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class SendMessageServiceImpl implements SendMessageService {

    private final WebClient webClient;
    private final MessageProducerService errorProducerService;

    private final MessageRepository messageRepository;

    private final MessageMapperDTOToObject mapperDTOToObject;
    private final String client;
    private final String clientId;
    private final String grantType;
    private final String tenantId;

    public SendMessageServiceImpl(MessageProducerService errorProducerService,
                                  MessageRepository messageRepository, MessageMapperDTOToObject mapperDTOToObject, @Value("${app.token.client}") String client,
                                  @Value("${app.token.clientId}") String clientId,
                                  @Value("${app.token.grantType}") String grantType,
                                  @Value("${app.token.tenantId}") String tenantId) {
        this.webClient = WebClient.builder().build();
        this.errorProducerService = errorProducerService;
        this.messageRepository = messageRepository;
        this.mapperDTOToObject = mapperDTOToObject;
        this.client = client;
        this.clientId = clientId;
        this.grantType = grantType;
        this.tenantId = tenantId;
    }

    @Override
    public Mono<Void> sendMessage(MessageDTO messageDTO, String messageUrl, String authenticationUrl, String entityId) {
        return getToken(authenticationUrl)
                .flatMap(token -> toUrl(messageDTO, messageUrl, token, entityId))
                .onErrorResume(e -> {
                    log.error("[EMD-MESSAGE-CORE][SEND]Error while sending message");
                    errorProducerService.enqueueMessage(messageDTO, messageUrl, authenticationUrl,entityId);
                    return null;
                })
                .then();
    }

    @Override
    public Mono<Void> sendMessage(MessageDTO messageDTO, String messageUrl, String authenticationUrl, String entityId, long retry) {
        return getToken(authenticationUrl)
                .flatMap(token -> toUrl(messageDTO, messageUrl, token, entityId))
                .onErrorResume(e -> {
                    log.error("[EMD-MESSAGE-CORE][SEND]Error while sending message");
                    errorProducerService.enqueueMessage(messageDTO, messageUrl, authenticationUrl,entityId,retry);
                    return null;
                })
                .then();
    }

    private Mono<TokenDTO> getToken(String authenticationUrl) {
        authenticationUrl = authenticationUrl.replace("tenantId", tenantId);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_secret", client);
        formData.add("client_id", clientId);
        formData.add("grant_type", grantType);

        return webClient.post()
                .uri(authenticationUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(TokenDTO.class)
                .doOnSuccess(token -> log.info("[EMD-MESSAGE-CORE][SEND]Token obtained: {}", token))
                .doOnError(error -> log.error("[EMD-MESSAGE-CORE][SEND]Error getting token"));
    }

    private Mono<String> toUrl(MessageDTO messageDTO, String messageUrl, TokenDTO token, String entityId ) {
        return webClient.post()
                .uri(messageUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(messageDTO)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> {
                    log.info("[EMD-MESSAGE-CORE][SEND]Message sent. Response: {}", response);
                    Message message = mapperDTOToObject.map(messageDTO,entityId);
                    messageRepository.save(message)
                            .doOnSuccess(messagePersisted -> log.info("[EMD-MESSAGE-CORE][SEND]Message {} save for entityId {}",messagePersisted.getMessageId(),messagePersisted.getEntityId()))
                            .onErrorResume(error -> {
                                log.error("[EMD-MESSAGE-CORE][SEND] Error save message");
                                return Mono.empty();
                            });
                })
                .doOnError(error -> log.error("[EMD-MESSAGE-CORE][SEND]Error sending message"));
    }
}
