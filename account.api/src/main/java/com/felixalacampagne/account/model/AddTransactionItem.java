package com.felixalacampagne.account.model;

import java.time.LocalDate;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddTransactionItem extends TransactionItem
{
   // Optional requires 
   // <groupId>com.fasterxml.jackson.datatype</groupId>
   // <artifactId>jackson-datatype-jdk8</artifactId>
   // Is it already part of Spring?
//   @JsonDeserialize(using = OptionalDeserializer.class)
   private Optional<Long> transferAccount = Optional.empty();
   
// private boolean transfer;    // Could make transferAccount optional but not sure how that is handled with Json
//   public boolean isTransfer()
//   {
//      return transfer;
//   }

   public Optional<Long> getTransferAccount()
   {
      return transferAccount;
   }
  
   public AddTransactionItem()
   {
      // Needed for conversion from JSON?
   }

   public AddTransactionItem(Long accid, LocalDate date, String amount, String type, String comment, Optional<Long> transferAccount)
   {
      super(accid, date, amount, type, comment, false, -1L, "", "", "");
      this.transferAccount = transferAccount;
   }

}
