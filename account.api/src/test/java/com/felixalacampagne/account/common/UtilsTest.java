package com.felixalacampagne.account.common;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UtilsTest
{
   private final Logger log = LoggerFactory.getLogger(UtilsTest.class);
   
   @Test
   void testFormatAmountBigDecimalString()
   {
      BigDecimal amt = new BigDecimal(1234567.9876);
      amt = amt.setScale(4, RoundingMode.HALF_EVEN);
      String fmt;
      
      fmt = "€#,##0.00";
      log.info("testFormatAmountBigDecimalString: amt:{} fmt:{}: {}", amt, fmt, Utils.formatAmount(amt, fmt));
      fmt = "£#,##0.00";
      log.info("testFormatAmountBigDecimalString: amt:{} fmt:{}: {}", amt, fmt, Utils.formatAmount(amt, fmt));

      amt = amt.negate();
      fmt = "€#,##0.00";
      log.info("testFormatAmountBigDecimalString: amt:{} fmt:{}: {}", amt, fmt, Utils.formatAmount(amt, fmt));
      fmt = "£#,##0.00";
      log.info("testFormatAmountBigDecimalString: amt:{} fmt:{}: {}", amt, fmt, Utils.formatAmount(amt, fmt));
   
      fmt = "€#,##0.00";

      amt = new BigDecimal(0.9876).setScale(4, RoundingMode.HALF_EVEN);
      log.info("testFormatAmountBigDecimalString: amt:{} fmt:{}: {}", amt, fmt, Utils.formatAmount(amt, fmt));

      amt = amt.negate();
      log.info("testFormatAmountBigDecimalString: amt:{} fmt:{}: {}", amt, fmt, Utils.formatAmount(amt, fmt));
      
   }

}
