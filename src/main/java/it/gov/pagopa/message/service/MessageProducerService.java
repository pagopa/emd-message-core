package it.gov.pagopa.message.service;


import it.gov.pagopa.message.dto.MessageDTO;

public interface MessageProducerService {

     void enqueueMessage(MessageDTO messageDTO);
}
