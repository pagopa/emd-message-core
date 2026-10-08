package it.gov.pagopa.message.dto;

import java.util.List;

public record MessageKeysetPage<T>(List<T> content, boolean hasNext, String nextCursor) {

    }
