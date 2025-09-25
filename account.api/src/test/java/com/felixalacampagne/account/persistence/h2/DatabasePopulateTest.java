package com.felixalacampagne.account.persistence.h2;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.felixalacampagne.account.TestApplication;
import com.felixalacampagne.account.persistence.repository.AccountJpaRepository;
import com.felixalacampagne.account.persistence.repository.PhoneAccountJpaRepository;
import com.felixalacampagne.account.persistence.repository.RepositoryConfig;
import com.felixalacampagne.account.persistence.repository.StandingOrdersJpaRepository;
import com.felixalacampagne.account.persistence.repository.TransactionJpaRepository;

// AAAAAAAAAAAAAAAaaaaaaaaaaaaaaaaaaaaaggggggggggggggggggggggghhhhhhhhhhhhhhhhhhhhh!
// WARNING: eclipse does not run @Disabled test even when
// the method name is used to runas->Junit test.
// Looking more and more like moving to IntelliJ is a good idea although I'm not sure
// which vital features are missing from the free version
//
// Fork Me! Disabled test can be run in eclipse by providing:
// -Djunit.jupiter.conditions.deactivate=org.junit.*DisabledCondition
// as a VM argument to the run configuration.
//@Disabled

@SpringBootTest(classes = {TestApplication.class})
//@Configuration
@Import({RepositoryConfig.class})
@ActiveProfiles({"h2create"
             , "noinittest"
             , "populatedb"
             })
public class DatabasePopulateTest
{
   @Autowired
   AccountJpaRepository accountJpaRepository;

   @Autowired
   TransactionJpaRepository transactionJpaRepository;

   @Autowired
   PhoneAccountJpaRepository phoneAccountJpaRepository;

   @Autowired
   StandingOrdersJpaRepository standingOrdersJpaRepository;


   @Test
   void databaseIsPopulated()
   {
      assertTrue(accountJpaRepository.count() > 0, "Account table should exist and not be empty");
      assertTrue(transactionJpaRepository.count() > 0, "Transaction table should exist and be empty");
      assertTrue(phoneAccountJpaRepository.count() > 0, "Phoneaccount table should exist and be empty");
      assertTrue(standingOrdersJpaRepository.count() > 0, "Standingorder table should exist and be empty");
   }
}
