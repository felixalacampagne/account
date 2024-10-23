package com.felixalacampagne.account.service;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.felixalacampagne.account.AccountTest;
import com.felixalacampagne.account.model.TransactionItem;
import com.felixalacampagne.account.persistence.entities.Transaction;
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
   void testUpdateTransaction()
   {
      List<Transaction> txnsforacc = transactionJpaRepository.findByAccountId(22L, PageRequest.of(0, 25, Sort.by("sequence").descending()));

      Transaction origtxn = txnsforacc.get(5);
      TransactionItem origtxnitm = transactionService.mapToItem(origtxn, BalanceType.NORMAL);
      TransactionItem updtxnitm = new TransactionItem(
            origtxnitm.getAccid(), origtxnitm.getDate(), "9" + origtxnitm.getAmount(), origtxnitm.getType(),
            "TEST" + origtxnitm.getComment(),
            origtxnitm.isLocked(), origtxnitm.getId(), origtxnitm.getToken(), origtxnitm.getBalance(), origtxnitm.getStatementref());

      Transaction updtxn = transactionService.updateTransaction(updtxnitm);
      log.info("testUpdateTransaction: new amount:{} balance: before:{} after:{}",
            updtxnitm.getAmount(), origtxn.getBalance(), updtxnitm.getBalance());

      Transaction updatedtxn = transactionService.getTransaction(origtxnitm.getId()).get();
      updtxnitm = transactionService.mapToItem(updatedtxn, BalanceType.NORMAL);
      log.info("testUpdateTransaction: update transaction: {}", updatedtxn);


      origtxnitm = new TransactionItem(
            origtxnitm.getAccid(), origtxnitm.getDate(), origtxnitm.getAmount(), origtxnitm.getType(),
            origtxnitm.getComment(),
            origtxnitm.isLocked(), origtxnitm.getId(),
            updtxnitm.getToken(), null,  origtxnitm.getStatementref()); // this transaction now has the token of the update
      updtxn = transactionService.updateTransaction(origtxnitm);
   }
}
