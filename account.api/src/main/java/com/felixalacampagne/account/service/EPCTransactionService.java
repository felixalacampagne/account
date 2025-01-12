package com.felixalacampagne.account.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.felixalacampagne.account.model.EPCTransaction;

@Service
public class EPCTransactionService
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());   
   public String makeEPCFromTransaction(EPCTransaction txn)
   {
      // Name of recipient, account, amount, communication
      String commst = "";
      String commff = txn.getCommunication();
      String normed = commff.replaceAll("[\\s+/\\\\]", "");
      Matcher match = Pattern.compile("^(\\d{10,10})(\\d{2,2})$").matcher(normed);
      if(match.find())
      {
         // According to 'febelfin.be' the structured communication is called 'OGM-VCS' and
         // is a 10digit number plus 2digit modulo 97 of the number (97 if modulo is zero) making
         // 12 digits in all. The "+++ / / +++"decoration should only be used for display - the bare number is
         // used in electronic communications. So I think just providing the number should be enough
         // maybe checking the modulo and putting it on the reference line will help.
         long nreference = 0;
         String reference = match.group(1);
         String modulo = match.group(2);

         nreference += Long.valueOf(reference);

         nreference = nreference % 97;
         nreference = (nreference == 0) ? 97 : nreference;
         if(nreference == Long.valueOf( modulo))
         {
            commst = String.format("%s%s", reference, modulo);
            commff = "";
         }
      }

      // Name of recipient, account, amount, struct comm, freeformat comm
      //String epctmpl = "BCD\n002\n1\nSCT\n\n%s\n%s\nEUR%s\n\n%s\n%s\nBeneToOrigIgnored\n";
      String epctmpl = "BCD\n002\n1\nSCT\n\n%s\n%s\nEUR%s\n\n%s\n%s\n\n";

      String epc = String.format(epctmpl, txn.getName(), txn.getIban(), txn.getAmount(), commst, commff);
      return epc;
   }

}
