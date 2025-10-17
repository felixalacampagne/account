package com.felixalacampagne.account.persistence.db;
 
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.felixalacampagne.account.TestApplication;
import com.felixalacampagne.account.persistence.repository.AccountJpaRepository;
import com.felixalacampagne.account.persistence.repository.PhoneAccountJpaRepository;
import com.felixalacampagne.account.persistence.repository.RepositoryConfig;
import com.felixalacampagne.account.persistence.repository.StandingOrdersJpaRepository;
import com.felixalacampagne.account.persistence.repository.TransactionJpaRepository;
import com.felixalacampagne.account.service.HouseKeepingService;
//AAAAAAAAAAAAAAAaaaaaaaaaaaaaaaaaaaaaggggggggggggggggggggggghhhhhhhhhhhhhhhhhhhhh!
//WARNING: eclipse does not run @Disabled test even when
//the method name is used to runas->Junit test.
//Looking more and more like moving to IntelliJ is a good idea although I'm not sure
//which vital features are missing from the free version
//
//Fork Me! Disabled test can be run in eclipse by providing:
//-Djunit.jupiter.conditions.deactivate=org.junit.*DisabledCondition
//as a VM argument to the run configuration.
@Disabled

// More AAAAAAAAAAAAAAAaaaaaaaaaaaaaaaaaaaaaggggggggggggggggggggggghhhhhhhhhhhhhhhhhhhhh!
// DatabasePopulateTest successfully use the values for db name/location from the h2create properties files
// however DatabaseConfigurationTest does NOT use db name/location from the h2create properties file
// even though the configuration of the two tests is the same except for the additional 'populatedb'
// in DatabasePopulateTest
// I have no forking clue why this is going wrong. Spring doc and Google indicate that it should work as
// expected, ie. values in h2create properties should override the values in the default property file.
//
// Later (much!) the test now runs as expected using the h2create properties. WTF!
// Spring developers once again having a great big forking laugh over how much time they
// manage to waste...

@SpringBootTest(classes = {TestApplication.class}
,webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT // Required to be able to access the H2 console
      )
@Import({RepositoryConfig.class})
@ActiveProfiles({"h2create"
               , "noinittest"
               })
public class DatabaseConfigurationTest {
   Logger log = LoggerFactory.getLogger(this.getClass());

   @Value("${falc.account.db.name}") private String dbname;
   @Value("${falc.account.db.location}") private String dblocation;
   @Value("${spring.jpa.hibernate.ddl-auto}") private String ddlauto;
	@Autowired
	AccountJpaRepository accountJpaRepository;

	@Autowired
	TransactionJpaRepository transactionJpaRepository;

	@Autowired
	PhoneAccountJpaRepository phoneAccountJpaRepository;

	@Autowired
	StandingOrdersJpaRepository standingOrdersJpaRepository;

	@Autowired
	HouseKeepingService houseKeepingService;

	@Test
	void databaseIsCreated() throws Exception
	{
	   log.info("databaseIsCreated: falc.account.db.name:{}", dbname);
      log.info("databaseIsCreated: falc.account.db.location:{}", dblocation);
      log.info("databaseIsCreated: spring.jpa.hibernate.ddl-auto:{}", ddlauto);

		assertTrue(accountJpaRepository.count() >= 0, "Account table should exist");
		assertTrue(transactionJpaRepository.count() >= 0, "Transaction table should exist");
		assertTrue(phoneAccountJpaRepository.count() >= 0, "Phoneaccount table should exist");
		assertTrue(standingOrdersJpaRepository.count() >= 0, "Standingorder table should exist");

		// This should work. If the console has loads of weird JPA/JDBC errors then there is probably
		// already a console running - eclipse doesn't always stop the tests when the stop button is clicked.
	   //log.info("Now is the time to connect to the H2 console: http://localhost:8080/h2-console  jdbc:h2:file:./db/accountH2create");
	   // Thread.sleep(60000 * 15);

	}

	@Test
	void databaseBackup()
	{

	   // Needs mysqldump on the PATH, eg. in C:\Program Files\MySQL\MySQL Workbench 8.0 CE
	   houseKeepingService.doHouseKeeping();
	}
}
