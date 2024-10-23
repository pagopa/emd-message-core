package it.gov.pagopa.message.service;


import it.gov.pagopa.message.dto.MessageDTO;

public interface MessageErrorProducerService {

     void sendError( MessageDTO messageDTO,String messageUrl,String authenticationUrl, String entityId);
     void sendError (MessageDTO messageDTO, String messageUrl, String authenticationUrl, String entityId, long retry);
}
