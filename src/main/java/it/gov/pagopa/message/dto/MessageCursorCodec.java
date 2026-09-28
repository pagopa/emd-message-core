package it.gov.pagopa.message.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cursor);

            return objectMapper.readValue(decoded,MessageSearchCursor.class);

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid message cursor", e
            );
        }
    }
}
