package it.gov.pagopa.message.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "it.gov.pagopa")
public class EmdMessageCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmdMessageCoreApplication.class, args);
	}

}
