package it.gov.pagopa.message.service;

public interface BloomFilterService {

     boolean mightContain(String hashedFiscalCode);
}
