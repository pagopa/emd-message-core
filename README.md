# emd-message-core
Service which acts as dispatcher for messages to end-users through 3rd-party application

## API Documentation

API specification: [openapi.message-core.yml](https://github.com/pagopa/cstar-infrastructure/blob/main/src/domains/mil-app-poc/api/emd_message_core/openapi.emd.yml)

## Components

---

#### [MessageCoreService](src/main/java/it/gov/pagopa/message/service/MessageCoreServiceImpl.java)

Main component that coordinates message reception and routing.

- **Operational flow:**

1. **Reception**: Receives the message to be sent from a channel (e.g., SEND)

2. **Recipient validation**: Checks if the recipient's fiscal code is present in the BloomFilter

3. **Queueing**: If the recipient is present, invokes the [`enqueueMessage`](src/main/java/it/gov/pagopa/message/service/MessageProducerService.java) method of [`MessageProducerService`](src/main/java/it/gov/pagopa/message/service/MessageProducerService.java) to queue the message to `emd-courtesy-message`, starting the notification delivery process

---

#### [MessageProducerService](src/main/java/it/gov/pagopa/message/service/MessageProducerService.java)

Component responsible for preparing and publishing messages to Kafka.

- **Operational flow:**

1. **Message creation**: Creates a `Message` with the content as payload

2. **Header initialization**: Sets the `retry` field in the header to `0` to track delivery attempts

3. **Scheduling**: Publishes the message to the Kafka queue `emd-courtesy-message` to enable consumption and delivery to the citizen's TPP (Third Party Provider)

---

## Integration Points

- **Upstream**: Channel service (e.g., SEND) (message source)
- **Downstream**: emd-courtesy-message (Kafka queue for notification processing)

## Key Components

- **[BloomFilter](https://github.com/pagopa/emd-citizen/blob/release-dev/src/main/java/it/gov/pagopa/onboarding/citizen/service/BloomFilterServiceImpl.java)**: Used to efficiently verify the existence of the recipient's fiscal code
- **Kafka Queue**: `emd-courtesy-message` - queue for asynchronous notification management