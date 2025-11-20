package it.gov.pagopa.message.utils.faker;



import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.enums.Channel;
import it.gov.pagopa.message.enums.WorkflowType;

public class MessageDTOFaker {
    public static MessageDTO mockInstance() {
        return MessageDTO.builder()
                .messageId("messageId")
                .recipientId("recipientId")
                .triggerDateTime("date")
                .analogSchedulingDate("date")
                .senderDescription("sender")
                .messageUrl("messageUrl")
                .originId("originId")
                .content("message")
                .associatedPayment(true)
                .idPsp("originId")
                .workflowType(WorkflowType.valueOf("ANALOG"))
                .channel(Channel.valueOf("SEND"))
                .build();

    }

}
