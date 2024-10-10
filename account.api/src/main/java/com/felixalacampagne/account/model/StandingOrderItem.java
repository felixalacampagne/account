package com.felixalacampagne.account.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

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

   private final String soperiod;
   private final String sotfrtype;
   private final String token;
   private final Long   accountid;
   private final String accountname;

   public StandingOrderItem(Long sOid, String sOAmount, Long sOCount, String sODesc,
         LocalDate sOEntryDate, LocalDate sONextPayDate, String sOPeriod, String sOTfrType,
         String token, Long accountid, String accountname)
   {
      this.soid = sOid;
      this.soamount = sOAmount;
      this.socount = sOCount;
      this.sodesc = sODesc;
      this.soentrydate = sOEntryDate;
      this.sonextpaydate = sONextPayDate;
      this.soperiod = sOPeriod;
      this.sotfrtype = sOTfrType;
      this.token = token;
      this.accountid = accountid;
      this.accountname = accountname;
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

   public String getSOPeriod()
   {
      return soperiod;
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

   @Override
   public String toString()
   {
      return "StandingOrderItem [SOid=" + soid + ", SOAmount=" + soamount + ", SOCount=" + socount + ", SODesc=" + sodesc + ", SOEntryDate=" + soentrydate + ", SONextPayDate=" + sonextpaydate + ", SOPeriod=" + soperiod + ", SOTfrType="
            + sotfrtype + ", token=" + token + ", accountid=" + accountid + ", accountname=" + accountname + "]";
   }

}
