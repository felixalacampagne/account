package com.felixalacampagne.account.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.PrintWriter;
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

   @Test
   void rotateFilesTest() throws Exception
   {
      for(int i = 0; i < 6; i++)
      {
         File f = new File("rotatetest_" + String.format("%02d", i) +  ".txt");
         f.delete();
      }

      File f = new File("rotatetest_00.txt");
      PrintWriter out = new PrintWriter(f);
      out.println("First Dummy test for file rotate test");
      out.close();

      assertTrue(f.exists(), "File should exist: " + f.getAbsolutePath());
      Utils.rotateFiles(f.getAbsolutePath(), 3);
      assertFalse(f.exists(), "File should NOT exist: " + f.getAbsolutePath());

      f = new File("rotatetest_01.txt");
      assertTrue(f.exists(), "File should exist: " + f.getAbsolutePath());

      f = new File("rotatetest_00.txt");
      out = new PrintWriter(f);
      out.println("Second Dummy test for file rotate test");
      out.close();

      assertTrue(f.exists(), "File should exist: " + f.getAbsolutePath());
      Utils.rotateFiles(f.getAbsolutePath(), 3);
      assertFalse(f.exists(), "File should NOT exist: " + f.getAbsolutePath());

      f = new File("rotatetest_01.txt");
      assertTrue(f.exists(), "File should exist: " + f.getAbsolutePath());
      f = new File("rotatetest_02.txt");
      assertTrue(f.exists(), "File should exist: " + f.getAbsolutePath());

      f = new File("rotatetest_00.txt");
      out = new PrintWriter(f);
      out.println("Third Dummy test for file rotate test");
      out.close();

      assertTrue(f.exists(), "File should exist: " + f.getAbsolutePath());
      Utils.rotateFiles(f.getAbsolutePath(), 3);
      assertFalse(f.exists(), "File should NOT exist: " + f.getAbsolutePath());

      f = new File("rotatetest_01.txt");
      assertTrue(f.exists(), "File should exist: " + f.getAbsolutePath());
      f = new File("rotatetest_02.txt");
      assertTrue(f.exists(), "File should exist: " + f.getAbsolutePath());


      f = new File("rotatetest_00.txt");
      out = new PrintWriter(f);
      out.println("Fourth Dummy test for file rotate test");
      out.close();

      assertTrue(f.exists(), "File should exist: " + f.getAbsolutePath());
      Utils.rotateFiles(f.getAbsolutePath(), 3);
      assertFalse(f.exists(), "File should NOT exist: " + f.getAbsolutePath());

      f = new File("rotatetest_01.txt");
      assertTrue(f.exists(), "File should exist: " + f.getAbsolutePath());
      f = new File("rotatetest_02.txt");
      assertTrue(f.exists(), "File should exist: " + f.getAbsolutePath());
      f = new File("rotatetest_03.txt");
      assertTrue(f.exists(), "File should exist: " + f.getAbsolutePath());


      f = new File("rotatetest_00.txt");
      out = new PrintWriter(f);
      out.println("Fifth Dummy test for file rotate test");
      out.close();

      assertTrue(f.exists(), "File should exist: " + f.getAbsolutePath());
      Utils.rotateFiles(f.getAbsolutePath(), 3);
      assertFalse(f.exists(), "File should NOT exist: " + f.getAbsolutePath());

      f = new File("rotatetest_01.txt");
      assertTrue(f.exists(), "File should exist: " + f.getAbsolutePath());
      f = new File("rotatetest_02.txt");
      assertTrue(f.exists(), "File should exist: " + f.getAbsolutePath());
      f = new File("rotatetest_03.txt");
      assertTrue(f.exists(), "File should exist: " + f.getAbsolutePath());
      f = new File("rotatetest_04.txt");
      assertFalse(f.exists(), "File NOT should exist: " + f.getAbsolutePath());

   }

}
