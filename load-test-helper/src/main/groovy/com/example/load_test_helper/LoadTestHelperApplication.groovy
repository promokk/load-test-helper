package com.example.load_test_helper

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@SpringBootApplication
@ConfigurationPropertiesScan
class LoadTestHelperApplication {

	static void main(String[] args) {
		SpringApplication.run(LoadTestHelperApplication, args)
	}

}
