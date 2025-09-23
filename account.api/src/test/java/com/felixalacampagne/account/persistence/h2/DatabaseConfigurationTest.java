package com.felixalacampagne.account.persistence.h2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import com.felixalacampagne.account.TestApplication;
import com.felixalacampagne.account.persistence.repository.AccountJpaRepository;
import com.felixalacampagne.account.persistence.repository.PhoneAccountJpaRepository;
import com.felixalacampagne.account.persistence.repository.StandingOrdersJpaRepository;
import com.felixalacampagne.account.persistence.repository.TransactionJpaRepository;

@SpringBootTest(classes = {TestApplication.class})
@Configuration
@ActiveProfiles("h2create")
public class DatabaseConfigurationTest {
	
	@Autowired
	AccountJpaRepository accountJpaRepository;

	@Autowired
	TransactionJpaRepository transactionJpaRepository;
	
	@Autowired
	PhoneAccountJpaRepository phoneAccountJpaRepository;
	
	@Autowired
	StandingOrdersJpaRepository standingOrdersJpaRepository;
	
	@Test
	void databaseIsCreated() 
	{
		assertEquals(0, accountJpaRepository.count(), "Account table should exist and be empty");
		assertEquals(0, transactionJpaRepository.count(), "Transaction table should exist and be empty");
		assertEquals(0, phoneAccountJpaRepository.count(), "Phoneaccount table should exist and be empty");
		assertEquals(0, standingOrdersJpaRepository.count(), "Standingorder table should exist and be empty");
		
	}
}
