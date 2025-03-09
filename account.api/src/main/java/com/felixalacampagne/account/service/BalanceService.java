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
import org.springframework.transaction.annotation.Transactional;

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

//   @Transactional
//   public Transaction calculateBalancesOld(Transaction startTransaction)
//   {
//      log.info("calculateBalances: startTransaction:{}", startTransaction.getSequence());
//      BigDecimal balance = BigDecimal.ZERO;
//      BigDecimal amt = Utils.getAmount(startTransaction);
//
//      // Get last transaction
//      Optional<Transaction> prevtxn = getPreviousTransaction(startTransaction);
//      if(prevtxn.isPresent())
//      {
//         balance = Utils.getZeroOrValue(prevtxn.get().getBalance());
//      }
//      balance = balance.add(amt);
//
//      // Could check if new balance is same as old balance and stop if no change
//      startTransaction.setBalance(balance);
//      startTransaction = transactionJpaRepository.save(startTransaction);
//
//      // Now need to update the balance for any transactions which occurred AFTER the updated transaction
//      List<Transaction> txns = getFollowingTransactions(startTransaction);
//
//      for(Transaction nxttxn : txns)
//      {
//         amt = Utils.getAmount(nxttxn);
//         balance = balance.add(amt);
//         nxttxn.setBalance(balance);
//      }
//      transactionJpaRepository.saveAll(txns);
//      transactionJpaRepository.flush();
//      log.info("calculateBalances: done startTransaction:{}", startTransaction.getSequence());
//      return txns.get(txns.size()-1);
//   }

   @Transactional
   public void doBalanceCalculation(List<Transaction> txns,
                                    Function<Transaction, BigDecimal> balanceGetter,
                                    BiConsumer<Transaction, BigDecimal> balanceSetter,
                                    Optional<Transaction> startTransaction)
   {
      log.debug("doBalanceCalculation: start: first id:{} final id:{} chkd bal: {}{}",
            txns.get(0).getSequence(),
            txns.get(txns.size()-1).getSequence(),
            balanceGetter.apply(txns.get(txns.size()-1)),
            startTransaction.map(t -> " start txn id:" + t.getSequence()).orElse(""));

      BigDecimal balance = BigDecimal.ZERO;
      BigDecimal amt = BigDecimal.ZERO;

      if(startTransaction.isPresent())
      {
        // Search backwards for the first transaction before start transaction with a non-null balance,
        // ie. the seed transaction
        // This balance is used as the start balance and the list adjusted to start at the next
        // transaction after the seed transaction - usually it will be the start transaction
        long stid = startTransaction.get().getSequence();
        boolean startFound = false;
        List<Transaction> subchktxns = txns;
        for(int i = txns.size() - 1 ; i > -1 ; i--)
        {
            Transaction pt = txns.get(i);
            BigDecimal curbalance = balanceGetter.apply(pt);

            // Find starttransaction in the list and then take the first previous
            // non-null balance from the list.
            // WARNING: starttransaction may not be in the list, eg. deleted, unverified. Since this
            // method is unaware of the sort criteria of the list it cannot 'guess' which
            // entry is previous to the starttransaction so the calculation will be from the first 
            // entry in the list which will make 'removal' actions quite time consuming!!!
            // TODO: Would be nice to find a way to avoid the full recalculation when an entry is removed.
            // The only way would be for the caller to supply the shortened list.
            if(pt.getSequence() == stid)
            {
               startFound = true;
            }
            else if(startFound && (curbalance != null))
            {
                balance = curbalance;
                subchktxns = txns.subList(i+1, txns.size()); // to index must be the index +1, ie. size() for the last one
                break;
            }
        }
        txns = subchktxns;
      }

      log.debug("doBalanceCalculation: adjusted for start txn: first id:{} final id:{} chkd bal: {}",
            txns.get(0).getSequence(),
            txns.get(txns.size()-1).getSequence(),
            balanceGetter.apply(txns.get(txns.size()-1)));

      List<Transaction> updtxns = new ArrayList<>();
      for(Transaction nxttxn : txns)
      {
         amt = Utils.getAmount(nxttxn);
         balance = balance.add(amt);
         BigDecimal curbalance = balanceGetter.apply(nxttxn);
         if((curbalance == null) || balance.compareTo(curbalance) != 0)
         {
            log.debug("doBalanceCalculation: update balance old:{} new:{}", curbalance, balance);
            balanceSetter.accept(nxttxn, balance);  // nxttxn.setCheckedBalance(balance);
            updtxns.add(nxttxn);
         }
      }

      if(!updtxns.isEmpty())
      {
         log.debug("doBalanceCalculation: saving {} records", updtxns.size());
         transactionJpaRepository.saveAll(updtxns);
         transactionJpaRepository.flush();
         log.debug("doBalanceCalculation: saved {} records", updtxns.size());
      }

      log.debug("doBalanceCalculation: finish: first id:{} final id: {} chkd bal: {}",
            txns.get(0).getSequence(),
            txns.get(txns.size()-1).getSequence(),
            balanceGetter.apply(txns.get(txns.size()-1)));
   }

   @Transactional // required as calls internal @Transactional
   public Optional<Transaction> calculateBalances(long accountId, Optional<Transaction> startTransaction)
   {
      List<Transaction> txns = transactionJpaRepository.findByAccountIdOrderBySequenceAsc(accountId);
      if(txns.isEmpty())
      {
         return Optional.empty();
      }

      doBalanceCalculation(txns,
            (t) -> t.getBalance(),
            (t, b) -> t.setBalance(b),
            startTransaction);
      return Optional.of(txns.get(txns.size()-1));

   }

   @Transactional // required as calls internal @Transactional
   public Optional<Transaction> calculateCheckedBalances(long accountId, Optional<Transaction> startTransaction)
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
            (t, b) -> t.setCheckedBalance(b),
            startTransaction);
      return Optional.of(chktxns.get(chktxns.size()-1));
   }

   @Transactional // required as calls internal @Transactional
   public Optional<Transaction> calculateDatesortedBalances(long accountId, Optional<Transaction> startTransaction)
   {
      List<Transaction> txns = transactionJpaRepository.findByAccountIdOrderByDateAscSequenceAsc(accountId);
      if(txns.isEmpty())
      {
         return Optional.empty();
      }

      doBalanceCalculation(txns,
            (t) -> t.getSortedBalance(),
            (t, b) -> t.setSortedBalance(b),
            startTransaction);
      return Optional.of(txns.get(txns.size()-1));

   }

//   public Optional<Transaction> getLatestTransaction(long accountId)
//   {
//      return transactionJpaRepository.findFirstByAccountIdOrderBySequenceDesc(accountId);
//   }



}
