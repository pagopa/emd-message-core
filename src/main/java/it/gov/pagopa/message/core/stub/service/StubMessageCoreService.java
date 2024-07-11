package it.gov.pagopa.message.core.stub.service;

import it.gov.pagopa.message.core.dto.MessageDTO;
import it.gov.pagopa.message.core.stub.model.Message;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;


@Service
public interface StubMessageCoreService {

    ArrayList<Message> getMessages(String fiscalCode);

    void saveMessage(MessageDTO messageDTO);
}
