package it.gov.pagopa.message.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class SendResponseDTO {
    private String outcome;

    public SendResponseDTO(String outcome){
        this.outcome = outcome;
    }
}
