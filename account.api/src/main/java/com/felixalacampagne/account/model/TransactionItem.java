package com.felixalacampagne.account.model;

public class TransactionItem
{
   private final long accid;
   private final String comment;
   private final String date;
   private final String amount;
   private final String type;
   private final long id;
   private final boolean locked;
   private final String balance;
   private final String token;

   public TransactionItem(Long accid, String date, String amount, String type, String comment,
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
