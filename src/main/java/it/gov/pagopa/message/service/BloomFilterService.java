package it.gov.pagopa.message.service;


import com.azure.cosmos.implementation.guava25.hash.BloomFilter;
import com.azure.cosmos.implementation.guava25.hash.Funnels;
import it.gov.pagopa.message.repository.CitizenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

@Component
public class BloomFilterService {

    private final CitizenRepository citizenRepository;

    private BloomFilter<String> bloomFilter;

    public BloomFilterService(CitizenRepository citizenRepository) {
        this.citizenRepository = citizenRepository;
    }

    @PostConstruct
    @Scheduled(fixedRate = 3600000)
    public void initializeBloomFilter() {
        bloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 1000000, 0.01);
        citizenRepository.findAll()
                .map(citizenConsent -> bloomFilter.put(citizenConsent.getHashedFiscalCode()));
    }

    public boolean mightContain(String hashedFiscalCode) {
        return bloomFilter.mightContain(hashedFiscalCode);
    }


}
