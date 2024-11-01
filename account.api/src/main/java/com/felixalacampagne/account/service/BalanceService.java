package com.felixalacampagne.account.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
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

   @Transactional
   public Transaction calculateBalances(Transaction startTransaction)
   {
      log.info("calculateBalances: startTransaction:{}", startTransaction.getSequence());
      BigDecimal balance = BigDecimal.ZERO;
      BigDecimal amt = Utils.getAmount(startTransaction);

      // Get last transaction
      Optional<Transaction> prevtxn = getPreviousTransaction(startTransaction);
      if(prevtxn.isPresent())
      {
         balance = Utils.getZeroOrValue(prevtxn.get().getBalance());
      }
      balance = balance.add(amt);

      // Could check if new balance is same as old balance and stop if no change
      startTransaction.setBalance(balance);
      startTransaction = transactionJpaRepository.save(startTransaction);

      // Now need to update the balance for any transactions which occurred AFTER the updated transaction
      List<Transaction> txns = getFollowingTransactions(startTransaction);

      for(Transaction nxttxn : txns)
      {
         amt = Utils.getAmount(nxttxn);
         balance = balance.add(amt);
         nxttxn.setBalance(balance);
      }
      transactionJpaRepository.saveAll(txns);
      transactionJpaRepository.flush();
      log.info("calculateBalances: done startTransaction:{}", startTransaction.getSequence());
      return startTransaction;
   }

   @Transactional
   public Optional<Transaction> calculateCheckedBalances(long accountId, Optional<Transaction> startTransaction)
   {
      List<Transaction> chktxns = transactionJpaRepository.findByAccountIdAndCheckedOrderBySequenceAsc(accountId, true);
      if(chktxns.isEmpty())
      {
         return Optional.empty();
      }
      
      log.debug("calculateCheckedBalances: start: first id:{} final id:{} chkd bal: {}", 
      		chktxns.get(0).getSequence(),
      		chktxns.get(chktxns.size()-1).getSequence(),
      		chktxns.get(chktxns.size()-1).getCheckedBalance());
      BigDecimal balance = BigDecimal.ZERO;
      BigDecimal amt = BigDecimal.ZERO;

      if(startTransaction.isPresent())
      {
      	// Search backwards for the first transaction before start transaction with a checked balance, 
      	// ie. the seed transaction
      	// This balance is used as the start balance and the list adjusted to start at the next
      	// transaction after the seed transaction - usually it will be the start transaction
      	long stid = startTransaction.get().getSequence();
      	List<Transaction> subchktxns = chktxns;
      	for(int i = chktxns.size() - 1 ; i > -1 ; i--)
      	{
      		Transaction pt = chktxns.get(i);
      		if((pt.getSequence() < stid) && (pt.getCheckedBalance() != null))
      		{
      			balance = pt.getCheckedBalance();
      			subchktxns = chktxns.subList(i+1, chktxns.size()); // to index must be the index +1, ie. size() for the last one
      			break;
      		}
      	}
      	chktxns = subchktxns;
      }

      log.debug("calculateCheckedBalances: adjusted for start txn: first id:{} final id:{} chkd bal: {}", 
      		chktxns.get(0).getSequence(),
      		chktxns.get(chktxns.size()-1).getSequence(),
      		chktxns.get(chktxns.size()-1).getCheckedBalance());
      
      // Calculate the checked balances, only update to DB those balances
      // which have changed. Calling saveAll on the entire list causes all
      // records to be updated even when they haven't changed, which is quite slow. 
      List<Transaction> updtxns = new ArrayList<>();
      for(Transaction nxttxn : chktxns)
      {
         amt = Utils.getAmount(nxttxn);
         balance = balance.add(amt); 
         if((nxttxn.getCheckedBalance() == null) || balance.compareTo(nxttxn.getCheckedBalance()) != 0)
         {
         	nxttxn.setCheckedBalance(balance);
         	updtxns.add(nxttxn);
         }
      }
      
      if(!updtxns.isEmpty())
      {
      	transactionJpaRepository.saveAll(chktxns);
      	transactionJpaRepository.flush();
      }
      log.debug("calculateCheckedBalances: finish: first id:{} final id: {} chkd bal: {}",
                        chktxns.get(0).getSequence(),
      		      		chktxns.get(chktxns.size()-1).getSequence(),
      		      		chktxns.get(chktxns.size()-1).getCheckedBalance());
      return Optional.of(chktxns.get(chktxns.size()-1));
   }

   // These were originally intended to go in TransactionService but that causes a
   // circular dependency between TransactionService and BalanceService.
   // BalanceService is only needed to make sure that Spring recognises the @Transactionals
   // since there is some issue when calling a @Transactional method from a method in the
   // same class.
   // Couldn't immediately think of an alternative way to do it and these methods are only really needed for
   // balance calculations.
   // Workaround could be to inject BalanceService into TransactionService and use @PostConstruct init method
   // to set TransactionService in BalanceService. There are other options using @Lazy.
   public Optional<Transaction> getLatestTransaction(long accountId)
   {
      return transactionJpaRepository.findFirstByAccountIdOrderBySequenceDesc(accountId);
   }

   public Optional<Transaction> getPreviousTransaction(Transaction txn)
   {
      return transactionJpaRepository.findFirstByAccountIdAndSequenceLessThanOrderBySequenceDesc(txn.getAccountId(), txn.getSequence());
   }

   public List<Transaction> getFollowingTransactions(Transaction txn)
   {
      return transactionJpaRepository.findByAccountIdAndSequenceGreaterThanOrderBySequenceAsc(txn.getAccountId(), txn.getSequence());
   }


}
