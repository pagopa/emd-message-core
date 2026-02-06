package it.gov.pagopa.message.utils.faker;



import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.enums.Channel;
import it.gov.pagopa.message.enums.WorkflowType;

public class MessageDTOFaker {
    
    public static MessageDTO mockInstance() {
        return MessageDTO.builder()
                .messageId("messageId")
                .recipientId("recipientId")
                .triggerDateTime("2023-12-25T10:30:00Z")
                .analogSchedulingDate("2023-12-25T10:30:00Z")
                .senderDescription("sender")
                .messageUrl("messageUrl")
                .originId("originId")
                .title("title")
                .content("message")
                .associatedPayment(true)
                .workflowType(WorkflowType.valueOf("ANALOG"))
                .channel(Channel.valueOf("SEND"))
                .build();
    }

}
