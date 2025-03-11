package com.felixalacampagne.account.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.felixalacampagne.account.common.Utils;
import com.felixalacampagne.account.persistence.entities.Transaction;
import com.felixalacampagne.account.persistence.repository.TransactionJpaRepository;

@Service
public class BalanceService
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final TransactionJpaRepository transactionJpaRepository;

   public BalanceService(TransactionJpaRepository transactionJpaRepository) {
      this.transactionJpaRepository = transactionJpaRepository;
   }

   // Made non-Transactional in attempt to avoid 'Closed connection' when committing many records
   // @Transactional
   public void doBalanceCalculation(List<Transaction> txns,
                                    Function<Transaction, BigDecimal> balanceGetter,
                                    BiConsumer<Transaction, BigDecimal> balanceSetter)
   {
      log.debug("doBalanceCalculation: start: first id:{} final id:{} final bal: {}{}",
            txns.get(0).getSequence(),
            txns.get(txns.size()-1).getSequence(),
            balanceGetter.apply(txns.get(txns.size()-1)));

      BigDecimal balance = BigDecimal.ZERO;
      BigDecimal amt = BigDecimal.ZERO;


      List<Transaction> updtxns = new ArrayList<>();
      for(Transaction nxttxn : txns)
      {
         amt = Utils.getAmount(nxttxn);
         balance = balance.add(amt);
         BigDecimal curbalance = balanceGetter.apply(nxttxn);
         if((curbalance == null) || balance.compareTo(curbalance) != 0)
         {
            log.debug("doBalanceCalculation: update balance old:{} new:{}", curbalance, balance);
            balanceSetter.accept(nxttxn, balance);
            updtxns.add(nxttxn);
         }
      }

      if(!updtxns.isEmpty())
      {
         log.debug("doBalanceCalculation: saving {} records", updtxns.size());
         for(Transaction txn : updtxns)
         {
            transactionJpaRepository.save(txn);
            transactionJpaRepository.flush();
         }
         log.debug("doBalanceCalculation: saved {} records", updtxns.size());
      }

      log.debug("doBalanceCalculation: finish: result final bal: {}",
                balanceGetter.apply(txns.get(txns.size()-1)));
   }

   // Made non-Transactional in attempt to avoid 'Closed connection' when committing many records
   // @Transactional // required as calls internal @Transactional
   public Optional<Transaction> calculateBalances(long accountId)
   {
      List<Transaction> txns = transactionJpaRepository.findByAccountIdOrderBySequenceAsc(accountId);
      if(txns.isEmpty())
      {
         return Optional.empty();
      }

      doBalanceCalculation(txns,
            (t) -> t.getBalance(),
            (t, b) -> t.setBalance(b));
      return Optional.of(txns.get(txns.size()-1));

   }

   // Made non-Transactional in attempt to avoid 'Closed connection' when committing many records
   // @Transactional // required as calls internal @Transactional
   public Optional<Transaction> calculateCheckedBalances(long accountId)
   {

      // Ideally need to find a list starting from the first transaction with a non-null checked balance BEFORE
      // the startTransaction, knowing that startTransaction may not been removed.
      // Actually this is not necessary since the balances before the changed record do not change, in theory,
      // so they are not updated, in theory, so the complication of trying to determine the previous record
      // should not be necessary.
      // In case theory doesn't translate to practice this is how I was going to do it...
      // I started to make this change because the updates of recent records were taking a very long time and
      // the log appeared to indicate all records were being updated. When repeated with additional logging
      // the number of records updated was limited and the update was much faster than previously. Not sure
      // why, maybe the balances were actually wrong, or for some reason didn't compare to the new balances.
      // sort date desc, seq desc
      // date <= st_date
      // id< st_id
      // checkbal != null
      // first entry is the start of the list: fe

      // recalc list
      // sort date asc, seq asc
      // date >= fe_date
      // id >= fe_id
      // List<Transaction> chktxns;
      // if(startTransaction.isPresent())
      // {
      //    Transaction t = startTransaction.get();
      //    chktxns = transactionJpaRepository.findPrevCheckedBal(accountId, t.getSequence(), t.getDate());
      //    if(chktxns.isEmpty())
      //    {
      //       chktxns = transactionJpaRepository.findByAccountIdAndCheckedIsTrueOrderByDateAscSequenceAsc(accountId);
      //    }
      //    else
      //    {
      //       Transaction p = chktxns.get(0);
      //       chktxns = transactionJpaRepository.findCheckedFromTransaction(accountId, p.getSequence(), p.getDate());
      //    }
      // }
      // else
      // {
      //    chktxns = transactionJpaRepository.findByAccountIdAndCheckedIsTrueOrderByDateAscSequenceAsc(accountId);
      // }

      List<Transaction> chktxns;
      chktxns = transactionJpaRepository.findByAccountIdAndCheckedIsTrueOrderByDateAscSequenceAsc(accountId);


      if(chktxns.isEmpty())
      {
         return Optional.empty();
      }

      doBalanceCalculation(chktxns,
            (t) -> t.getCheckedBalance(),
            (t, b) -> t.setCheckedBalance(b));
      return Optional.of(chktxns.get(chktxns.size()-1));
   }

   // Made non-Transactional in attempt to avoid 'Closed connection' when committing many records
   // @Transactional // required as calls internal @Transactional
   public Optional<Transaction> calculateDatesortedBalances(long accountId)
   {
      List<Transaction> txns = transactionJpaRepository.findByAccountIdOrderByDateAscSequenceAsc(accountId);
      if(txns.isEmpty())
      {
         return Optional.empty();
      }

      doBalanceCalculation(txns,
            (t) -> t.getSortedBalance(),
            (t, b) -> t.setSortedBalance(b));
      return Optional.of(txns.get(txns.size()-1));

   }
}
