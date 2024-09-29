package com.felixalacampagne.account.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.felixalacampagne.account.model.TransactionItem;
import com.felixalacampagne.account.model.Transactions;
import com.felixalacampagne.account.persistence.entities.Transaction;
import com.felixalacampagne.account.persistence.repository.TransactionJpaRepository;

@Service
public class TransactionService
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final ObjectMapper objmap = new ObjectMapper();

   private final TransactionJpaRepository transactionJpaRepository;
   private final ConnectionResurrector<TransactionJpaRepository> connectionResurrector;

   // yyyy-MM-dd is the iso date format supported by Javascript Date
   private final DateTimeFormatter DATEFORMAT_YYYYMMDD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

   @Autowired
   public TransactionService(TransactionJpaRepository transactionJpaRepository) {
      this.transactionJpaRepository = transactionJpaRepository;
      this.connectionResurrector = new ConnectionResurrector<TransactionJpaRepository>(transactionJpaRepository, TransactionJpaRepository.class);
   }

   public Transactions getTransactions(long accountId)
   {
      List<TransactionItem> txnitems = getTransactionPage(0, 25, accountId).stream()
            .map(t -> mapToItem(t))
            .collect(Collectors.toList());
      Transactions trns = new Transactions(txnitems); // For fronted compatibility
      return trns;
   }


   public String getTransactionsJson(long accountId)
   {
      String result = "";
      Transactions trns = getTransactions(accountId);
      try
      {
         result = objmap.writeValueAsString(trns);
      }
      catch (JsonProcessingException e)
      {
         log.info("getTransactions: failed to serialize account list to json:", e);
      }
      return result;
   }

   public List<Transaction> getTransactionPage(int page, int rows, long accountId)
   {
      connectionResurrector.ressurectConnection();
      List<Transaction> txns = transactionJpaRepository.
            findByAccountId(accountId,  PageRequest.of(page, rows, Sort.by("sequence").descending())).stream()
            .sorted(Comparator.comparingLong(Transaction::getSequence))
            .collect(Collectors.toList());
      return txns;
   }

   public void addTransaction(TransactionItem transactionItem)
   {
      Transaction txn = mapToEntity(transactionItem);

      connectionResurrector.ressurectConnection();

      // TODO: calculate the balance field
      txn = add(txn);
      log.info("addTransaction: added transaction for account id {}: id:{}", txn.getAccountId(), txn.getSequence());
   }

   public Optional<Transaction> getTransaction(long id)
   {
      connectionResurrector.ressurectConnection();

      // Fingers crossed the magic works and this uses transaction.sequence
      return transactionJpaRepository.findById(id);
   }

   public Optional<Transaction> getLatestTransaction(long accountId)
   {
      return transactionJpaRepository.findFirstByAccountIdOrderBySequenceDesc(accountId);
   }

   public void updateTransaction(TransactionItem transactionItem)
   {
      log.info("updateTransaction: transactionItem:{}", transactionItem);
      if(transactionItem == null)
         return;
      Transaction txn = getTransaction(transactionItem.getId())
            .orElseThrow(()->new AccountException("Transaction id " + transactionItem.getId() + " not found"));

      if(txn.getChecked())
      {
         log.info("updateTransaction: Locked transaction: id:{}", transactionItem.getId());

         throw new  AccountException("Transaction id " + transactionItem.getId() + " is locked");
      }

      String origToken = getToken(txn);
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
      if(!(areEqual(txn.getCredit(), updtxn.getCredit()) && areEqual(txn.getDebit(), updtxn.getDebit())))
      {
         // TODO: re-calculate the balance fields for this a subsequent txns. Will need a DB transaction.
         txn.setBalance(null);
      }
      txn.setCredit(updtxn.getCredit());
      txn.setDebit(updtxn.getDebit());
      transactionJpaRepository.saveAndFlush(txn);
   }

   public BigDecimal getZeroOrValue(BigDecimal value)
   {
   	return value==null ? BigDecimal.ZERO : value;
   }
   
   public BigDecimal getAmount(Transaction txn)
   {
   	return getZeroOrValue(txn.getCredit()).subtract(getZeroOrValue(txn.getDebit()));
   }
   
   @Tranasactional
   public Transaction update(Transaction txn)
   {
   	BigDecimal balance = BigDecimal.ZERO;
   	BigDecimal amt = getAmount(txn);
   	
      // Get last transaction
      Optional<Transaction> prevtxn = getPreviousTransaction(txn);
      if(prevtxn.isPresent() && (prevtxn.get().getBalance() != null))
      {
         balance = getZeroOrValue(prevtxn.get().getBalance());
      }
      balance = balance.subtract(amt);
      txn.setBalance(balance);
      transactionJpaRepository.saveAndFlush(txn);
      
      // Now need to update the balanace for any transactions which occurred AFTER the updated transaction
      List<Transaction> txns = getFollowingTransaction(txn);
      
      for(Transaction nxttxn : txns)
      {
      	amt = getAmount(nxttxn);
      	balance = balance.subtract(amt);
      	nxttxn.setBalance(balance);
      	transactionJpaRepository.saveAndFlush(nxttxn);
      }
   }
   
   public Transaction add(Transaction txn)
   {
   	BigDecimal balance = BigDecimal.ZERO;
   	BigDecimal amt = getAmount(txn);
   	
      // Get last transaction
      Optional<Transaction> lasttxn = getLatestTransaction(txn.getAccountId());

      // Calculate next balance
      if(lasttxn.isPresent() && (lasttxn.get().getBalance() != null))
      {
         balance = getZeroOrValue(lasttxn.get().getBalance());
      }
      balance = balance.subtract(amt);
      
      txn.setBalance(balance);
   	// Need to calculate the new balance
      return transactionJpaRepository.saveAndFlush(txn);
   }

   private boolean areEqual(BigDecimal one, BigDecimal two)
   {
      if((one == null) && (two == null))
         return true;
      else if(((one == null) && (two != null))
             || ((one != null) && (two == null)))
         return false;
      return one.compareTo(two) == 0;
   }

   public String getToken(Transaction transaction)
   {
      // Crude value intended to confirm the record being updated is the correct one,
      // eg. to avoid a wrong/spoofed ID from being sent from the client
      return formatDate(transaction.getDate())
         + ":" + formatAmount(transaction.getDebit())
         + ":" + formatAmount(transaction.getDebit())
         + ":" + transaction.getType()
         + ":" + transaction.getComment()
         + ":" + transaction.getChecked();
   }

   private Transaction mapToEntity(TransactionItem transactionItem)
   {
      Transaction tosave = new Transaction();
      tosave.setAccountId(transactionItem.getAccid());

      String isodate = transactionItem.getDate();

      LocalDate localDate = LocalDate.parse(isodate, DATEFORMAT_YYYYMMDD);
      tosave.setDate(localDate); // TODO: change the type to LocalDate
      tosave.setType(transactionItem.getType());
      tosave.setComment(transactionItem.getComment());

      BigDecimal amount = new BigDecimal(transactionItem.getAmount());
      amount.setScale(2); // Max. two decimal places for a normal currency transaction
      if(amount.signum() < 0)
      {
         amount = amount.abs();
         tosave.setCredit(amount);
      }
      else
      {
         tosave.setDebit(amount);
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



      // jackson doesn't handle Java dates and bigdecimal has too many decimal places so it's
      // simpler just to send the data as Strings with the desired formating.
      String token = getToken(t);
      return new TransactionItem(t.getAccountId(),
      		formatDate(t.getDate()),
            formatAmount(amount),
            t.getType(),
            t.getComment(),
            t.getChecked(), t.getSequence(), token, formatAmount(t.getBalance()));
   }

   private String formatAmount(BigDecimal bigdec)
   {
   String amt = "";
      if(bigdec != null)
      {
         amt = bigdec.setScale(2, RoundingMode.HALF_UP).toString();
      }
      return amt;
   }

   private String formatDate(LocalDate ts)
   {
      String date = "";
      if(ts != null)
      {
         date = ts.format(DATEFORMAT_YYYYMMDD);
      }
      return date;
   }
}
