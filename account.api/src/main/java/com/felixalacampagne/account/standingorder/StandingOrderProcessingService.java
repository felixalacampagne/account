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

@Service
public class StandingOrderProcessingService
{
   Logger log = LoggerFactory.getLogger(this.getClass());

   private final StandingOrdersJpaRepository standingOrdersJpaRepository;
   private final TransactionJpaRepository transactionJpaRepository;

   @Autowired
   public StandingOrderProcessingService(StandingOrdersJpaRepository standingOrdersJpaRepository,
                                         TransactionJpaRepository transactionJpaRepository)
   {
      this.standingOrdersJpaRepository = standingOrdersJpaRepository;
      this.transactionJpaRepository = transactionJpaRepository;
   }

   @Transactional
   void updateTxnAndSo(StandingOrders so, Transaction txn)
   {
      // needs to be in same transaction
      this.transactionJpaRepository.saveAndFlush(txn);
      this.standingOrdersJpaRepository.saveAndFlush(so);
   }
}
