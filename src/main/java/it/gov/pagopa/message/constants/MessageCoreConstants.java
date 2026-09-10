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

    /**
     * Container class for Message exception codes used in structured error responses.
     */
    public static final class ExceptionCode {

        public static final String MESSAGE_NOT_FOUND = "MESSAGE_NOT_FOUND";
        private ExceptionCode() {}
    }

    /**
     * Container class for Message exception messages used in error responses and logging.
     */
    public static final class ExceptionMessage {

        public static final String MESSAGE_NOT_FOUND = "Message not found";
        private ExceptionMessage() {}
    }

    /**
     * Container class for Message exception names used in exception mapping and factory patterns.
     */
    public static final class ExceptionName {

        public static final String MESSAGE_NOT_FOUND = "MESSAGE_NOT_FOUND";
        private ExceptionName() {}
    }
}
