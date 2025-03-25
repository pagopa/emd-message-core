package it.gov.pagopa.message.utils.faker;



import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.enums.Channel;

public class MessageDTOFaker {
    public static MessageDTO mockInstance() {
        return MessageDTO.builder()
                .messageId("messageId")
                .recipientId("recipientId")
                .triggerDateTime("date")
                .senderDescription("sender")
                .messageUrl("messageUrl")
                .originId("originId")
                .content("message")
                .associatedPayment(true)
                .idPsp("originId")
                .channel(Channel.valueOf("SEND"))
                .build();

    }

}
