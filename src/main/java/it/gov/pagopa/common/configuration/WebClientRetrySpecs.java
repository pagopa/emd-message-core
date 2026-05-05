package it.gov.pagopa.common.configuration;

import io.netty.channel.ConnectTimeoutException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;

/**
 * Centralized {@link Retry} specifications for all {@code WebClient} calls.
 *
 * <p>Two policies are exposed to address the trade-off between resilience
 * (recovering from stale connections / transient network issues) and safety
 * (avoiding duplicate side-effects on non-idempotent operations).
 *
 * <p>Both policies use exponential back-off with a 50% jitter to prevent
 * thundering-herd retries across pods after a coordinated network event
 * (e.g. an Azure LB simultaneously dropping idle connections).
 */
public final class WebClientRetrySpecs {

    /** Maximum number of retry attempts (initial attempt is not counted). */
    public static final int MAX_RETRY_ATTEMPTS = 2;

    /** Minimum back-off between attempts; actual delay is randomized by {@link #JITTER}. */
    public static final Duration MIN_BACKOFF = Duration.ofMillis(100);

    /** Jitter factor (0.0–1.0) applied to the back-off to spread retries over time. */
    public static final double JITTER = 0.5;

    private WebClientRetrySpecs() {
        // utility class
    }

    /**
     * Permissive policy for <strong>idempotent operations</strong> (GET, PUT, DELETE).
     *
     * <p>Retries on:
     * <ul>
     *   <li><b>Transport errors</b> — any {@link WebClientRequestException}
     *       (TCP drop, stale connection reset, read/write timeout …)</li>
     *   <li><b>Transient HTTP 5xx</b> — 502 Bad Gateway, 503 Service Unavailable,
     *       504 Gateway Timeout. These are expected during AKS rolling updates when
     *       a pod is restarting and the upstream infrastructure briefly returns
     *       gateway errors before re-routing traffic.</li>
     * </ul>
     *
     * <p>Does <em>not</em> retry 4xx or non-transient 5xx (e.g. 500 Internal Server Error).
     *
     * @return a fresh {@link Retry} spec — must NOT be reused across pipelines
     */
    public static Retry transientNetwork() {
        return Retry.backoff(MAX_RETRY_ATTEMPTS, MIN_BACKOFF)
                .jitter(JITTER)
                .filter(ex -> {
                    // Transport-level failure (TCP drop, stale connection, timeout …)
                    if (ex instanceof WebClientRequestException) {
                        return true;
                    }
                    // Transient gateway errors — common during AKS rolling updates
                    if (ex instanceof WebClientResponseException responseEx) {
                        int status = responseEx.getStatusCode().value();
                        return status == 502 || status == 503 || status == 504;
                    }
                    return false;
                });
    }

    /**
     * Conservative policy: retries <strong>only</strong> when it is certain that
     * the request did not reach the server, i.e. the TCP handshake itself failed.
     *
     * <p>Safe for non-idempotent operations (POST). Does NOT recover from
     * stale-connection drops mid-write; for that level of resilience an
     * application-level idempotency key is required.
     *
     * @return a fresh {@link Retry} spec — must NOT be reused across pipelines
     */
    public static Retry connectFailureOnly() {
        return Retry.backoff(MAX_RETRY_ATTEMPTS, MIN_BACKOFF)
                .jitter(JITTER)
                .filter(ex -> ex instanceof WebClientRequestException
                        && (ex.getCause() instanceof ConnectException
                            || ex.getCause() instanceof ConnectTimeoutException));
    }
}

