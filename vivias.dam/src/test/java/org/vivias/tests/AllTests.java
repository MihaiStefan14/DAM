package org.vivias.tests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/*
 * Suita completa, in ordinea din modelul msd.t2_c4.spring_domain.
 * Rulare din IDE: Run AllTests. (mvn test descopera clasele individual.)
 */
@Suite
@SelectClasses({
	Test1_SimpleDomainService.class,
	Test2_ComputingDomainServices.class,
	Test3_BusinessWorkflowService.class,
	Test4_ValidatingDomainService.class,
	Test5_AuditedWorkflowService.class,
	Test6_EventWorkflowService.class,
	TestEntityManagerJPA.class,
	TestRegistruCasaRepository.class,
	TestRegistruCasaEntityRepositoryJPA.class,
	TestAgregateRepository.class
})
public class AllTests {

}

/*
* Junit5 docs: https://junit.org/junit5/docs/current/user-guide/#overview
*/
