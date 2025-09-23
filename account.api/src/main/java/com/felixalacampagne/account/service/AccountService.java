package com.felixalacampagne.account.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.felixalacampagne.account.common.Utils;
import com.felixalacampagne.account.model.AccountDetail;
import com.felixalacampagne.account.model.AccountItem;
import com.felixalacampagne.account.model.Accounts;
import com.felixalacampagne.account.model.TfrAccountItem;
import com.felixalacampagne.account.persistence.entities.Account;
import com.felixalacampagne.account.persistence.repository.AccountJpaRepository;
import com.felixalacampagne.account.persistence.repository.PhoneAccountJpaRepository;
import com.felixalacampagne.account.persistence.repository.TransactionJpaRepository;

@Service
public class AccountService
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());

   private final AccountJpaRepository accountJpaRepository;
   private final PhoneAccountJpaRepository phoneAccountJpaRepository;
   private final TransactionJpaRepository transactionJpaRepository;
   private final ConnectionResurrector<AccountJpaRepository> connectionResurrector;

   @Autowired
   public AccountService(AccountJpaRepository accountJpaRepository,
         PhoneAccountJpaRepository phoneAccountJpaRepository,
         TransactionJpaRepository transactionJpaRepository) {
      this.accountJpaRepository = accountJpaRepository;
      this.phoneAccountJpaRepository = phoneAccountJpaRepository;
      this.transactionJpaRepository = transactionJpaRepository;
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

      return phoneAccountJpaRepository.findTransferAccountsWithAccount(srcacc.getId())
            .stream()
            .map(a -> {
               String desc = AelseB(a.getAccDesc(), a.getPhoneAccount().getDesc());

               // The Account code appears to be out of date or not suitable for making transfers so don't use it.
               String code = a.getPhoneAccount().getAccountNumber(); // aElseB(a.getAccCode(), a.getPhoneAccount().getAccountNumber());
               return new TfrAccountItem(a.getPhoneAccount().getId(), 
            		   a.getPhoneAccount().getAccount().getId(), 
            		   desc, code, a.getPhoneAccount().getLastComm());
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
                          .map(a -> { return new AccountItem(a.getId(), a.getAccDesc(), a.getAccSid()); })
                          .orElseThrow(() -> new AccountException("Account not found: " + id));
   }

   public Accounts getAccounts() {
      List<AccountItem> accitems = getAccountList();
      Accounts accs = new Accounts(accitems); // For fronted compatibility
      return accs;
   }

   public List<AccountDetail> getAccountDetailList()
   {
      Sort sort = Sort.by("accOrder", "accDesc");
      return accountJpaRepository.findAll(sort).stream()
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

   public void addAccount(AccountDetail accountDetail)
   {
      Account account = mapToEntity(accountDetail);
      this.accountJpaRepository.saveAndFlush(account);
   }

   public void updateAccount(AccountDetail accountDetail)
   {
      log.info("updateAccount: accountDetail:{}", accountDetail);
      if(accountDetail == null)
         return;
      Account account = accountJpaRepository.findById(accountDetail.getId())
            .orElseThrow(()->new AccountException("Account id " + accountDetail.getId() + " not found"));

      String origToken = Utils.getToken(account);
      if(!origToken.equals(accountDetail.getToken()))
      {
         log.info("updateAccount: Token mismatch for Account id:{}: original:{} supplied:{}",
               accountDetail.getId(), origToken, accountDetail.getToken());
         throw new  AccountException("Token does not match Account id " + accountDetail.getId());
      }

      account = mapToEntity(accountDetail, account);
      this.accountJpaRepository.saveAndFlush(account);

   }

   @Transactional
   public void deleteAccount(AccountDetail accountDetail)
   {
      log.info("deleteAccount: accountDetail:{}", accountDetail);
      if(accountDetail == null)
         return;
      Account account = accountJpaRepository.findById(accountDetail.getId())
            .orElseThrow(()->new AccountException("Account id " + accountDetail.getId() + " not found"));

      String origToken = Utils.getToken(account);
      if(!origToken.equals(accountDetail.getToken()))
      {
         log.info("deleteAccount: Token mismatch for Account id:{}: original:{} supplied:{}",
               accountDetail.getId(), origToken, accountDetail.getToken());
         throw new  AccountException("Token does not match Account id " + accountDetail.getId());
      }

      long deleted = this.transactionJpaRepository.deleteByAccountId(account.getAccId());
      log.info("deleteAccount: transactions for account id:{} deleted: {}", account.getAccId(), deleted);

      deleted = this.phoneAccountJpaRepository.deleteByAccountId(account.getAccId());
      log.info("deleteAccount: phoneAccount for account id:{} deleted: {}", account.getAccId(), deleted);

      this.accountJpaRepository.delete(account);
      log.info("deleteAccount: account id:{} desc:{} deleted", account.getAccId(), account.getAccDesc());
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

   private Account mapToEntity(AccountDetail accountDetail)
   {
      Account acc = new Account();
      return mapToEntity(accountDetail, acc);
   }

   private Account mapToEntity(AccountDetail accountDetail, Account acc)
   {
      if((acc.getAccId() == null) || (acc.getAccId() < 1))
      {
         acc.setAccId(accountDetail.getId());
      }
      acc.setAccDesc(accountDetail.getName());
      acc.setAccAddr(accountDetail.getAddress());
      acc.setAccCode(accountDetail.getCode());
      acc.setAccCurr(accountDetail.getCurrency());
      acc.setAccFmt(accountDetail.getFormat());
      acc.setAccOrder(accountDetail.getOrder());
      acc.setAccSid(accountDetail.getStatementref());
      acc.setAccSwiftbic(accountDetail.getBic());
      acc.setAccTel(accountDetail.getTelephone());
      return acc;
   }

}
