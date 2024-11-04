package it.gov.pagopa.message.service;

import it.gov.pagopa.message.model.CitizenConsent;
import it.gov.pagopa.message.repository.CitizenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BloomFilterServiceTest {

    @Mock
    private CitizenRepository citizenRepository;

    @InjectMocks
    private BloomFilterServiceImpl bloomFilterService;

    @BeforeEach
    void setUp() {
        CitizenConsent consent1 = CitizenConsent.builder().hashedFiscalCode("hashedFiscalCode1").build();
        CitizenConsent consent2 = CitizenConsent.builder().hashedFiscalCode("hashedFiscalCode2").build();
        when(citizenRepository.findAll()).thenReturn(Flux.just(consent1, consent2));
        bloomFilterService.initializeBloomFilter();
    }

    @Test
    void testMightContain() {
        assertTrue(bloomFilterService.mightContain("hashedFiscalCode1"));
        assertTrue(bloomFilterService.mightContain("hashedFiscalCode2"));
        assertFalse(bloomFilterService.mightContain("nonexistentHashedFiscalCode"));
    }

    @Test
    void testUpdate() {
        CitizenConsent consent1 = CitizenConsent.builder().hashedFiscalCode("hashedFiscalCode1").build();
        when(citizenRepository.findAll()).thenReturn(Flux.just(consent1));
        bloomFilterService.update();
        assertTrue(bloomFilterService.mightContain("hashedFiscalCode1"));
        assertFalse(bloomFilterService.mightContain("hashedFiscalCode2"));
        assertFalse(bloomFilterService.mightContain("nonexistentHashedFiscalCode"));
    }
}
