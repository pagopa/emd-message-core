package it.gov.pagopa.message.core.service;

import it.gov.pagopa.message.core.connector.citizen.CitizenConnectorImpl;
import it.gov.pagopa.message.core.connector.tpp.TppConnectorImpl;
import it.gov.pagopa.message.core.dto.*;
import it.gov.pagopa.message.core.enums.OutcomeStatus;
import it.gov.pagopa.common.utils.Utils;
import it.gov.pagopa.message.core.model.Channel;
import it.gov.pagopa.message.core.model.CitizenConsent;
import it.gov.pagopa.message.core.dto.Outcome;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@Slf4j
@Service
public class MessageCoreServiceImpl implements MessageCoreService {

    private final RestTemplate restTemplate;
    private final CitizenConnectorImpl citizenService;
    private final TppConnectorImpl tppService;
    public MessageCoreServiceImpl(CitizenConnectorImpl citizenService,
                                  TppConnectorImpl tppService,
                                  RestTemplate restTemplate
                                        ) {
        this.restTemplate = restTemplate;
        this.tppService = tppService;
        this.citizenService = citizenService;
    }

    @Override
    public Outcome sendMessage(MessageDTO messageDTO) {

        log.info("[EMD][SEND-MESSAGE] Recived message: {}",messageDTO);
        String hashedFiscalCode = Utils.createSHA256(messageDTO.getRecipientId());
        ArrayList<CitizenConsent> citizenConsentList =
                citizenService.getCitizenConsentsEnabled(hashedFiscalCode);

        if(citizenConsentList.isEmpty()) {
            log.info("[EMD][SEND-MESSAGE] Citizen consent list is empty");
            return new Outcome(OutcomeStatus.NO_CHANNELS_ENABLED);
        }

        log.info("[EMD][SEND-MESSAGE] Citizen consent list: {}",citizenConsentList);
        List<Channel> channelList = tppService.getChannelsList(
                                citizenConsentList
                                .stream()
                                .map(CitizenConsent::getChannelId)
                                .toList()
                        );

        if(channelList.isEmpty()) {
            log.info("[EMD][SEND-MESSAGE] Channel list is empty");
            return new Outcome(OutcomeStatus.NO_CHANNELS_ENABLED);
        }
        log.info("[EMD][SEND-MESSAGE] Channel list: {}",channelList);

        for (CitizenConsent citizenConsent : citizenConsentList) {
            Iterator<Channel> iterator = channelList.iterator();
            while (iterator.hasNext()) {
                Channel channel = iterator.next();
                if (channel.getId().equals(citizenConsent.getChannelId())) {
                    sendMessageToUri(
                            messageDTO,
                            channel.getMessageUrl(),
                            getToken(channel.getAuthenticationUrl(),messageDTO.getRecipientId()));
                    iterator.remove();
                    break;
                }
            }
        }

        return new Outcome(OutcomeStatus.OK);
    }

    private String getToken(String messageUrl, String fiscalCode){

        HttpHeaders headers = new HttpHeaders();
        headers.set("RequestId","00000000-0000-0000-0000-000000000006");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String clientSecret = "64b5f14d-56ef-4d14-8b65-ff7bc5d4661e";
        String clientId = "6010064c-ec73-4fa5-9ed5-5446af8920cf";
        String grantType = "client_credentials";
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_secret", clientSecret);
        map.add("client_id", clientId);
        map.add("grant_type", grantType);
        map.add("fiscal_code", fiscalCode);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map,headers);

        log.info("[EMD][SEND-MESSAGE] Getting Token");
         String token = restTemplate.exchange(
                    messageUrl,
                    HttpMethod.POST,
                    entity,
                    String.class).getBody();
        log.info("[EMD][SEND-MESSAGE] Token got");

        return token;
    }

    private void sendMessageToUri(MessageDTO messageDTO, String messageUrl, String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<MessageDTO> entity = new HttpEntity<>(messageDTO, headers);


         try {
             log.info("[EMD][SEND-MESSAGE] Sending message to: {}",messageDTO.toString());
             restTemplate.exchange(
                    messageUrl,
                    HttpMethod.POST,
                    entity,
                    String.class);
             log.info("[EMD][SEND-MESSAGE] Message sent");
         }
         catch(Exception e){
             log.info("[EMD][SEND-MESSAGE] Message error");
         }

    }
}
