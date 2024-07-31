package it.gov.pagopa.message.core.service;

import it.gov.pagopa.message.core.dto.MessageDTO;
import it.gov.pagopa.message.core.dto.TokenDTO;
import it.gov.pagopa.message.core.stub.model.MessageMapperDTOToObject;
import it.gov.pagopa.message.core.stub.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static it.gov.pagopa.common.utils.Utils.logInfo;

@Service
public class SendMessageServiceImpl implements SendMessageService {

    private final RestTemplate restTemplate;
    private final MessageErrorProducerService errorProducerService;

    private final MessageRepository messageRepository;

    private final MessageMapperDTOToObject mapperDTOToObject;

    private final String client;
    private final String clientId;
    private final String grantType;

    private final String tenantId;
    public SendMessageServiceImpl(MessageErrorProducerService errorProducerService,
                                  RestTemplate restTemplate,
                                  MessageRepository messageRepository, MessageMapperDTOToObject mapperDTOToObject, @Value("${app.token.client}")String client,
                                  @Value("${app.token.clientId}") String clientId,
                                  @Value("${app.token.grantType}") String grantType,
                                  @Value("${app.token.tenantId}") String tenantId) {
        this.restTemplate = restTemplate;
        this.errorProducerService = errorProducerService;
        this.messageRepository = messageRepository;
        this.mapperDTOToObject = mapperDTOToObject;
        this.client = client;
        this.clientId = clientId;
        this.grantType = grantType;
        this.tenantId = tenantId;
    }

    @Override
    public void sendMessage(MessageDTO messageDTO, String messageUrl, String authenticationUrl) {
        try {
            TokenDTO token = getToken(authenticationUrl);
            toUrl(messageDTO, messageUrl, token);
        }
        catch (RestClientException | NullPointerException e) {
            logInfo("[EMD][SEND-MESSAGE] Error while sending message");
            errorProducerService.sendError(messageDTO,messageUrl,authenticationUrl);
        }
    }

    @Override
    public void sendMessage(MessageDTO messageDTO, String messageUrl, String authenticationUrl, long retry) {
        try {
            TokenDTO token = getToken(authenticationUrl);
            toUrl(messageDTO, messageUrl, token);
        }
        catch (RestClientException | NullPointerException e) {
            logInfo("[EMD][SEND-MESSAGE] Error while sending message");
            errorProducerService.sendError(messageDTO,messageUrl,authenticationUrl,retry);
        }
    }

    private TokenDTO getToken(String authenticationUrl) throws RestClientException, NullPointerException{

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_secret", client);
        map.add("client_id", clientId);
        map.add("grant_type", grantType);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map,headers);
        TokenDTO token;

        authenticationUrl = authenticationUrl.replace("tenantId",tenantId);
        logInfo("[EMD][SEND-MESSAGE] Getting Token");
        token = restTemplate.exchange(
                authenticationUrl,
                HttpMethod.POST,
                entity,
                TokenDTO.class).getBody();
        logInfo("[EMD][SEND-MESSAGE] Token got");

        return token;
    }

    private void toUrl(MessageDTO messageDTO, String messageUrl, TokenDTO token) throws RestClientException, NullPointerException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token.getAccessToken());
        HttpEntity<MessageDTO> entity = new HttpEntity<>(messageDTO, headers);

        logInfo("[EMD][SEND-MESSAGE] Sending request:%s to: %s".formatted(entity, messageUrl));
        String response = restTemplate.exchange(
                messageUrl,
                HttpMethod.POST,
                entity,
                String.class).getBody();
        logInfo("[EMD][SEND-MESSAGE] Message sent correctly. Response: %s".formatted(response));
        messageRepository.save(mapperDTOToObject.messageObjectMapper(messageDTO));
    }


}