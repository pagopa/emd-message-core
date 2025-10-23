package it.gov.pagopa.message.constants;

/**
 * Constants used in the Message Core module.
 */
public class MessageCoreConstants {

    /**
     * Constants for message headers for the {@link org.springframework.messaging.Message}.
     */
    public static final class MessageHeader {
        public static final String ERROR_MSG_AUTH_URL = "authenticationUrl";
        public static final String ERROR_MSG_MESSAGE_URL = "messageUrl";
        public static final String ERROR_MSG_HEADER_RETRY = "retry";

        private MessageHeader() {}
    }
    private MessageCoreConstants() {}
}
