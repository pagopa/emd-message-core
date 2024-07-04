package it.gov.pagopa.message.core.service;


import it.gov.pagopa.message.core.dto.MessageDTO;


public interface SendMessageService {
    void sendMessage(MessageDTO messageDTO, String messageUrl, String authenticationUrl);
    void sendMessage(MessageDTO messageDTO, String messageUrl, String authenticationUrl, long retry);

}
