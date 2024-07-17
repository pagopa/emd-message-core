package it.gov.pagopa.message.core.service;

import it.gov.pagopa.message.core.dto.MessageDTO;
import it.gov.pagopa.message.core.dto.TokenDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
@Service
@Slf4j
public class SendMessageServiceImpl implements SendMessageService {

    private final RestTemplate restTemplate;
    private final MessageErrorProducerService errorProducerService;

    private final String client;
    private final String clientId;
    private final String grantType;

    private final String tenantId;
    public SendMessageServiceImpl(MessageErrorProducerService errorProducerService,
                                  RestTemplate restTemplate,
                                  @Value("${app.token.client}")String client,
                                  @Value("${app.token.clientId}") String clientId,
                                  @Value("${app.token.grantType}") String grantType,
                                  @Value("${app.token.tenantId}") String tenantId) {
        this.restTemplate = restTemplate;
        this.errorProducerService = errorProducerService;
        this.client = client;
        this.clientId = clientId;
        this.grantType = grantType;
        this.tenantId = tenantId;
    }

    @Override
    public void sendMessage(MessageDTO messageDTO, String messageUrl, String authenticationUrl) {
        try {
            toUrl(messageDTO, messageUrl, getToken(authenticationUrl));
        }
        catch (Exception e) {
            log.info("[EMD][SEND-MESSAGE] Error while sending message");
            errorProducerService.sendError(messageDTO,messageUrl,authenticationUrl);
        }
    }

    @Override
    public void sendMessage(MessageDTO messageDTO, String messageUrl, String authenticationUrl, long retry) {
        try {
            toUrl(messageDTO, messageUrl, getToken(authenticationUrl));
        }
        catch (Exception e) {
            log.info("[EMD][SEND-MESSAGE] Error while sending message");
            errorProducerService.sendError(messageDTO,messageUrl,authenticationUrl,retry);
        }
    }

    private TokenDTO getToken(String authenticationUrl) throws Exception{

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_secret", client);
        map.add("client_id", clientId);
        map.add("grant_type", grantType);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map,headers);
        TokenDTO token;

        authenticationUrl = authenticationUrl.replace("tenantId",tenantId);
        log.info("[EMD][SEND-MESSAGE] Getting Token");
        token = restTemplate.exchange(
                authenticationUrl,
                HttpMethod.POST,
                entity,
                TokenDTO.class).getBody();
        log.info("[EMD][SEND-MESSAGE] Token got: {}",token);

        return token;
    }

    private void toUrl(MessageDTO messageDTO, String messageUrl, TokenDTO token) throws Exception{

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token.getAccessToken());
        HttpEntity<MessageDTO> entity = new HttpEntity<>(messageDTO, headers);

        log.info("[EMD][SEND-MESSAGE] Sending request:{} to: {}",entity, messageUrl);
        String response = restTemplate.exchange(
                messageUrl,
                HttpMethod.POST,
                entity,
                String.class).getBody();
        log.info("[EMD][SEND-MESSAGE] Message sent correctly. Response: {}",response);

    }


}