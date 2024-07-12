package it.gov.pagopa.message.core.stub.controller;

import it.gov.pagopa.message.core.dto.MessageDTO;
import it.gov.pagopa.message.core.stub.model.Message;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("stub/emd/message-core")
public interface StubMessageCoreController {


    @PostMapping("/save")
    ResponseEntity<String> saveMessage(@Valid @RequestBody MessageDTO messageDTO);

    @GetMapping("/get/{fiscalCode}")
    ResponseEntity<ArrayList<Message>> getMessages(@Valid @PathVariable String fiscalCode);
}
