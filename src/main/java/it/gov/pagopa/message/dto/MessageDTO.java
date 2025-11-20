package it.gov.pagopa.message.dto;

import it.gov.pagopa.common.utils.CommonUtilities;
import it.gov.pagopa.message.enums.Channel;
import it.gov.pagopa.message.enums.WorkflowType;
import it.gov.pagopa.message.validator.ValidAnalogScheduling;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO with all the information needed to send a notification message
 * through TTP applications to citizens.
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
@ValidAnalogScheduling
public class MessageDTO {

    /**
     * Unique identifier of the message.
     */
    private String messageId;

    /**
     * Unique identifier of the recipient (e.g., fiscal code).
     */
    private String recipientId;

    /**
     * Date and time when the message was originated. <br>
     * Expected format: ISO 8601 date-time string.
     */
    private String triggerDateTime;

    /**
     * Description of the message sender.
     */
    private String senderDescription;

    /**
     * URL to retrieve the original message.
     */
    private String messageUrl;

    /**
     * ID of the original message (e.g., IUN ).
     */
    private String originId;

    /**
     * Text content displayed in the header section.
     */
    private String title;

    /**
     * Message content in Markdown format, dynamic based on workflowType (ANALOG/DIGITAL)
     */
    private String content;

    /**
     * Flag indicating whether this notification is associated with a PagoPA payment transaction.
     */
    private Boolean associatedPayment;

    /**
     * Identifier of the Payment Service Provider (PSP).
     */
    private String idPsp;

    /**
     * Expiry date for the 5-day deadline. <br>
     * Required only when workflowType is ANALOG.
     */
    private String analogSchedulingDate;

    /**
     * Communication channel through which the courtesy message originated.
     *
     * @see Channel
     */
    private Channel channel;

    /**
     *  Indetifier of the workflow type.
     *
     * @see WorkflowType
     */
    private WorkflowType workflowType;

    @Override
    public String toString() {

        return "MessageDTO{" +
                "messageId='" + messageId + '\'' +
                ", recipientId='" + CommonUtilities.createSHA256(recipientId) + '\'' +
                ", triggerDateTime='" + triggerDateTime + '\'' +
                ", senderDescription='" + senderDescription + '\'' +
                ", messageUrl='" + messageUrl + '\'' +
                ", originId='" + originId + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", idPsp='" + idPsp + '\'' +
                ", analogSchedulingDate='" + analogSchedulingDate + '\'' +
                ", channel='" + channel + '\'' +
                ", workflowType='" + workflowType + '\'' +
                '}';
    }
}
