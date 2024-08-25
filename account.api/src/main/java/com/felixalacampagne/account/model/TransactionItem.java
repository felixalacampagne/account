package com.felixalacampagne.account.model;

public class TransactionItem
{
   private final long accid;
   private final String comment;
   private final String date;
   private final String amount;
   private final String type;

   public TransactionItem(Long accid, String date, String amount, String type, String comment)
   {
      this.accid = accid;
      this.date = date;
      this.amount = amount;
      this.type = type;
      this.comment = comment;
   }


   public String getDate()
   {
      return date;
   }

   public String getAmount()
   {
      return amount;
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
      return "TransactionItem [accid=" + accid + ", comment=" + comment + ", date=" + date + ", amount=" + amount + ", type=" + type + "]";
   }

}
