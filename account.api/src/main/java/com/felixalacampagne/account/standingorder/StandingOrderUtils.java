package com.felixalacampagne.account.standingorder;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class StandingOrderUtils
{
   public String expandSOmemo(String memo, LocalDate txndate, LocalDate entdate)
   {
      int i;
      int e;
      String srtn = memo;
      String sl;
      String sr;
      String pat;
      LocalDate fmtdate;

      do
      {
         i = srtn.indexOf('#');
         if(i < 0)
         {
            break;
         }
         e = srtn.indexOf('#', i+1);
         if(e < 0)
         {
            break;
         }

         sl = "";
         sr = "";

         if(i > 0)
            sl = srtn.substring(0, i);

         if(e < srtn.length())
            sr = srtn.substring(e+1, srtn.length());

         pat = srtn.substring(i+1, e);

         fmtdate = txndate;
         if(pat.startsWith("E"))
         {
            pat = pat.substring(1, pat.length());
            if(entdate != null)
            {
               fmtdate = entdate;
            }
         }
         // pat should now be a VB date format - unfortunately this is not the same as a Java date format
         // There are only a few formats actually used and the main difference is for month, ie. m vs. M for java
         pat = pat.replace('m', 'M');
         DateTimeFormatter df = DateTimeFormatter.ofPattern(pat);

         srtn = sl + df.format(fmtdate) + sr;
      }while(true);

      return srtn;
   }

   public boolean isEndOfMonth(LocalDate d)
   {
      return (d.getDayOfMonth() == d.lengthOfMonth());
   }
   
   public LocalDate adjustMonthWithEOM(LocalDate orig, long offset)
   {
      LocalDate next = orig.plusMonths(offset);
      
      if(isEndOfMonth(orig))
      {
         // nextEOM = nextEOM.withDayOfMonth(nextEOM.lengthOfMonth());
         next = YearMonth.from(next).atEndOfMonth();
      }
      return next;
   }
   

   public LocalDate adjustDate(LocalDate origdate, TemporalUnit periodUnit, long numperiods)
   {
      LocalDate adjustdate = origdate;
      if(ChronoUnit.MONTHS.equals(periodUnit))
      {
         adjustdate = adjustMonthWithEOM(origdate, numperiods);
      }
      else
      {
         adjustdate = adjustdate.plus(numperiods, periodUnit);
      }
      return adjustdate; //Date.valueOf(adjustdate);
   }   
}
