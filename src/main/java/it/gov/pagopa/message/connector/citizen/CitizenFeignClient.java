package it.gov.pagopa.message.connector.citizen;

import it.gov.pagopa.message.dto.CitizenConsentDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@FeignClient(
        name = "citizen",
        url = "{rest-client.citizen.baseUrl}")
public interface CitizenFeignClient {

    @GetMapping(
            value = "/emd/citizen/list/{fiscalCode}/enabled",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<CitizenConsentDTO> getCitizenConsentsEnabled(@Valid @PathVariable String fiscalCode);

}
