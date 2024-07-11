package it.gov.pagopa.message.core.service;

import it.gov.pagopa.message.core.dto.MessageDTO;
import it.gov.pagopa.message.core.dto.Outcome;


public interface MessageCoreService {

    Outcome sendMessage(MessageDTO messageDTO);
}
