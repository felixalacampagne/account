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

      Optional<Transaction> optTrans = balanceService.calculateCheckedBalances(22L);
      assertTrue(optTrans.isPresent());
      Transaction t = optTrans.get();
      log.info("testCalcBalances: id:{} amount:{} balance:{} ref:{} checked:{} checked balance:{}",
            t.getSequence(), Utils.getAmount(t), t.getBalance(), t.getStid(), t.getChecked(), t.getCheckedBalance());

      BigDecimal origCBal = t.getCheckedBalance();

      List<Transaction> ctxns = transactionJpaRepository.findByAccountIdAndCheckedIsTrueOrderByDateAscSequenceAsc(22);
      log.info("testCalcBalances: checked transaction count: {}", ctxns.size());

      Transaction tupd = ctxns.get(ctxns.size() / 2);
      tupd.setDebit(BigDecimal.valueOf(10000).add(tupd.getDebit()));
      tupd = transactionJpaRepository.save(tupd);
      optTrans = balanceService.calculateCheckedBalances(22L);
      assertTrue(optTrans.isPresent());
      t = optTrans.get();
      log.info("testCalcBalances: id:{} amount:{} balance:{} ref:{} checked:{} checked balance:{}",
            t.getSequence(), Utils.getAmount(t), t.getBalance(), t.getStid(), t.getChecked(), t.getCheckedBalance());

      assertEquals(origCBal.subtract(BigDecimal.valueOf(10000)), t.getCheckedBalance());

   }

   @Test
   void testBalances()
   {
      List<Transaction> txns = transactionJpaRepository.findByAccountIdOrderBySequenceAsc(22);

      Transaction lastTxn = txns.get(txns.size()-1);
      BigDecimal origBal = lastTxn.getBalance();

      log.info("testBalances: id:{} amount:{} initial balance: {}",
            lastTxn.getSequence(), Utils.getAmount(lastTxn), origBal);

      Transaction tupd = txns.get(txns.size() / 2);
      tupd.setDebit(BigDecimal.valueOf(10000).add(tupd.getDebit()));
      tupd = transactionJpaRepository.save(tupd);

      Optional<Transaction> optt = balanceService.calculateBalances(22);
      assertTrue(optt.isPresent());

      Transaction t = optt.get();
      log.info("testBalances: id:{} amount:{} balance:{}",
            t.getSequence(), Utils.getAmount(t), t.getBalance());

      assertEquals(origBal.subtract(BigDecimal.valueOf(10000)), t.getBalance());

   }

}
