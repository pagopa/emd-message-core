package it.gov.pagopa.message.faker;

import it.gov.pagopa.message.model.Message;

public class MessageFaker {

    private MessageFaker(){}
    public static Message mockInstance() {
        return Message.builder()
                .messageId("messageId")
                .messageUrl("messageUrl")
                .content("message")
                .triggerDateTime("date")
                .senderDescription("sender")
                .hashedFiscalCode("recipientId")
                .originId("originId")
                .entityId("entityId")
                .build();

    }
}
