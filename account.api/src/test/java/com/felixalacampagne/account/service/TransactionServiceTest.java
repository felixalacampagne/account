package com.felixalacampagne.account.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.felixalacampagne.account.AccountTest;



@AccountTest
class TransactionServiceTest
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());

   @Autowired
   private TransactionService transactionService;
   @AfterEach
   void tearDown() throws Exception
   {
   }

   @Test
   void testGetTransactions()
   {
      String txnjson = transactionService.getTransactions(1L);
      log.info("testGetTransactions: transaction list\n{}", txnjson);
   }

}
