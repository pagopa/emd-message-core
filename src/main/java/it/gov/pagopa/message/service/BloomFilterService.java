package it.gov.pagopa.message.service;


import com.azure.cosmos.implementation.guava25.hash.BloomFilter;
import com.azure.cosmos.implementation.guava25.hash.Funnels;
import it.gov.pagopa.message.model.CitizenConsent;
import it.gov.pagopa.message.repository.CitizenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
        List<String> hashedFiscalCodes = citizenRepository.findAll()
                .map(CitizenConsent::getHashedFiscalCode)
                .collectList()
                .block();

        if (hashedFiscalCodes != null) {
            for (String code : hashedFiscalCodes) {
                bloomFilter.put(code);
            }
        }
    }

    public boolean mightContain(String hashedFiscalCode) {
        return bloomFilter.mightContain(hashedFiscalCode);
    }


}
