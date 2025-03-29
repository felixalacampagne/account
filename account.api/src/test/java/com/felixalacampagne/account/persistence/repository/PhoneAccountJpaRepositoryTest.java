package com.felixalacampagne.account.persistence.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.felixalacampagne.account.AccountTest;
import com.felixalacampagne.account.common.Utils;
import com.felixalacampagne.account.persistence.entities.PhoneAccount;
import com.felixalacampagne.account.persistence.entities.PhoneWithAccountDTO;
import com.felixalacampagne.account.persistence.entities.PhoneWithAccountProjection;

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

   @Test
   void testfindTransferAccountsWithAccount()
   {
      List<PhoneWithAccountProjection> accs = phoneAccountJpaRepository.findTransferAccountsWithAccount(22L);
      log.info("testfindTransferAccountsWithAccount: found {} phoneaccounts", accs.size());
      log.info("testfindTransferAccountsWithAccount: phoneaccounts:\n{}",
            accs.stream()
                .map(a -> "" + a.stringify())
                .collect(Collectors.joining("   \n")));
   }

   @Test
   void testfindTransferAccountsWithAccountDTO()
   {
      List<PhoneWithAccountDTO> accs = phoneAccountJpaRepository.findTransferAccountsWithAccountDTO(22L);
      log.info("testfindTransferAccountsWithAccount: found {} phoneaccounts", accs.size());
      log.info("testfindTransferAccountsWithAccount: phoneaccounts:\n{}", Utils.listToString(accs, "\n   "));
   }

   @Test
   void testfindPhoneWithAccountById()
   {
      PhoneWithAccountProjection pwa = phoneAccountJpaRepository.findPhoneWithAccountById(86L).get();
      log.info("testfindPhoneWithAccountById: id: 86: {}", pwa.stringify());

      pwa = phoneAccountJpaRepository.findPhoneWithAccountById(236L).get();
      log.info("testfindPhoneWithAccountById: id:236: {}", pwa.stringify());
   }

   @Test
   void testUpdateFromPhoneWithAccount()
   {
      PhoneWithAccountProjection pwa = phoneAccountJpaRepository.findPhoneWithAccountById(86L).get();
      log.info("testfindPhoneWithAccountById: PRE id: 86: {}", pwa.stringify());

      PhoneAccount pa = pwa.getPhoneAccount();
      pa.setLastComm(pa.getLastComm() + " updated via PhoneWithAccountProjection");
      phoneAccountJpaRepository.save(pa);

      pwa = phoneAccountJpaRepository.findPhoneWithAccountById(86L).get();
      log.info("testfindPhoneWithAccountById: POST id: 86: {}", pwa.stringify());

   }
}
