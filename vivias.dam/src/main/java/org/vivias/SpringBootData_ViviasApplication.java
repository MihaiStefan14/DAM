package org.vivias;

import java.util.logging.Logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootData_ViviasApplication {
	private static Logger logger = Logger.getLogger(SpringBootData_ViviasApplication.class.getName());

	public static void main(String[] args) {
		logger.info("Loading vivias.dam :: SpringBootData - ViviasApplication Default Settings ");
		SpringApplication.run(SpringBootData_ViviasApplication.class, args);
	}
}
