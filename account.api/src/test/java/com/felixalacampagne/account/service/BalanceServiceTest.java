package com.felixalacampagne.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import com.felixalacampagne.account.AccountTest;
import com.felixalacampagne.account.common.Utils;
import com.felixalacampagne.account.persistence.entities.Transaction;
import com.felixalacampagne.account.persistence.repository.TransactionJpaRepository;

@AccountTest
public class BalanceServiceTest
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());

   @Autowired
   private BalanceService balanceService;

   @Autowired
   private TransactionJpaRepository transactionJpaRepository;

   @Test
   void testCalcBalances()
   {

      Optional<Transaction> optTrans = balanceService.calculateCheckedBalances(22L, Optional.empty());
      assertTrue(optTrans.isPresent());
      Transaction t = optTrans.get();
      log.info("testCalcBalances: id:{} amount:{} balance:{} ref:{} checked:{} checked balance:{}",
            t.getSequence(), Utils.getAmount(t), t.getBalance(), t.getStid(), t.getChecked(), t.getCheckedBalance());

      BigDecimal origCBal = t.getCheckedBalance();

      List<Transaction> ctxns = transactionJpaRepository.findByAccountIdAndCheckedOrderBySequenceAsc(22, true);
      log.info("testCalcBalances: checked transaction count: {}", ctxns.size());

      Transaction tupd = ctxns.get(ctxns.size() / 2);
      tupd.setDebit(BigDecimal.valueOf(10000).add(tupd.getDebit()));
      tupd = transactionJpaRepository.save(tupd);
      optTrans = balanceService.calculateCheckedBalances(22L, Optional.of(tupd));
      assertTrue(optTrans.isPresent());
      t = optTrans.get();
      log.info("testCalcBalances: id:{} amount:{} balance:{} ref:{} checked:{} checked balance:{}",
            t.getSequence(), Utils.getAmount(t), t.getBalance(), t.getStid(), t.getChecked(), t.getCheckedBalance());

      assertEquals(origCBal.subtract(BigDecimal.valueOf(10000)), t.getCheckedBalance());

   }
}
