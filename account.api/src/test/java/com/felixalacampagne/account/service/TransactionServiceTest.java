package com.felixalacampagne.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.felixalacampagne.account.AccountTest;
import com.felixalacampagne.account.common.Utils;
import com.felixalacampagne.account.model.AddTransactionItem;
import com.felixalacampagne.account.model.TransactionItem;
import com.felixalacampagne.account.persistence.entities.PhoneAccount;
import com.felixalacampagne.account.persistence.entities.Transaction;
import com.felixalacampagne.account.persistence.repository.PhoneAccountJpaRepository;
import com.felixalacampagne.account.persistence.repository.TransactionJpaRepository;
import com.felixalacampagne.account.service.TransactionService.BalanceType;



@AccountTest
class TransactionServiceTest
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());

   @Autowired
   private TransactionService transactionService;

   @Autowired
   private TransactionJpaRepository transactionJpaRepository;

   @Autowired
   private PhoneAccountJpaRepository phoneAccountJpaRepository;

   @AfterEach
   void tearDown() throws Exception
   {
   }

//   @Test
//   void testGetTransactions()
//   {
//      String txnjson = transactionService.getTransactionsJson(1L);
//      log.info("testGetTransactions: transaction list\n{}", txnjson);
//   }

   @Test
   void addTransactionNewPhoneAccTest()
   {
      List<Transaction> txnsforacc = transactionJpaRepository.findByAccountIdOrderBySequenceAsc(22L);
      List<Transaction> txnsfortfracc = transactionJpaRepository.findByAccountIdOrderBySequenceAsc(34L);
      List<PhoneAccount> phoneaccs = phoneAccountJpaRepository.findTransferAccounts(22L);

      int origtxncnt = txnsforacc.size();
      int origtfrtxncnt = txnsfortfracc.size();
      int origpacnt = phoneaccs.size();

      Transaction origtxn = txnsforacc.get(5);
      TransactionItem origtxnitm = transactionService.mapToItem(origtxn, BalanceType.NORMAL);

      // tfr 34
      AddTransactionItem addtxnitm = new AddTransactionItem(
            origtxnitm.getAccid(), LocalDate.now(), "12345.12", "TEST",
            "TEST TFR with new PA",
            Optional.empty(), "communication", "AAAA New Test Phone Account", "BE12 3456 7890 0123");

      transactionService.addTransaction(addtxnitm);
      txnsforacc = transactionJpaRepository.findByAccountIdOrderBySequenceAsc(22L);
      assertEquals(origtxncnt+1 , txnsforacc.size());

      phoneaccs = phoneAccountJpaRepository.findTransferAccounts(22L);
      log.info("addTransactionNewPhoneAccTest: active phoneaccounts after addition:\n{}", Utils.listToString(phoneaccs, "\n   "));
      assertEquals(origpacnt+1, phoneaccs.size(), "new Phone account should be created");
      origpacnt++;
      origtxncnt++;


      // tfr 34
      addtxnitm = new AddTransactionItem(
            origtxnitm.getAccid(), LocalDate.now(), "12345.12", "TEST",
            "TEST TFR with new PA",
            Optional.of(Long.valueOf(118L)), "bcc comm test", "accname Ignored", "acc id ignored");

      transactionService.addTransaction(addtxnitm);
      txnsforacc = transactionJpaRepository.findByAccountIdOrderBySequenceAsc(22L);
      assertEquals(origtxncnt+1 , txnsforacc.size());

      phoneaccs = phoneAccountJpaRepository.findTransferAccounts(22L);
      log.info("addTransactionNewPhoneAccTest: active phoneaccounts phoneaccount addition:\n{}", Utils.listToString(phoneaccs, "\n   "));
      assertEquals(origpacnt, phoneaccs.size(), "new Phone account should be created");

   }

   @Test
   void addTransactionTest()
   {
      long accid=22L;
      long cptyaccid=35L;
      List<Transaction> txnsforacc = transactionJpaRepository.findByAccountIdOrderBySequenceAsc(accid);
      List<Transaction> txnsfortfracc = transactionJpaRepository.findByAccountIdOrderBySequenceAsc(cptyaccid);

      int origtxncnt = txnsforacc.size();
      int origtfrtxncnt = txnsfortfracc.size();
      Transaction origtxn = txnsforacc.get(5);
      TransactionItem origtxnitm = transactionService.mapToItem(origtxn, BalanceType.NORMAL);

      AddTransactionItem addtxnitm = new AddTransactionItem(
            origtxnitm.getAccid(), LocalDate.now(), "12345.12", "TEST",
            "TEST no TFR",
            Optional.empty());

      transactionService.addTransaction(addtxnitm);
      txnsforacc = transactionJpaRepository.findByAccountIdOrderBySequenceAsc(accid);
      txnsfortfracc = transactionJpaRepository.findByAccountIdOrderBySequenceAsc(cptyaccid);
      assertEquals(origtxncnt+1 , txnsforacc.size());
      assertEquals(origtfrtxncnt, txnsfortfracc.size());
      origtxncnt = txnsforacc.size();

      // last param must be PhoneAccount.ID, not Account.ID (which happens to be 242 for Account.ID=35)  
      // Example didn't work and no way to know why. Brute force filter works just fine!!
//      PhoneAccount phoneAccount = new PhoneAccount();
//      phoneAccount.setAccountId(cptyaccid);
//      Example<PhoneAccount> example = Example.of(phoneAccount);
//      phoneAccount = phoneAccountJpaRepository.findOne(example).orElseThrow(()-> new RuntimeException("no PhoneAccount for Account.ID:" + cptyaccid));
      PhoneAccount phoneAccount = phoneAccountJpaRepository.findAll().stream()
            .filter(p-> p.getAccountId()==cptyaccid)
            .findAny()
            .orElseThrow(()-> new RuntimeException("no PhoneAccount for Account.ID:" + cptyaccid));
      
      
      addtxnitm = new AddTransactionItem(
            origtxnitm.getAccid(), LocalDate.now(), "22345.12", "TEST",
            "TEST with TFR from (debit) src account",
            Optional.of(phoneAccount.getId()));
      transactionService.addTransaction(addtxnitm);
      txnsforacc = transactionJpaRepository.findByAccountIdOrderBySequenceAsc(accid);
      txnsfortfracc = transactionJpaRepository.findByAccountIdOrderBySequenceAsc(cptyaccid);
      log.info("addTransactionTest: transfer account credit: {}", txnsfortfracc.get(txnsfortfracc.size()-1));
      assertEquals(origtxncnt+1 , txnsforacc.size());
      assertEquals(origtfrtxncnt+1, txnsfortfracc.size());
      origtxncnt = txnsforacc.size();
      origtfrtxncnt = txnsfortfracc.size();

      // last param must be PhoneAccount.ID, not Account.ID (which happens to be 242 for Account.ID=35)
      addtxnitm = new AddTransactionItem(
            origtxnitm.getAccid(), LocalDate.now(), "-32345.12", "TEST",
            "TEST with TFR to (credit) src account",
            Optional.of(phoneAccount.getId()));
      transactionService.addTransaction(addtxnitm);
      txnsforacc = transactionJpaRepository.findByAccountIdOrderBySequenceAsc(accid);
      txnsfortfracc = transactionJpaRepository.findByAccountIdOrderBySequenceAsc(cptyaccid);
      log.info("addTransactionTest: transfer account debit: {}", txnsfortfracc.get(txnsfortfracc.size()-1));
      assertEquals(origtxncnt+1 , txnsforacc.size());
      assertEquals(origtfrtxncnt+1, txnsfortfracc.size());
      origtxncnt = txnsforacc.size();
      origtfrtxncnt = txnsfortfracc.size();

   }

   @Test
   void updateTransaction()
   {
      List<Transaction> txnsforacc = transactionJpaRepository.findByAccountId(22L, PageRequest.of(0, 25, Sort.by("sequence").descending()));

      Transaction origtxn = txnsforacc.get(5);
      TransactionItem origtxnitm = transactionService.mapToItem(origtxn, BalanceType.NORMAL);
      TransactionItem updtxnitm = new TransactionItem(
            origtxnitm.getAccid(), origtxnitm.getDate(), "9" + origtxnitm.getAmount(), "9" + origtxnitm.getAmount(),origtxnitm.getType(),
            "TEST" + origtxnitm.getComment(),
            origtxnitm.isLocked(), origtxnitm.getId(), origtxnitm.getToken(), origtxnitm.getBalance(), origtxnitm.getStatementref());

      Transaction updtxn = transactionService.updateTransaction(updtxnitm);
      log.info("testUpdateTransaction: new amount:{} balance: before:{} after:{}",
            updtxnitm.getAmount(), origtxn.getBalance(), updtxnitm.getBalance());

      Transaction updatedtxn = transactionService.getTransaction(origtxnitm.getId()).get();
      updtxnitm = transactionService.mapToItem(updatedtxn, BalanceType.NORMAL);
      log.info("testUpdateTransaction: update transaction: {}", updatedtxn);


      origtxnitm = new TransactionItem(
            origtxnitm.getAccid(), origtxnitm.getDate(), origtxnitm.getAmount(), origtxnitm.getAmount(),origtxnitm.getType(),
            origtxnitm.getComment(),
            origtxnitm.isLocked(), origtxnitm.getId(),
            updtxnitm.getToken(), null,  origtxnitm.getStatementref()); // this transaction now has the token of the update
      updtxn = transactionService.updateTransaction(origtxnitm);
   }
}
