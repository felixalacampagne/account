package com.felixalacampagne.account.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.felixalacampagne.account.persistence.entities.Transaction;

import com.felixalacampagne.account.AccountTest;

@AccountTest
class TransactionJpaRepositoryTest
{
   Logger log = LoggerFactory.getLogger(this.getClass());

   @Autowired
   TransactionJpaRepository transactionJpaRepository;

   @BeforeEach
   void setUp() throws Exception
   {
   }

   @AfterEach
   void tearDown() throws Exception
   {
   }

   @Test
   void testFindall()
   {
      List<Transaction> alltxns = transactionJpaRepository.findAll();
      List<Transaction> txnsforacc = transactionJpaRepository.findByAccountId(1L, Pageable.unpaged());
      log.info("testFindall: found {} transactions in total, of which {} for account 1",
            alltxns.size()
            , txnsforacc.size()
            );
   }

   @Test
   void testGetLast()
   {
      int cnt = (int) transactionJpaRepository.countByAccountId(1L);

      List<Transaction> txnsforacc = transactionJpaRepository.findByAccountId(1L, PageRequest.of(0, 25, Sort.by("sequence").descending()));
      log.info("testGetLast: of {} records {} returned;\n{}",
            cnt,
            txnsforacc.size(),
            txnsforacc.stream()
            .map(t -> "" + t.getSequence() + " " + t.getDate() + " " + t.getComment())
            .collect(Collectors.joining("\n"))
            );
      
      txnsforacc = transactionJpaRepository.findByAccountId(1L, PageRequest.of(2, 25, Sort.by("sequence").descending()));
      log.info("testGetLast: of Page 3 records;\n{}",
            txnsforacc.stream()
            .map(t -> "" + t.getSequence() + " " + t.getDate() + " " + t.getComment())
            .collect(Collectors.joining("\n"))
            );
   }
   
   @Test
   void testAddTransaction()
   {
      long txncount = transactionJpaRepository.countByAccountId(1);

      Transaction tosave = new Transaction();
      tosave.setAccountId(1L);
      tosave.setDate(Timestamp.valueOf(LocalDateTime.now()));
      tosave.setType("TEST");
      tosave.setComment("This is a test");
      tosave.setDebit(BigDecimal.valueOf(2.34));
      tosave.setBalance(BigDecimal.valueOf(1000.00));

      Transaction saved = transactionJpaRepository.save(tosave);

      assertEquals((txncount+1), transactionJpaRepository.countByAccountId(1));


      long newid = saved.getSequence();
      System.out.println("new id is " + newid);
      log.info("testSave: transaction saved");

   }

}
