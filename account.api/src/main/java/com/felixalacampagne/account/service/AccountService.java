package com.felixalacampagne.account.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.felixalacampagne.account.common.Utils;
import com.felixalacampagne.account.model.AccountDetail;
import com.felixalacampagne.account.model.AccountItem;
import com.felixalacampagne.account.model.Accounts;
import com.felixalacampagne.account.model.TfrAccountItem;
import com.felixalacampagne.account.persistence.entities.Account;
import com.felixalacampagne.account.persistence.repository.AccountJpaRepository;
import com.felixalacampagne.account.persistence.repository.PhoneAccountJpaRepository;

@Service
public class AccountService
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());

   private final AccountJpaRepository accountJpaRepository;
   private final PhoneAccountJpaRepository phoneAccountJpaRepository;
   private final ConnectionResurrector<AccountJpaRepository> connectionResurrector;

   @Autowired
   public AccountService(AccountJpaRepository accountJpaRepository, PhoneAccountJpaRepository phoneAccountJpaRepository) {
      this.accountJpaRepository = accountJpaRepository;
      this.phoneAccountJpaRepository = phoneAccountJpaRepository;
      this.connectionResurrector = new ConnectionResurrector<AccountJpaRepository>(accountJpaRepository, AccountJpaRepository.class);
   }

   public List<AccountItem> getAccountList()
   {
      connectionResurrector.ressurectConnection();
      List<Account> accs = accountJpaRepository.findAccountsExcludeAccOrderSorted(Collections.singletonList(255L));
      List<AccountItem> accitems = accs.stream()
            // .sorted(Comparator.comparing(Account::getAccDesc))
            .map(a -> { return new AccountItem(a.getAccId(), a.getAccDesc(), a.getAccSid()); })
            .collect(Collectors.toList());
      return accitems;
   }

   public List<TfrAccountItem> getTransferAccounts(Long id)
   {
      Account srcacc = accountJpaRepository.findById(id)
         .orElseThrow(() -> new AccountException("Account not found: " + id));

      return phoneAccountJpaRepository.findTransferAccountsWithAccount(srcacc.getAccId())
            .stream()
            .map(a -> { 
               String desc = AelseB(a.getAccDesc(), a.getPhoneAccount().getDesc());
               
               // The Account code appears to be out of date or not suitable for making transfers so don't use it.
               String code = a.getPhoneAccount().getAccountNumber(); // aElseB(a.getAccCode(), a.getPhoneAccount().getAccountNumber());
               return new TfrAccountItem(a.getPhoneAccount().getId(), a.getPhoneAccount().getAccountId(), desc, code, a.getPhoneAccount().getLastComm()); 
               })
            .collect(Collectors.toList());
   }

   String AelseB(String a, String b) {
      return !isNullOrEmpty(a) ? a : b;
   }
   
   boolean isNullOrEmpty(String s) {
      return (s==null) || (s.isBlank());
   }
   
   public AccountItem getAccountItem(long id)
   {
      return accountJpaRepository.findById(id)
                          .map(a -> { return new AccountItem(a.getAccId(), a.getAccDesc(), a.getAccSid()); })
                          .orElseThrow(() -> new AccountException("Account not found: " + id));
   }

   public Accounts getAccounts() {
      List<AccountItem> accitems = getAccountList();
      Accounts accs = new Accounts(accitems); // For fronted compatibility
      return accs;
   }

   public List<AccountDetail> getAccountDetailList()
   {
      return accountJpaRepository.findAll().stream()
                          .map(a -> mapToDetail(a))
                          .collect(Collectors.toList());
   }

   public AccountDetail getAccountDetail(long id)
   {
      return accountJpaRepository.findById(id)
            .map(a -> mapToDetail(a))
            .orElseThrow(() -> new AccountException("Account not found: " + id));
   }

   public Account updateStatementRef(AccountItem accountItem)
   {
      log.info("updateStatementRef: account:{}", accountItem);
      String accstref = accountItem.getStatementref();
      if((accstref==null) || accstref.isEmpty())
      {
         throw new AccountException("Invalid statement reference: '" + accstref + "'");
      }
      Account acc = accountJpaRepository.findById(accountItem.getId()).orElseThrow(() -> new AccountException("Account not found: " + accountItem.getId()));
      acc.setAccSid(accstref);
      acc = accountJpaRepository.saveAndFlush(acc);
      return acc;
   }

   private AccountDetail mapToDetail(Account acc)
   {
      String token = Utils.getToken(acc);
      return new AccountDetail(acc.getAccId(),
            acc.getAccDesc(),
            acc.getAccAddr(),
            acc.getAccCode(),
            acc.getAccCurr(),
            acc.getAccFmt(),
            acc.getAccOrder(),
            acc.getAccSid(),
            acc.getAccSwiftbic(),
            acc.getAccTel(),
            token);
   }


}
