package it.gov.pagopa.message.core.service;

import it.gov.pagopa.message.core.dto.MessageDTO;



public interface MessageErrorProducerService {

     void sendError( MessageDTO messageDTO,String messageUrl,String authenticationUrl);
     void sendError (MessageDTO messageDTO, String messageUrl, String authenticationUrl, long retry);
}
