package com.felixalacampagne.account.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.felixalacampagne.account.common.Utils;
import com.felixalacampagne.account.model.TransactionItem;
import com.felixalacampagne.account.model.Transactions;
import com.felixalacampagne.account.persistence.entities.Transaction;
import com.felixalacampagne.account.persistence.repository.TransactionJpaRepository;

@Service
public class TransactionService
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());
//   private final ObjectMapper localdateJsonMapper;

   private final TransactionJpaRepository transactionJpaRepository;
   private final ConnectionResurrector<TransactionJpaRepository> connectionResurrector;

   BalanceService balanceService;

   @Autowired
   public TransactionService(TransactionJpaRepository transactionJpaRepository,
                             BalanceService balanceService
//                             ,ObjectMapper localdateJsonMapper
                             ) {
      this.transactionJpaRepository = transactionJpaRepository;
      this.balanceService = balanceService;
      this.connectionResurrector = new ConnectionResurrector<TransactionJpaRepository>(transactionJpaRepository, TransactionJpaRepository.class);

//      this.localdateJsonMapper = localdateJsonMapper;
   }

   public Transactions getTransactions(long accountId)
   {
      List<TransactionItem> txnitems = getTransactionPage(0, 25, accountId).stream()
            .map(t -> mapToItem(t))
            .collect(Collectors.toList());
      Transactions trns = new Transactions(txnitems); // For fronted compatibility
      return trns;
   }


//   public String getTransactionsJson(long accountId)
//   {
//      String result = "";
//      Transactions trns = getTransactions(accountId);
//      try
//      {
//         result = localdateJsonMapper.writeValueAsString(trns);
//      }
//      catch (JsonProcessingException e)
//      {
//         log.info("getTransactions: failed to serialize account list to json:", e);
//      }
//      return result;
//   }

   public List<Transaction> getTransactionPage(int page, int rows, long accountId)
   {
      connectionResurrector.ressurectConnection();
      List<Transaction> txns = transactionJpaRepository.
            findByAccountId(accountId,  PageRequest.of(page, rows, Sort.by("sequence").descending())).stream()
            .sorted(Comparator.comparingLong(Transaction::getSequence))
            .collect(Collectors.toList());
      return txns;
   }

   public Optional<Transaction> getTransaction(long id)
   {
      connectionResurrector.ressurectConnection();

      // Fingers crossed the magic works and this uses transaction.sequence
      return transactionJpaRepository.findById(id);
   }

   public void addTransaction(TransactionItem transactionItem)
   {
      Transaction txn = mapToEntity(transactionItem);



      txn = add(txn);
      log.info("addTransaction: added transaction for account id {}: id:{}", txn.getAccountId(), txn.getSequence());
   }

   // This must be @Transactional because it calls 'update()' which is @Transactional and in the same class which
   // means the Spring @Transactional proxies would be bypassed - I think
   @Transactional
   public Transaction updateTransaction(TransactionItem transactionItem)
   {
      log.info("updateTransaction: transactionItem:{}", transactionItem);
      if(transactionItem == null)
         return null;
      Transaction txn = getTransaction(transactionItem.getId())
            .orElseThrow(()->new AccountException("Transaction id " + transactionItem.getId() + " not found"));

      if(txn.getChecked())
      {
         log.info("updateTransaction: Locked transaction: id:{}", transactionItem.getId());

         throw new  AccountException("Transaction id " + transactionItem.getId() + " is locked");
      }

      String origToken = Utils.getToken(txn);
      if(!origToken.equals(transactionItem.getToken()))
      {
         log.info("updateTransaction: Token mismatch for transaction id:{}: original:{} supplied:{}",
               transactionItem.getId(), origToken, transactionItem.getToken());
         throw new  AccountException("Token does not match Transaction id " + transactionItem.getId());
      }

      Transaction updtxn = mapToEntity(transactionItem);
      if(txn.getAccountId() != updtxn.getAccountId())
      {
         log.info("updateTransaction: Account id does not match transaction id:{}: original:{} supplied:{}",
               transactionItem.getId(), txn.getAccountId(), updtxn.getAccountId());
         throw new  AccountException("Account id does not match Transaction id " + transactionItem.getId());
      }

      // updtxn is incomplete and only a limited number of values can be
      // updated from web UI at the moment so copy values into txn from DB
      txn.setDate(updtxn.getDate());
      txn.setType(updtxn.getType());
      txn.setComment(updtxn.getComment());
      txn.setCredit(updtxn.getCredit());
      txn.setDebit(updtxn.getDebit());

      return update(txn);
   }

   // This must be @Transactional because it recalculates the balances of all following transactions
   // and any failure during this calculation should revert all changes
   @Transactional
   public Transaction update(Transaction txn)
   {
      txn = transactionJpaRepository.save(txn);
      txn = balanceService.calculateBalances(txn);
      return txn;
   }

   // Add is effectively the same as update but with 0 following transactions
   @Transactional
   public Transaction add(Transaction txn)
   {
      txn = transactionJpaRepository.save(txn);
      txn = balanceService.calculateBalances(txn);
      return txn;
   }

   private Transaction mapToEntity(TransactionItem transactionItem)
   {
      Transaction tosave = new Transaction();
      tosave.setAccountId(transactionItem.getAccid());

//      String isodate = transactionItem.getDate();
//      LocalDate localDate = LocalDate.parse(isodate, Utils.DATEFORMAT_YYYYMMDD);
//      tosave.setDate(localDate); // TODO: change the type to LocalDate
      tosave.setDate(transactionItem.getDate());

      tosave.setType(transactionItem.getType());
      tosave.setComment(transactionItem.getComment());

      BigDecimal amount = new BigDecimal(transactionItem.getAmount());
      amount.setScale(2); // Max. two decimal places for a normal currency transaction

      // With VB app both credit and debit could be set. This is not
      // supported for the web UI, ie. either credit or debit can be set, not both
      if(amount.signum() < 0)
      {
         // Transactions in web UI are +ve for DEBIT since debits are what is usually entered
         amount = amount.abs();
         tosave.setCredit(amount);
         tosave.setDebit(null);
      }
      else
      {
         tosave.setDebit(amount);
         tosave.setCredit(null);
      }
      return tosave;
   }

   public TransactionItem mapToItem(Transaction t)
   {
      BigDecimal amount = BigDecimal.ZERO;

      if(t.getDebit() != null)
      {
         amount = t.getDebit();
      }
      else if(t.getCredit() != null) // Shouldn't happen but does in the TEST DB so maybe can in live db
      {
         amount = t.getCredit().negate();
      }

      // jackson doesn't handle Java dates [it does now!!] and bigdecimal has too many decimal places so it's
      // simpler just to send the data as Strings with the desired formating.
      String token = Utils.getToken(t);
      return new TransactionItem(t.getAccountId(),
            t.getDate(),
            Utils.formatAmount(amount),
            t.getType(),
            t.getComment(),
            t.getChecked(), t.getSequence(), token, Utils.formatAmount(t.getBalance()));
   }



}
