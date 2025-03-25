package it.gov.pagopa.message;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "it.gov.pagopa")
@EnableFeignClients(basePackages = "it.gov.pagopa.message.connector")
public class EmdMessageCore {

	public static void main(String[] args) {
		SpringApplication.run(EmdMessageCore.class, args);
	}

}
