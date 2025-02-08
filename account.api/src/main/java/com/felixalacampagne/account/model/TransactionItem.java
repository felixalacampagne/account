package com.felixalacampagne.account.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionItem
{
   private  long accid;
   private  String comment;

   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
   private  LocalDate date;
   private  String amount;
   
   // Kludge for amounts formatted with the account format string on server because the existing
   // format string are compatible with Java decimal formatter. The formatted
   // amounts cannot be used for updates as the formatted string is unparsable
   // so UI must know to use the 'normal', numeric, amount value for updates.
   // If I find a way to format in Javascript using the existing format strings
   // then the formatting can be moved to the UI and only the numeric value needs to be provided.
   private String amountfmtd; 

   private  String type;
   private  long id;
   private  boolean locked;
   private  String balance;  // This is formatted with the account format string. 
   private String statementref;


   private  String token;

   public TransactionItem()
   {
   }
   
   public TransactionItem(Long accid, LocalDate date, String amount, String amountfmtd, String type, String comment,
                          boolean locked, long id, String token, String balance, String statementref)
   {
      this.accid = accid;
      this.date = date;
      this.amount = amount;
      this.amountfmtd = amountfmtd;
      this.type = type;
      this.comment = comment;
      this.id = id;
      this.locked = locked;
      this.balance = balance;
      this.statementref = statementref;
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
            + ", amountfmtd=" + amountfmtd
            + ", type=" + type
            + ", id=" + id
            + ", locked=" + locked
            + ", statementref=" + statementref
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

   public String getStatementref()
   {
      return statementref;
   }

   public String getToken()
   {
      return token;
   }
   public String getAmountfmtd()
   {
      return amountfmtd;
   }

}
