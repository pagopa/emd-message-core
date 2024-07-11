package it.gov.pagopa.message.core.stub.controller;

import it.gov.pagopa.message.core.dto.MessageDTO;
import it.gov.pagopa.message.core.stub.model.Message;
import it.gov.pagopa.message.core.stub.service.StubMessageCoreServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class StubMessageCoreControllerImpl implements StubMessageCoreController {

    private final StubMessageCoreServiceImpl stubMessageCoreService;

    public StubMessageCoreControllerImpl(StubMessageCoreServiceImpl stubMessageCoreService) {
        this.stubMessageCoreService = stubMessageCoreService;
    }

    @Override
    public ResponseEntity<String> saveMessage(MessageDTO messageDTO) {
        stubMessageCoreService.saveMessage(messageDTO);
        return ResponseEntity.ok("OK");
    }

    @Override
    public ResponseEntity<ArrayList<Message>> getMessages(String fiscalCode) {
        return ResponseEntity.ok(stubMessageCoreService.getMessages(fiscalCode));
    }
}
