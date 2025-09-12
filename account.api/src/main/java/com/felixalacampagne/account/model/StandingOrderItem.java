package com.felixalacampagne.account.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

// JSON friendly form of StandingOrders entity
public class StandingOrderItem
{
   private final Long soid;
   private final String soamount;
   private final Long socount;
   private final String sodesc;

   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
   private final LocalDate soentrydate;

   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
   private final LocalDate sonextpaydate;

   private final String period;
   private final String sotfrtype;
   private final String token;
   private final Long   accountid;
   private final String accountname;

   private final boolean entryeom;

   private final boolean payeom;
   
   public StandingOrderItem(Long sOid, String sOAmount, Long sOCount, String sODesc,
         LocalDate sOEntryDate, LocalDate sONextPayDate, String sOPeriod, String sOTfrType,
         String token, Long accountid, String accountname)
   {
      String period;
      boolean entryeom = false;
      boolean payeom = false;
      switch(sOPeriod)
      {
      case "E":
         period = "M";
         entryeom = true;
         break;
      case "P":
         period = "M";
         payeom = true;
         break;
      case "B":
         period = "M";
         entryeom = true;
         payeom = true;
         break;
      default:
         period = sOPeriod;
         break;
      }
      this.soid = sOid;
      this.soamount = sOAmount;
      this.socount = sOCount;
      this.sodesc = sODesc;
      this.soentrydate = sOEntryDate;
      this.sonextpaydate = sONextPayDate;
      this.period = period;
      this.sotfrtype = sOTfrType;
      this.token = token;
      this.accountid = accountid;
      this.accountname = accountname;
      this.entryeom = entryeom;
      this.payeom = payeom;
   }

   public Long getSOId()
   {
      return soid;
   }

   public String getSOAmount()
   {
      return soamount;
   }

   public Long getSOCount()
   {
      return socount;
   }

   public String getSODesc()
   {
      return sodesc;
   }

   public LocalDate getSOEntrydate()
   {
      return soentrydate;
   }

   public LocalDate getSONextpaydate()
   {
      return sonextpaydate;
   }

   // recombines EOM and period to return the DB value of SOPeriod.
   // This wont work if JSON is created using the getter - maybe need to change the
   // field name to simply 'period', it is no longer the SOPeriod value so would make sense.
   @JsonIgnore
   public String getSOPeriod()
   {
      String dbperiod = this.period;
      if( "M".equals(dbperiod) )
      {
         // EOMs are only valid for Monthly periods
         if( this.entryeom && this.payeom )
         {
            dbperiod = "B";
         }
         else if( this.entryeom )
         {
            dbperiod = "E";
         }
         else if( this.payeom )
         {
            dbperiod = "P";
         }
      }
      return dbperiod;
   }

   public String getSOTfrtype()
   {
      return sotfrtype;
   }

   public String getToken()
   {
      return token;
   }

   public Long getAccountid()
   {
      return accountid;
   }

   public String getAccountname()
   {
      return accountname;
   }
   public String getPeriod()
   {
      return period;
   }

   public boolean isEntryeom()
   {
      return entryeom;
   }

   public boolean isPayeom()
   {
      return payeom;
   }

   @Override
   public String toString()
   {
      return "StandingOrderItem [SOid=" + soid + ", SOAmount=" + soamount + ", SOCount=" + socount + ", SODesc=" + sodesc 
            + ", SOEntryDate=" + soentrydate + ", SONextPayDate=" + sonextpaydate + ", SOPeriod=" + getSOPeriod() + ", SOTfrType="
            + sotfrtype + ", token=" + token + ", accountid=" + accountid + ", accountname=" + accountname 
            + "]";
   }

}
