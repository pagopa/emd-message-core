package it.gov.pagopa.message.constants;

/**
 * <p>Constants used in the Message Core module.</p>
 *
 * <p>Centralizes configuration values, header names, and validation patterns.</p>
 */
public class MessageCoreConstants {

    /**
     * <p>Header constants for {@link org.springframework.messaging.Message}.</p>
     *
     * <p>Used to attach metadata during message processing and error handling.</p>
     */
    public static final class MessageHeader {
        public static final String ERROR_MSG_AUTH_URL = "authenticationUrl";
        public static final String ERROR_MSG_MESSAGE_URL = "messageUrl";
        public static final String ERROR_MSG_HEADER_RETRY = "retry";

        private MessageHeader() {}
    }
    private MessageCoreConstants() {}
}
