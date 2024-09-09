package com.felixalacampagne.account.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
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
      // TODO: it is advised not to use IDs in front-end. Therefore should probably use the account name instead of the id
      // and map the name to the id here.
      Transaction txn = mapToEntity(transactionItem);
      
      connectionResurrector.ressurectConnection();
      txn = transactionJpaRepository.save(txn);
      log.info("addTransaction: added transaction for account id {}: id:{}", txn.getAccountId(), txn.getSequence());
   }
   
   private Transaction mapToEntity(TransactionItem transactionItem)
   {
      Transaction tosave = new Transaction();
      tosave.setAccountId((long)transactionItem.getAccid());
      
      String isodate = transactionItem.getDate();
      
      
      LocalDate localDate = LocalDate.parse(isodate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
      Timestamp tstamp = Timestamp.valueOf(localDate.atStartOfDay());
      
      tosave.setDate(tstamp); // TODO: change the type to LocalDate
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

   private TransactionItem mapToItem(Transaction t)
   {
      BigDecimal amount;

      if(t.getDebit() != null)
      {
         amount = t.getDebit();
      }
      else
      {
         amount = t.getCredit().negate();
      }
      
      // jackson doesn't handle Java dates and bigdecimal has too many decimal places so it's
      // simpler just to send the data as Strings with the desired formating.
      return new TransactionItem(t.getAccountId(), 
            formatTimestamp(t.getDate()), 
            formatAmount(amount), 
            t.getType(), 
            t.getComment());
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
   
   private String formatTimestamp(Timestamp ts)
   {
      String date = "";
      if(ts != null)
      {
         date = ts.toLocalDateTime().toLocalDate().format(DATEFORMAT_YYYYMMDD);
      }
      return date;
   }
}
