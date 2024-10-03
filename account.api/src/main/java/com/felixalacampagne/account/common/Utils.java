package com.felixalacampagne.account.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.felixalacampagne.account.persistence.entities.StandingOrders;
import com.felixalacampagne.account.persistence.entities.Transaction;

public class Utils
{

   // yyyy-MM-dd is the iso date format supported by Javascript Date
   public static final DateTimeFormatter DATEFORMAT_YYYYMMDD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

   public static BigDecimal getZeroOrValue(BigDecimal value)
   {
      return value==null ? BigDecimal.ZERO : value;
   }

   // Combines credit and debit into a single amount, +ve for credit, -ve for debit
   public static BigDecimal getAmount(Transaction txn)
   {
      return getZeroOrValue(txn.getCredit()).subtract(getZeroOrValue(txn.getDebit()));
   }

   // Like equals but true if both are null
   public static boolean areSame(BigDecimal one, BigDecimal two)
   {
      if((one == null) && (two == null))
         return true;
      else if(((one == null) && (two != null))
             || ((one != null) && (two == null)))
         return false;
      return one.compareTo(two) == 0;
   }


   public static String getToken(Transaction transaction)
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

   public static String getToken(StandingOrders standingOrder)
   {
      // Crude value intended to confirm the record being updated is the correct one,
      // eg. to avoid a wrong/spoofed ID from being sent from the client
      return formatDate(standingOrder.getSOEntryDate())
         + ":" + formatDate(standingOrder.getSONextPayDate())
         + ":" + formatAmount(standingOrder.getSOAmount())
         + ":" + standingOrder.getSOPeriod()
         + ":" + standingOrder.getSOCount()
         + ":" + standingOrder.getSOTfrType()
         + ":" + standingOrder.getSOid()
         + ":" + standingOrder.getAccount().getAccId()
         ;
   }


   public static String formatAmount(BigDecimal bigdec)
   {
   String amt = "";
      if(bigdec != null)
      {
         amt = bigdec.setScale(2, RoundingMode.HALF_UP).toString();
      }
      return amt;
   }

   public static String formatDate(LocalDate ts)
   {
      String date = "";
      if(ts != null)
      {
         date = ts.format(DATEFORMAT_YYYYMMDD);
      }
      return date;
   }


   private Utils()
   {
      // utility class should only contain statics
   }
}
