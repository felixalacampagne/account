package com.felixalacampagne.account.standingorder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandingOrderUtilsTest
{
   Logger log = LoggerFactory.getLogger(this.getClass());
   StandingOrderUtils soUtils = new StandingOrderUtils();
   
   @Test
   void lastDayOfMonth()
   {
      LocalDate origEOM;
      LocalDate nextEOM;
      String memotmpl;
      String memo;
      
      memotmpl = "#dd mm yyyy#";

      // 31/05 -> 30/06
      origEOM = LocalDate.of(2025, 05, 31);
      nextEOM = origEOM.plus(1, ChronoUnit.MONTHS);
      memo = soUtils.expandSOmemo(memotmpl, nextEOM, nextEOM);
      log.info("lastDayOfMonth: orig:{} next:{}", soUtils.expandSOmemo(memotmpl, origEOM, origEOM), memo);
      assertEquals("30 06 2025", memo);
      
      // 30/06 -> 30/07 (lastDOM=31/07) can't use 32/06 or 31/06 - must be valid DOM value
      origEOM = LocalDate.of(2025, 02, 28);
      nextEOM = soUtils.adjustMonthAtEOM(origEOM, 1);

      memo = soUtils.expandSOmemo(memotmpl, nextEOM, nextEOM);
      log.info("lastDayOfMonth: orig:{} next:{}", soUtils.expandSOmemo(memotmpl, origEOM, origEOM), memo);
      assertEquals("31 03 2025", memo);
   
      origEOM = LocalDate.of(2025, 01, 31);
      nextEOM = soUtils.adjustMonthAtEOM(origEOM, 3);

      memo = soUtils.expandSOmemo(memotmpl, nextEOM, nextEOM);
      log.info("lastDayOfMonth: orig:{} next:{}", soUtils.expandSOmemo(memotmpl, origEOM, origEOM), memo);
      assertEquals("30 04 2025", memo);


      // Dates are always adjusted to the EOM by adjustMonthAtEOM
      origEOM = LocalDate.of(2025, 01, 27);
      nextEOM = soUtils.adjustMonthAtEOM(origEOM, 3);

      memo = soUtils.expandSOmemo(memotmpl, nextEOM, nextEOM);
      log.info("lastDayOfMonth: orig:{} next:{}", soUtils.expandSOmemo(memotmpl, origEOM, origEOM), memo);
      assertEquals("30 04 2025", memo);

   
   }
}
