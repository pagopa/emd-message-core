package it.gov.pagopa.message.stub.controller;


import it.gov.pagopa.message.dto.MessageDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;


@RequestMapping("stub/emd/message/")
public interface StubMessageCoreController {


    @GetMapping("/get/{fiscalCode}")
    Mono<ResponseEntity<List<MessageDTO>>> getMessages(@Valid @PathVariable String fiscalCode);
}
