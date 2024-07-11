package it.gov.pagopa.message.core.stub.service;

import it.gov.pagopa.message.core.dto.MessageDTO;
import it.gov.pagopa.message.core.stub.model.Message;
import it.gov.pagopa.message.core.stub.model.MessageMapperDTOToObject;
import it.gov.pagopa.message.core.stub.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static it.gov.pagopa.common.utils.Utils.createSHA256;

@Service
public class StubMessageCoreServiceImpl implements StubMessageCoreService {

    private final MessageRepository messageRepository;
    private final MessageMapperDTOToObject mapperDTOToObject;

    public StubMessageCoreServiceImpl(MessageRepository messageRepository, MessageMapperDTOToObject mapperDTOToObject) {
        this.messageRepository = messageRepository;
        this.mapperDTOToObject = mapperDTOToObject;
    }

    @Override
    public ArrayList<Message> getMessages(String fiscalCode) {
        return messageRepository.findByHashedFiscalCode(createSHA256(fiscalCode));
    }

    @Override
    public void saveMessage(MessageDTO messageDTO) {
        messageRepository.save(mapperDTOToObject.messageObjectMapper(messageDTO));
    }
}
