package com.felixalacampagne.account.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.felixalacampagne.account.AccountTest;

// Had lots of weird errors trying to get this to work. At one point the test was dropping the database
// tables and I could not figure out why. I deleted the the database altogether yet still the test ran,
// deleted the tables and ran successfully. No idea WTF was going on and eventually had to revert all changes.
// The persistence tests then worked again (they gave an error for the missing DB!).
// This test would not run at all which I eventually figured out was due to the TestApplication being in
// the persistence package. Moving to the root package then resulted in another error which seemed to
// suggest that the reference to the service package in TestApplication scanBasePackages did not work.
// I added the Import of an explicit config class here and the test started to work. What a forking mess!
// Anyway, the best way to do this is probably defining a data test annotation class which could include
// all the config classes as required...
@AccountTest
class AccountServiceTest
{
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	AccountService accountService;
	
	@Test
	void testGetAccounts()
	{
		String acclistjson = accountService.getAccounts();
		log.info("testGetAccounts: json accoount list: {}", acclistjson);
	}

}
