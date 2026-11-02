package org.vivias.domain.services;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.vivias.spring.AppScanConfig;

/*
 * DOMAIN Service Tests
 * Strategy: Simple-Facade
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppScanConfig.class)
public class Test1_SimpleDomainService {
	private static Logger logger = Logger.getLogger(Test1_SimpleDomainService.class.getName());

	@Autowired @Qualifier("RegistruCasaDomainService")
	private IRegistruCasaDomainService service;

	@Test
	public void test() {
		logger.info("Service implementation object :: " + service);

		// Registru_1 (ziua de ieri) este seedat in RegistruCasaEntityFactoryBase.initDomainServiceEntities()
		// cu incasarea in numerar din raportul Z + un avans
		Integer numarOperatiuni = service.getNumarOperatiuni(1L);
		assertTrue(numarOperatiuni == 2, "Operatiunile de casa nu se numara corect...");
		logger.info("Numar operatiuni Registru_1:: " + numarOperatiuni);
	}
}
