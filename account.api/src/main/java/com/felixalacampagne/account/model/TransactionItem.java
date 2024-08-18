package com.felixalacampagne.account.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class TransactionItem
{
   private final long id;
   private final Timestamp date;
   private final BigDecimal amount;
   private final BigDecimal balance;
   private final String comment;

   public TransactionItem(long sequence, Timestamp date, BigDecimal amount, BigDecimal balance, String comment)
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

   public Timestamp getDate()
   {
      return date;
   }

   public BigDecimal getAmount()
   {
      return amount;
   }

   public BigDecimal getBalance()
   {
      return balance;
   }

   public String getComment()
   {
      return comment;
   }

}
