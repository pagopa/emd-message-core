package it.gov.pagopa.message.core.service;

import it.gov.pagopa.message.core.dto.MessageDTO;
import it.gov.pagopa.message.core.dto.TokenDTO;
import lombok.extern.slf4j.Slf4j;
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
    public SendMessageServiceImpl(MessageErrorProducerService errorProducerService,
                                  RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.errorProducerService = errorProducerService;
    }

    @Override
    public void sendMessage(MessageDTO messageDTO, String messageUrl, String authenticationUrl) {
        try {
            toUrl(messageDTO, messageUrl, getToken(authenticationUrl, messageDTO.getRecipientId()));
        }
        catch (Exception e) {
            log.info("[EMD][SEND-MESSAGE] Error while sending message");
            errorProducerService.sendError(messageDTO,messageUrl,authenticationUrl);
        }
    }

    @Override
    public void sendMessage(MessageDTO messageDTO, String messageUrl, String authenticationUrl, long retry) {
        try {
            toUrl(messageDTO, messageUrl, getToken(authenticationUrl, messageDTO.getRecipientId()));
        }
        catch (Exception e) {
            log.info("[EMD][SEND-MESSAGE] Error while sending message");
            errorProducerService.sendError(messageDTO,messageUrl,authenticationUrl,retry);
        }
    }

    private TokenDTO getToken(String authenticationUrl, String recipientId) throws Exception{

        HttpHeaders headers = new HttpHeaders();
        headers.set("RequestId", "00000000-0000-0000-0000-000000000006");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String client = "64b5f14d-56ef-4d14-8b65-ff7bc5d4661e";
        String clientId = "6010064c-ec73-4fa5-9ed5-5446af8920cf";
        String grantType = "client_credentials";
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_secret", client);
        map.add("client_id", clientId);
        map.add("grant_type", grantType);
        map.add("fiscal_code", recipientId);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        TokenDTO token;

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