package com.felixalacampagne.account.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felixalacampagne.account.model.TransactionItem;

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

   @Test
   void testJsonDateFormatting() throws Exception
   {
      ObjectMapper objmap = new ObjectMapper();
      String baseDatestr = "2027-06-10";
      String testDateStr;
      objmap.findAndRegisterModules();
      TransactionItem ti = new TransactionItem(99L,
            LocalDate.of(2027,06,10),
            "1.0", "1.0", "BC", "test", false, 999L, "none", "200.00", "N/A");

      String json = objmap.writeValueAsString(ti);
      log.info("testJsonDateFormatting: json:\n{}", json);
      assertTrue(json.contains('"' + baseDatestr + '"'), json);

      TransactionItem tifromj =  objmap.readValue(json, TransactionItem.class );
      log.info("testJsonDateFormatting: tifromj:\n{}", tifromj);
      assertEquals(ti.getDate(), tifromj.getDate(), baseDatestr);

      testDateStr = "2027-06-10T11:00:00.000Z";
      String jsonx = json.replace(baseDatestr, testDateStr);
      tifromj =  objmap.readValue(jsonx, TransactionItem.class );
      log.info("testJsonDateFormatting: {} => {}", testDateStr, tifromj.getDate());
      assertEquals(ti.getDate(), tifromj.getDate(), testDateStr);

      testDateStr = "2027-06-10T11:00:00CET";
      jsonx = json.replace(baseDatestr, testDateStr);
      tifromj =  objmap.readValue(jsonx, TransactionItem.class );
      log.info("testJsonDateFormatting: {} => {}", testDateStr, tifromj.getDate());
      assertEquals(ti.getDate(), tifromj.getDate(), testDateStr);

      testDateStr = "2027-06-10T11:00:00";
      jsonx = json.replace(baseDatestr, testDateStr);
      tifromj =  objmap.readValue(jsonx, TransactionItem.class );
      log.info("testJsonDateFormatting: {} => {}", testDateStr, tifromj.getDate());
      assertEquals(ti.getDate(), tifromj.getDate(),testDateStr);

      testDateStr = "2027-06-10T00:00";
      jsonx = json.replace(baseDatestr, testDateStr);
      tifromj =  objmap.readValue(jsonx, TransactionItem.class );
      log.info("testJsonDateFormatting: {} => {}", testDateStr, tifromj.getDate());
      assertEquals(ti.getDate(), tifromj.getDate(),testDateStr);

      testDateStr = "2027-06-10T00:00Z";
      jsonx = json.replace(baseDatestr, testDateStr);
      tifromj =  objmap.readValue(jsonx, TransactionItem.class );
      log.info("testJsonDateFormatting: {} => {}", testDateStr, tifromj.getDate());
      assertEquals(ti.getDate(), tifromj.getDate(),testDateStr);

   }
}
