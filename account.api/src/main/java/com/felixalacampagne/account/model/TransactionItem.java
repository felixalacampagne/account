package com.felixalacampagne.account.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class TransactionItem
{
   private  long accid;
   private  String comment;

   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
   private  LocalDate date;
   private  String amount;
   private  String type;
   private  long id;
   private  boolean locked;
   private  String balance;
   private  String token;

   public TransactionItem()
   {
   }
   public TransactionItem(Long accid, LocalDate date, String amount, String type, String comment,
                          boolean locked, long id, String token, String balance)
   {
      this.accid = accid;
      this.date = date;
      this.amount = amount;
      this.type = type;
      this.comment = comment;
      this.id = id;
      this.locked = locked;
		this.balance = balance;
      this.token = token; // Maybe create this here?
   }


   public LocalDate getDate()
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

   public long getAccid()
   {
      return accid;
   }


   public String getType()
   {
      return type;
   }


   public String getComment()
   {
      return comment;
   }


   @Override
   public String toString()
   {
      return "TransactionItem [accid=" + accid
            + ", comment=" + comment
            + ", date=" + date
            + ", amount=" + amount
            + ", type=" + type
            + ", id=" + id
            + ", locked=" + locked
            + ", balance=" + balance
            + ", token=" + token
            + "]";
   }


   public long getId()
   {
      return id;
   }


   public boolean isLocked()
   {
      return locked;
   }


   public String getToken()
   {
      return token;
   }

}
