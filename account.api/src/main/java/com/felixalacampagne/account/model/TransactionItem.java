package com.felixalacampagne.account.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransactionItem
{
   private final long id;
   private final String date;
   private final String amount;
   private final String balance;
   private final String comment;

   public TransactionItem(long sequence, String date, String amount, String balance, String comment)
   {
      this.id = sequence;
      this.date = date;
      this.amount = amount;
      this.balance = balance;
      this.comment = comment;
   }

   public long getId()
   {
      return id;
   }

   public String getDate()
   {
      return date;
   }

   public String getAmount()
   {
      return amount;
   }

   public String getBalance()
   {
      return balance;
   }

   public String getComment()
   {
      return comment;
   }

}
