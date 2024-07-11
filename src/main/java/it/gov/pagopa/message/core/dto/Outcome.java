package it.gov.pagopa.message.core.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import it.gov.pagopa.message.core.enums.OutcomeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class Outcome {
    @JsonAlias("outcome")
    private OutcomeStatus outcomeStatus;
}
