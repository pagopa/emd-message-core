package it.gov.pagopa.message.dto;

import it.gov.pagopa.common.utils.CommonUtilities;
import it.gov.pagopa.message.enums.Channel;
import it.gov.pagopa.message.enums.WorkflowType;
import it.gov.pagopa.message.validator.ValidAnalogScheduling;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
@Builder(toBuilder = true)
@ValidAnalogScheduling
public class MessageDTO {

    /**
     * Unique identifier of the message.
     */
    @Size(min = 1, max = 100, message = "The messageId field must be between 1 and 100")
    @NotBlank(message = "The messageId field is required")
    private String messageId;

    /**
     * Unique identifier of the recipient (e.g., fiscal code).
     */
    @Size(min = 1, max = 100, message = "The recipientId field must be between 1 and 100")
    @NotBlank(message = "The recipientId field is required")
    private String recipientId;

    /**
     * Date and time when the message was originated. <br>
     * Expected format: ISO 8601 date-time string.
     */
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])T([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9](\\.\\d{1,9})?(Z|[+-]\\d{2}:\\d{2})$",
            message = "The date format must be ISO 8601 (es. YYYY-MM-DDTHH:mm:ssZ)")
    @NotBlank(message = "The triggerDateTime field is required")
    private String triggerDateTime;

    /**
     * Description of the message sender.
     */
    @Size(min = 1, max = 250, message = "The senderDescription field must be between 1 and 250")
    @NotBlank(message = "The senderDescription field is required")
    private String senderDescription;

    /**
     * URL to retrieve the original message.
     */
    @Size(min = 1, max = 2048, message = "The messageUrl field must be between 1 and 2048")
    @NotBlank(message = "The messageUrl field is required")
    private String messageUrl;

    /**
     * ID of the original message (e.g., IUN ).
     */
    @Size(min = 1, max = 100, message = "The originId field must be between 1 and 100")
    @NotBlank(message = "The originId field is required")
    private String originId;

    /**
     * Text content displayed in the header section.
     */
    @Size(min = 1, max = 250, message = "The title field must be between 1 and 250")
    @NotBlank(message = "The title field is required")
    private String title;

    /**
     * Message content in Markdown format, dynamic based on workflowType (ANALOG/DIGITAL)
     */
    @Size(min = 1, max = 100000, message = "The content field must be between 1 and 100000")
    @NotBlank(message = "The content field is required")
    private String content;

    /**
     * Flag indicating whether this notification is associated with a PagoPA payment transaction.
     */
    private Boolean associatedPayment = false;

    public void setAssociatedPayment(Boolean associatedPayment) {
        this.associatedPayment = associatedPayment != null ? associatedPayment : false;
    }

    /**
     * Expiry date for the 5-day deadline. <br>
     * Required only when workflowType is ANALOG.
     */
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])T([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9](\\.\\d{1,9})?(Z|[+-]\\d{2}:\\d{2})$",
            message = "The date format must be ISO 8601 (es. YYYY-MM-DDTHH:mm:ssZ)")
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
    @NotNull(message = "The workflowType field is required")
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
                ", analogSchedulingDate='" + analogSchedulingDate + '\'' +
                ", channel='" + channel + '\'' +
                ", workflowType='" + workflowType + '\'' +
                '}';
    }
}
