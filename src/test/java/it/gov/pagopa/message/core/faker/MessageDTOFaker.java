package it.gov.pagopa.message.core.faker;

import it.gov.pagopa.message.core.dto.MessageDTO;

public class MessageDTOFaker {

    private MessageDTOFaker(){}
    public static MessageDTO mockInstance() {
        return MessageDTO.builder()
                .messageId("messageId")
                .messageUrl("messageUrl")
                .message("message")
                .triggerDateTime("triggerDateTime")
                .senderDescription("sender")
                .recipientId("recipientId")
                .originId("originId")
                .build();

    }

}
