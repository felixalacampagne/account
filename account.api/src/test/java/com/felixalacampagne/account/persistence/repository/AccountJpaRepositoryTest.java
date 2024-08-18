package com.felixalacampagne.account.persistence.repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.felixalacampagne.account.persistence.entities.Account;
import com.felixalacampagne.account.AccountTest;

@AccountTest
class AccountJpaRepositoryTest
{
   Logger log = LoggerFactory.getLogger(this.getClass());

   @Autowired
   private AccountJpaRepository accountJpaRepository;

   @AfterEach
   void tearDown() throws Exception
   {
   }

   @Test
   void testFindAll()
   {
      List<Account> accs = accountJpaRepository.findAll();
      log.info("testFindAll: found {} accounts", accs.size());
   }

   @Test
   void testFindActiveAccountsSorted()
   {
      List<Account> accs = accountJpaRepository.findAccountsExcludeAccOrderSorted(Collections.singletonList(255L));
      log.info("testFindActiveAccountsSorted: Query result for active accounts:\n{}",
            accs.stream()
            .map(a -> "" + a.getAccOrder() + " " + a.getAccDesc())
            .collect(Collectors.joining("\n"))
            );

   }   
}
