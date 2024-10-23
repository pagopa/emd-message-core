package it.gov.pagopa.message.dto;


import it.gov.pagopa.common.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class MessageDTO {
    private String messageId;
    private String recipientId;
    private String triggerDateTime;
    private String senderDescription;
    private String messageUrl;
    private String originId;
    private String content;
    private String entityId;

    @Override
    public String toString() {

        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", recipientId='" + Utils.createSHA256(recipientId) + '\'' +
                ", triggerDateTime='" + triggerDateTime + '\'' +
                ", senderDescription='" + senderDescription + '\'' +
                ", messageUrl='" + messageUrl + '\'' +
                ", originId='" + originId + '\'' +
                '}';
    }
}
