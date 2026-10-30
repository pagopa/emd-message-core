package it.gov.pagopa.message.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import it.gov.pagopa.message.enums.MessageState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageSearchParameter {

    private String messageId;
    private String recipientId;
    private String originId;
    private MessageState state;

    @DateTimeFormat (iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private String cursor;
    private Integer size = 10;
    private List<String> fields;

}