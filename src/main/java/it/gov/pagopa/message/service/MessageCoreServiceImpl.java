package it.gov.pagopa.message.service;

import it.gov.pagopa.message.connector.CitizenConnectorImpl;
import it.gov.pagopa.message.constants.MessageCoreConstants;
import it.gov.pagopa.message.constants.MessageCoreConstants.ExceptionMessage;
import it.gov.pagopa.message.constants.MessageCoreConstants.ExceptionName;
import it.gov.pagopa.message.config.ExceptionMap;
import it.gov.pagopa.message.connector.CitizenConnector;
import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.dto.ResponseMessageMapperObjectToDTO;
import it.gov.pagopa.message.dto.MessageSearchResponseDTO;
import it.gov.pagopa.message.dto.ResponseMessageDTO;
import it.gov.pagopa.message.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static it.gov.pagopa.common.utils.CommonUtilities.createSHA256;
import static it.gov.pagopa.common.utils.CommonUtilities.inputSanitization;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>Implementation of {@link MessageCoreService}.</p>
 *
 * <p>Orchestrates recipient validation via {@link CitizenConnectorImpl} and message queueing via {@link MessageProducerServiceImpl}.</p>
 */
@Slf4j
@Service
public class MessageCoreServiceImpl implements MessageCoreService {

    private final CitizenConnectorImpl citizenConnector;

    private final MessageProducerServiceImpl messageProducerService;

    private final ResponseMessageMapperObjectToDTO messageMapperObjectToDTO;

    private final ExceptionMap exceptionMap;


    
    /**
     * Maximum page size accepted for search operations. Requests exceeding this value are
     * capped to protect database resources.
     */
    @Value ("${app.tpp.search.max-page-size:100}")
    private int maxPageSize;

    /**
     * Default page size used when the client does not provide a valid {@code size}.
     */
    @Value ("${app.tpp.search.default-page-size:10}")
    private int defaultPageSize;
    
    private final MessageRepository messageRepository;

    public MessageCoreServiceImpl(CitizenConnectorImpl citizenConnector,
                                  MessageProducerServiceImpl messageProducerService,
                                  MessageRepository messageRepository,
                                  ResponseMessageMapperObjectToDTO messageMapperObjectToDTO,
                                  ExceptionMap exceptionMap) {
        this.citizenConnector = citizenConnector;
        this.messageProducerService = messageProducerService;
        this.messageRepository = messageRepository;
        this.messageMapperObjectToDTO = messageMapperObjectToDTO;
        this.exceptionMap = exceptionMap;
    }


    /**
     * <p>Validates recipient eligibility and enqueues message for delivery.</p>
     *
     * <p>Flow:</p>
     * <ul>
     *   <li>Check recipient fiscal code via {@link CitizenConnector#checkFiscalCode(String)}.</li>
     *   <li>If response is {@code "OK"}, enqueue message via {@link MessageProducerServiceImpl#enqueueMessage(MessageDTO, String)}.</li>
     *   <li>If response is not {@code "OK"}, return {@code false} without queueing.</li>
     * </ul>
     *
     *
     * @param messageDTO the message to be queued for delivery
     * @return {@code Mono<Boolean>}
     *      <ul>
     *          <li>{@code true} if successfully queued, </li>
     *          <li>{@code false} if recipient has no enabled channels</li>
     *      </ul>
     */
    @Override
    public Mono<Boolean> send(MessageDTO messageDTO) {
        String messageId = inputSanitization(messageDTO.getMessageId());
        String recipientIdHashed = createSHA256(inputSanitization(messageDTO.getRecipientId()));
        Boolean hasPayment = messageDTO.getAssociatedPayment();
        log.info("[MESSAGE-CORE][SEND] Received message: {}", messageId);

        return citizenConnector.checkFiscalCode(messageDTO.getRecipientId())
                .flatMap(response -> {
                    if ("OK".equals(response)) {
                        log.info("[MESSAGE-CORE][SEND] Fiscal code check passed for recipient: {}", recipientIdHashed);
                        return messageProducerService.enqueueMessage(messageDTO,messageId)
                                .doOnSuccess(aVoid -> log.info("[MESSAGE-CORE][SEND] Message {} enqueued successfully for recipient: {} associatedPayment: {}", messageId, recipientIdHashed, hasPayment))
                                .thenReturn(true);
                    } else {
                        log.warn("[MESSAGE-CORE][SEND] Fiscal code check failed for recipient: {} associatedPayment: {}", recipientIdHashed, hasPayment);
                        return Mono.just(false);
                    }
                })
                .doOnError(error -> log.error("[MESSAGE-CORE][SEND] Error while checking fiscal code for recipient: {}. Error: {}", recipientIdHashed, error.getMessage()));
    }

