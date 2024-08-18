package com.felixalacampagne.account.service;

import java.math.BigDecimal;
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

@Service
public class TransactionService
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final ObjectMapper objmap = new ObjectMapper();
   private final TransactionJpaRepository transactionJpaRepository;

   @Autowired
   public TransactionService(TransactionJpaRepository transactionJpaRepository) {
      this.transactionJpaRepository = transactionJpaRepository;
   }

   public String getTransactions(long accountId)
   {
      String result = "";
      List<TransactionItem> txnitems = getTransactionPage(0, 25, accountId).stream()
            .map(t -> mapToItem(t))
            .collect(Collectors.toList());
      try
      {
         result = objmap.writeValueAsString(txnitems);
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


   // TODO change Item to all Strings with required formatting, ie. 'yyyy-mm-dd' for date, two decimal places for amount
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
      return new TransactionItem(t.getSequence(), t.getDate(), amount, t.getBalance(), t.getComment());
   }
}
