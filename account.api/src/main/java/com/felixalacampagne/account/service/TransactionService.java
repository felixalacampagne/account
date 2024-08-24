package com.felixalacampagne.account.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
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
import com.felixalacampagne.account.persistence.entities.Transaction;
import com.felixalacampagne.account.persistence.repository.TransactionJpaRepository;
import com.felixalacampagne.account.model.TransactionItem;
import com.felixalacampagne.account.model.Transactions;

@Service
public class TransactionService
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final ObjectMapper objmap = new ObjectMapper();
   private final TransactionJpaRepository transactionJpaRepository;
   private final DateTimeFormatter DATEFORMAT_YYYYMMDD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
   
   @Autowired
   public TransactionService(TransactionJpaRepository transactionJpaRepository) {
      this.transactionJpaRepository = transactionJpaRepository;
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
      List<Transaction> txns = transactionJpaRepository.
            findByAccountId(accountId,  PageRequest.of(page, rows, Sort.by("sequence").descending())).stream()
            .sorted(Comparator.comparingLong(Transaction::getSequence))
            .collect(Collectors.toList());
      return txns;
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
