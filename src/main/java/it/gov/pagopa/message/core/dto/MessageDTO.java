package it.gov.pagopa.message.core.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class MessageDTO {
    private String messageId;
    private String recipientId;
    private ZonedDateTime triggerDateTime;
    private String senderDescription;
    private String messageUrl;
    private String originId;
    private String message;
}
