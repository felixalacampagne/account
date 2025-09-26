package com.felixalacampagne.account.persistence.h2;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;


// Manual test to modify the CSV output from UCanAccess for populating the empty H2 database
public class CSVHeaderFixTest
{
   Logger log = LoggerFactory.getLogger(this.getClass());

String accountAccessColumns = "acc_id;acc_code;acc_desc   ;acc_addr;acc_tel;acc_curr;acc_fmt       ;acc_sid     ;acc_order;acc_swiftbic";
String accountH2Columns =     "id    ;code    ;description;address ;contact;currency;currencyformat;statementref;ranking  ;swiftbic";
String csvpath = "csv";
// Aaaaagggghhhhhh: no Pair class in Java. It is in javafx which is not part of standard JRE
// Thus forced to use the spring pair, which is already available so no extra dependencies required.
Map<String, Pair> columnMaps = new HashMap<>();
{
   columnMaps.put("account", Pair.of(accountAccessColumns, accountH2Columns));
   columnMaps.put("transaction", Pair.of(
         "sequence;AccountId;Date           ;Type           ;Comment;Checked;Credit;Debit;Balance;CheckedBalance;SortedBalance;Stid",
         "id      ;accountid;transactiondate;transactiontype;comment;checked;credit;debit;balance;checkedbalance;sortedbalance;statementref"
         ));
   columnMaps.put("transaction", Pair.of(
         "sequence;AccountId;Date           ;Type           ;Comment;Checked;Credit;Debit;Balance;CheckedBalance;SortedBalance;Stid",
         "id      ;accountid;transactiondate;transactiontype;comment;checked;credit;debit;balance;checkedbalance;sortedbalance;statementref"
         ));
   columnMaps.put("phoneaccount", Pair.of(
         "PAid;PAtype     ;PAaccid  ;PAnumber;PAdesc ;PAorder;PALastComm   ;PAAccessCode;PAmaster;PASWIFTBIC",
         "id  ;accounttype;accountid;code    ;comment;ranking;communication;PAAccessCode;PAmaster;PASWIFTBIC"
         ));
   columnMaps.put("phonetransaction", Pair.of(
         "PTid;PTPayDate;PTsrcpaid           ;PTdstpaid              ;PTamount;PTcomm       ;PTaccom;PTSentDate;PTTransDate    ;PTErrStatus",
         "id  ;paydate  ;senderphoneaccountid;recipientphoneaccountid;amount  ;communication;comment;sentdate  ;transactiondate;errorstatus"
         ));
   columnMaps.put("standingorders", Pair.of(
         "SOid;SOPeriod;SOCount;SONextPayDate;SOEntryDate;SODesc ;SOAccId  ;SOTfrType      ;SOAmount",
         "id  ;period  ;count  ;paydate      ;entrydate  ;comment;accountid;transactiontype;amount"
         ));
   columnMaps.put("prefs", Pair.of(
         "prefs_name;prefs_text;prefs_numeric",
         "name;text;numeric"
         ));
}


   @Test
   void createH2csvs()
   {
      columnMaps.forEach((t, p) -> {
         createH2csv(t, (String) p.getFirst(), (String) p.getSecond());
      });

   }

   void createH2csv(String tablename, String accessCols, String h2cols)
   {
      String[] acolnames = accessCols.split(";");
      String[] hcolnames = h2cols.split(";");
      Map<String, String> columnmap = new HashMap<>();
      for(int i = 0; i < acolnames.length; i++)
      {
         columnmap.put(acolnames[i].strip(), hcolnames[i].strip());
      }

      File accfile = new File(csvpath, tablename + ".csv");
      File h2file = new  File(csvpath, tablename + "_h2cols.csv");
      int linecount=0;

      // try-with-resources: Scanner will be closed automatically
      try (PrintWriter out = new PrintWriter(h2file))
      {
         try (Scanner myReader = new Scanner(accfile))
         {
           while (myReader.hasNextLine())
           {
             String data = myReader.nextLine();
             linecount++;
             if(linecount == 1)
             {
                String [] head = data.split(";");
                String h2head = "";
                for(int i=0 ; i < head.length; i++)
                {
                   String h2col = columnmap.get(head[i]);
                   h2head += h2col + ";";
                }
                h2head = h2head.substring(0, h2head.length()-1);
                out.println(h2head);
             }
             else
             {
                out.println(fixLine(tablename, data));
             }
           }
         }
         catch (Exception ex)
         {
           log.error("createH2csv: error processing {}", accfile.getAbsolutePath(), ex);
         }
      }
      catch (Exception ex)
      {
         log.error("createH2csv: error processing {}", accfile.getAbsolutePath(), ex);
      }
    }


    String fixLine(String tablename, String line)
    {
       String fixline = line;
       if("transaction".equals(tablename))
       {
          fixline = line.replaceAll("^(\\d{1,6});;", "$1;25;");
          fixline = fixline.replaceAll("^(\\d{1,6});\\(null\\);", "$1;25;");
          // maybe needs this also: ^\d{1,5};\(null\);
       }
       return fixline;
    }

}
