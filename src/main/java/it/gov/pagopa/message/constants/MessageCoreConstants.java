package it.gov.pagopa.message.constants;

import java.util.Set;

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

    /**
     * Container class for Message exception codes used in structured error responses.
     */
    public static final class ExceptionCode {

        public static final String INVALID_SEARCH_FIELD = "INVALID_SEARCH_FIELD";
        private ExceptionCode() {}
    }

    /**
     * Container class for Message exception messages used in error responses and logging.
     */
    public static final class ExceptionMessage {

        public static final String INVALID_SEARCH_FIELD = "One or more requested 'fields' are not allowed for search projection";
        private ExceptionMessage() {}
    }

    /**
     * Container class for Message exception names used in exception mapping and factory patterns.
     */
    public static final class ExceptionName {

        public static final String INVALID_SEARCH_FIELD = "INVALID_SEARCH_FIELD";
        private ExceptionName() {}
    }

    /**
     * Container class defining the fields available in {@code MessageDTO} that
     * can be requested for projection in the {@code searchMessages} operation, together with the
     * default set of fields returned when no override is provided (the ones shown in the grid).
     */
    public static final class SearchFields {

        public static final String ENTITY_ID = "entityId";
        public static final String MESSAGE_ID = "messageId";
        public static final String RECIPIENT_ID = "recipientId";
        public static final String TRIGGER_DATE_TIME = "triggerDateTime";
        public static final String REGISTRATION_DATE = "messageRegistrationDate";
        public static final String SENDER_DESCRIPTION = "senderDescription";
        public static final String MESSAGE_URL = "messageUrl";
        public static final String MESSAGE_STATE = "messageState";
        public static final String ORIGIN_ID = "originId";
        public static final String TITLE = "title";
        public static final String CONTENT = "content";
        public static final String ASSOCIATED_PAYMENT = "associatedPayment";
        public static final String ANALOG_SCHEDULING_DATE = "analogSchedulingDate";
        public static final String CHANNEL = "channel";
        public static final String WORKFLOW_TYPE = "workflowType";
        public static final String ID_PSP = "idPsp";


        /**
         * All the fields that can be requested through the {@code fields} search parameter.
         */
        public static final Set<String> ALLOWED = Set.of(
                MESSAGE_STATE, MESSAGE_ID, RECIPIENT_ID, TRIGGER_DATE_TIME, SENDER_DESCRIPTION,
                MESSAGE_URL, ORIGIN_ID, TITLE, CONTENT, ASSOCIATED_PAYMENT,
                ANALOG_SCHEDULING_DATE, CHANNEL, WORKFLOW_TYPE, ID_PSP, ENTITY_ID, REGISTRATION_DATE
        );

        /**
         * Default fields returned by {@code searchMessages} when no {@code fields} override is
         * provided, matching the columns shown in the message grid.
         */
        public static final Set<String> DEFAULT_GRID_FIELDS = Set.of(
                MESSAGE_STATE, ORIGIN_ID, MESSAGE_ID, RECIPIENT_ID, ID_PSP, WORKFLOW_TYPE, REGISTRATION_DATE
        );

        private SearchFields() {}
    }

    private MessageCoreConstants() {}
}