    /**
     * Performs a paginated search for messages applying optional filters and field projection.
     * <p>
     * The internal logic executes the following steps:
     * <ul>
     *     <li>Normalizes pagination parameters (page and size) to ensure they fall within safe bounds.</li>
     *     <li>Resolves and validates the requested {@code fields} for response projection.</li>
     *     <li>Executes concurrent reactive calls for data retrieval and total record count using {@link Mono#zip}.</li>
     *     <li>Maps database entities to {@link MessageDTO} objects, including only the allowed fields.</li>
     *     <li>Calculates pagination metadata such as total elements and total pages.</li>
     * </ul>
     * </p>
     *
     * @param messageId     optional unique message identifier (exact match)
     * @param recipientId   optional recipient identifier (exact match)
     * @param originId      optional source system identifier (exact match)
     * @param startDate     optional inclusive start date for the registration range
     * @param endDate       optional inclusive end date for the registration range
     * @param page          the zero-based page index to retrieve
     * @param size          the requested number of items per page (subject to capping)
     * @param fields        list of specific fields to include in the response; if null or empty, default fields are used
     * @return a {@link Mono} emitting the {@link MessageSearchResponseDTO} containing the results and pagination metadata
     * @throws it.gov.pagopa.message.config.ExceptionMap (or specific exception) if the requested {@code fields} are invalid
     * 
     * @see it.gov.pagopa.message.repository.MessageRepository#searchMessages
     * @see it.gov.pagopa.message.repository.MessageRepository#countMessages
     */
    @Override
    public Mono<MessageSearchResponseDTO> searchMessages(String messageId, String recipientId, String originId, LocalDateTime startDate, LocalDateTime endDate, int page, int size, List<String> fields) {
        
        return Mono.defer(() -> {
            int safePage = Math.max(page, 0);
            int safeSize = normalizePageSize(size);
            Set<String> safeFields = resolveSearchFields(fields);

            log.info("[MESSAGE-CORE][SEARCH] Received search request - messageIdPresent: {}, startDatePresent: {}, endDatePresent: {}, page: {}, size: {}, fields: {}",
                    messageId != null && !messageId.isBlank(), startDate != null, endDate != null, safePage, safeSize, safeFields);

                    Mono<List<ResponseMessageDTO>> contentMono = messageRepository.searchMessages(messageId, recipientId, originId, startDate, endDate, safePage, safeSize, safeFields)
                    //Mappa gli elementi recuperati dal DB nel DTO
                    .map(message -> messageMapperObjectToDTO.map(message, safeFields))
                    .collectList();

            Mono<Long> countMono = messageRepository.countMessages(messageId, recipientId, originId, startDate, endDate);

            return Mono.zip(contentMono, countMono)
                    .map(tuple -> {
                        List<ResponseMessageDTO> content = tuple.getT1();
                        long totalElements = tuple.getT2();
                        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
                        return MessageSearchResponseDTO.builder()
                                .content(content)
                                .page(safePage)
                                .size(safeSize)
                                .totalElements(totalElements)
                                .totalPages(totalPages)
                                .build();
                    });
        })
        .doOnSuccess(result -> log.info("[MESSAGE-CORE][SEARCH] Search completed - returned {} elements, totalElements: {}, totalPages: {}",
                result.getContent().size(), result.getTotalElements(), result.getTotalPages()))
        .doOnError(error -> log.error("[MESSAGE-CORE][SEARCH] Error while searching messages: {}", error.getMessage()));
    }

    /**
     * Resolves the effective set of fields to project/return for the {@code searchTpps}
     * operation. Falls back to the default grid fields when none are provided, otherwise
     * validates the requested fields against the allowed set, throwing an
     * {@code INVALID_SEARCH_FIELD} exception if any unknown field is requested.
     * <p>
     * Called from within a {@code Mono.defer}, so any exception thrown here is correctly
     * captured and propagated as a reactive error signal instead of being thrown synchronously.
     *
     * @param fields the raw fields requested by the caller (nullable/empty)
     * @return the validated, effective set of fields to use
     */
    private Set<String> resolveSearchFields(List<String> fields) {
        if (fields == null || fields.isEmpty()) {
            return MessageCoreConstants.SearchFields.DEFAULT_GRID_FIELDS;
        }

        Set<String> requestedFields = new HashSet<>(fields);
        Set<String> invalidFields = requestedFields.stream()
                .filter(field -> !MessageCoreConstants.SearchFields.ALLOWED.contains(field))
                .collect(Collectors.toSet());

        if (!invalidFields.isEmpty()) {
            throw exceptionMap.throwException(ExceptionName.INVALID_SEARCH_FIELD,
                    ExceptionMessage.INVALID_SEARCH_FIELD + ": " + invalidFields);
        }

        return requestedFields;
    }

    /**
     * Normalizes the requested page size, falling back to the default when non-positive and
     * capping to the configured maximum otherwise.
     *
     * @param size the requested page size
     * @return a safe page size within {@code [1, maxPageSize]}
     */
    private int normalizePageSize(int size) {
        if (size <= 0) {
            return defaultPageSize;
        }
        return Math.min(size, maxPageSize);
    }

}


