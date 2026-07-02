package it.gov.pagopa.message.config;

import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Customizes the Jackson {@link com.fasterxml.jackson.databind.ObjectMapper} to enforce strict
 * type coercion, preventing silent conversion of integers and booleans into textual fields.
 */
@Configuration
public class JacksonConfig {

    /**
     * Disallows coercion of {@link CoercionInputShape#Integer} and {@link CoercionInputShape#Boolean}
     * into textual types, causing deserialization to fail on unexpected input types.
     *
     * @return a customizer that applies the coercion restrictions to the global {@code ObjectMapper}
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer coercionCustomizer() {
        return builder -> builder.postConfigurer(objectMapper ->
            objectMapper.coercionConfigFor(LogicalType.Textual)
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail)
        );
    }
}