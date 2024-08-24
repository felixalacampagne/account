package com.felixalacampagne.account.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.felixalacampagne.account.model.AccountItem;
import com.felixalacampagne.account.model.Accounts;
import com.felixalacampagne.account.persistence.entities.Account;
import com.felixalacampagne.account.persistence.repository.AccountJpaRepository;

@Service
public class AccountService
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final ObjectMapper objmap = new ObjectMapper();
   private final AccountJpaRepository accountJpaRepository;

   @Autowired
   public AccountService(AccountJpaRepository accountJpaRepository) {
      this.accountJpaRepository = accountJpaRepository;
   }

   public List<AccountItem> getAccountList() 
   {
      List<Account> accs = accountJpaRepository.findAccountsExcludeAccOrderSorted(Collections.singletonList(255L));
      List<AccountItem> accitems = accs.stream()
            // .sorted(Comparator.comparing(Account::getAccDesc))
            .map(a -> { return new AccountItem(a.getAccId(), a.getAccDesc()); })
            .collect(Collectors.toList());
      return accitems;
   }
   
   public Accounts getAccounts() {
      List<AccountItem> accitems = getAccountList();
      Accounts accs = new Accounts(accitems); // For fronted compatibility
      return accs;
   }
   
   public String getAccountsJson() {
      String result = "";
      Accounts accs = getAccounts();
      try
      {
         result = objmap.writeValueAsString(accs);
      }
      catch (JsonProcessingException e)
      {
         log.info("getAccounts: failed to serialize account list to json:", e);
      }
      return result;
   }
}
