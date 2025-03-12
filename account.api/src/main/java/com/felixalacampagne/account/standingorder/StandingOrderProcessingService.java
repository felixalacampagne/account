package com.felixalacampagne.account.standingorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.felixalacampagne.account.persistence.entities.StandingOrders;
import com.felixalacampagne.account.persistence.entities.Transaction;
import com.felixalacampagne.account.persistence.repository.StandingOrdersJpaRepository;
import com.felixalacampagne.account.persistence.repository.TransactionJpaRepository;
import com.felixalacampagne.account.service.TransactionService;

@Service
public class StandingOrderProcessingService
{
   Logger log = LoggerFactory.getLogger(this.getClass());

   private final StandingOrdersJpaRepository standingOrdersJpaRepository;
   private final TransactionService transactionService;

   @Autowired
   public StandingOrderProcessingService(StandingOrdersJpaRepository standingOrdersJpaRepository,
         TransactionService transactionService)
   {
      this.standingOrdersJpaRepository = standingOrdersJpaRepository;
      this.transactionService = transactionService;
   }

   @Transactional
   void updateTxnAndSo(StandingOrders so, Transaction txn)
   {
      this.transactionService.add(txn);
      this.standingOrdersJpaRepository.saveAndFlush(so);
   }
}
