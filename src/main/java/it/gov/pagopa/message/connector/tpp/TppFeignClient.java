package it.gov.pagopa.message.connector.tpp;


import it.gov.pagopa.message.dto.TppDTO;
import it.gov.pagopa.message.dto.TppIdList;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@FeignClient(
        name = "tpp",
        url = "${rest-client.tpp.baseUrl}")
public interface TppFeignClient {
    @PostMapping(
            value ="/emd/tpp/list",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<TppDTO> getTppsEnabled(@RequestBody TppIdList tppIdList);
}
