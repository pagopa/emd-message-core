package it.gov.pagopa.message.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.common.web.exception.InvalidCursorException;
import lombok.RequiredArgsConstructor;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class MessageCursorCodec {

    private final ObjectMapper objectMapper;

    public String encode(MessageSearchCursor cursor) {
        try {
            String json = objectMapper.writeValueAsString(cursor);

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Unable to encode message cursor", e
            );
        }
    }

    public MessageSearchCursor decode(String cursor) {
        MessageSearchCursor decodedCursor;
        
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cursor);
            decodedCursor = objectMapper.readValue(decoded, MessageSearchCursor.class);
        } catch (IllegalArgumentException | IOException e) {
            throw new InvalidCursorException("The provided cursor is malformed", e);
        }

        if (decodedCursor == null) {
            throw new InvalidCursorException("The provided cursor is empty");
        }
        
        if (decodedCursor.id() == null || !ObjectId.isValid(decodedCursor.id())) {
            throw new InvalidCursorException("The cursor contains a missing or invalid 'id'");
        }
        
        if (decodedCursor.messageRegistrationDate() == null) {
            throw new InvalidCursorException("The cursor is missing the registration date");
        }

        return decodedCursor;
    }
}
