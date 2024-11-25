package com.felixalacampagne.account.persistence.repository;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.felixalacampagne.account.AccountTest;
import com.felixalacampagne.account.common.Utils;
import com.felixalacampagne.account.persistence.entities.PhoneAccount;

@AccountTest
public class PhoneAccountJpaRepositoryTest
{
   Logger log = LoggerFactory.getLogger(this.getClass());

   @Autowired
   PhoneAccountJpaRepository phoneAccountJpaRepository;

   @Test
   void testFindAll()
   {
      List<PhoneAccount> accs = phoneAccountJpaRepository.findAll();
      log.info("testFindAll: found {} phoneaccounts", accs.size());
      log.info("testFindAll: phoneaccounts:\n{}", Utils.listToString(accs, "\n   "));
   }

   @Test
   void testFindTransferAccounts()
   {
      List<PhoneAccount> accs = phoneAccountJpaRepository.findTransferAccounts(22L);
      log.info("testFindTransferAccounts: found {} phoneaccounts", accs.size());
      log.info("testFindTransferAccounts: phoneaccounts:\n{}", Utils.listToString(accs, "\n   "));
   }
}
