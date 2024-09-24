package com.felixalacampagne.account.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import com.felixalacampagne.account.AccountTest;
import com.felixalacampagne.account.model.TransactionItem;
import com.felixalacampagne.account.persistence.entities.Transaction;
import com.felixalacampagne.account.persistence.repository.TransactionJpaRepository;



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

   @Test
   void testGetTransactions()
   {
      String txnjson = transactionService.getTransactionsJson(1L);
      log.info("testGetTransactions: transaction list\n{}", txnjson);
   }

   @Test
   void testUpdateTransaction()
   {
      Transaction transaction = transactionJpaRepository.findByAccountId(1L, Pageable.unpaged())
                .stream().filter((t) -> !t.getChecked())
                         .findAny()
                         .get();

      Transaction origtxn = transactionService.getTransaction(transaction.getSequence()).get();
      TransactionItem origtxnitm = transactionService.mapToItem(origtxn);
      TransactionItem updtxnitm = new TransactionItem(
            origtxnitm.getAccid(), origtxnitm.getDate(), origtxnitm.getAmount(), origtxnitm.getType(),
            "TEST" + origtxnitm.getComment(),
            origtxnitm.isLocked(), origtxnitm.getId(), origtxnitm.getToken());

      transactionService.updateTransaction(updtxnitm);


      Transaction updatedtxn = transactionService.getTransaction(origtxnitm.getId()).get();
      updtxnitm = transactionService.mapToItem(updatedtxn);
      log.info("testUpdateTransaction: update transaction: {}", updatedtxn);


      origtxnitm = new TransactionItem(
            origtxnitm.getAccid(), origtxnitm.getDate(), origtxnitm.getAmount(), origtxnitm.getType(),
            origtxnitm.getComment(),
            origtxnitm.isLocked(), origtxnitm.getId(),
            updtxnitm.getToken()); // this transaction now has the token of the update
      transactionService.updateTransaction(origtxnitm);
   }
}
