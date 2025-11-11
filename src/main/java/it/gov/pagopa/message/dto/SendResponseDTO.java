package it.gov.pagopa.message.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * DTO representing the response after sending a message.
 */
@Data
@NoArgsConstructor
@Builder
public class SendResponseDTO {

    /** Outcome of the send operation */
    private String outcome;

    public SendResponseDTO(String outcome){
        this.outcome = outcome;
    }
}
