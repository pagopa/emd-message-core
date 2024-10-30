package it.gov.pagopa.message.service;

import it.gov.pagopa.message.model.CitizenConsent;
import it.gov.pagopa.message.repository.CitizenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {
        BloomFilterServiceImpl.class,
})
class BloomFilterServiceTest {

    @MockBean
    private CitizenRepository citizenRepository;

    @Autowired
    private BloomFilterServiceImpl bloomFilterService;

    @Test
    void testInitializeBloomFilter() {
        CitizenConsent citizen1 = CitizenConsent.builder().hashedFiscalCode("hashedCode1").build();

        when(citizenRepository.findAll()).thenReturn(Flux.just(citizen1));

        bloomFilterService.initializeBloomFilter();

        assertTrue(bloomFilterService.mightContain("hashedCode1"), "Bloom filter should contain hashedCode1");
        assertFalse(bloomFilterService.mightContain("nonExistentCode"), "Bloom filter should NOT contain nonExistentCode");
    }

    @Test
    void updateitializeBloomFilter() {
        CitizenConsent citizen1 = CitizenConsent.builder().hashedFiscalCode("hashedCode1").build();

        when(citizenRepository.findAll()).thenReturn(Flux.just(citizen1));

        bloomFilterService.updateBloomFilter();

        assertTrue(bloomFilterService.mightContain("hashedCode1"), "Bloom filter should contain hashedCode1");
        assertFalse(bloomFilterService.mightContain("nonExistentCode"), "Bloom filter should NOT contain nonExistentCode");
    }
}
