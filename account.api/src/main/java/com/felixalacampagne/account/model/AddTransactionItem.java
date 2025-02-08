package com.felixalacampagne.account.model;

import java.time.LocalDate;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddTransactionItem extends TransactionItem
{
   // Optional requires
   // <groupId>com.fasterxml.jackson.datatype</groupId>
   // <artifactId>jackson-datatype-jdk8</artifactId>
   // Is it already part of Spring?
//   @JsonDeserialize(using = OptionalDeserializer.class)
   private Optional<Long> transferAccount = Optional.empty(); // Changed: must now be a PhoneAccount id instead of Account id

   // This is a bit of a mess but I want to be able to enter a transfer account even if it is not
   // defined in PhoneAccounts because there is currently no way to define PhoneAccounts.
   private String communication;     // Only used if transferAccount or cptyAccountNumber are present
   private String cptyAccount;       // Only used if transferAccount is missing and communication and cptyAccountNumber are present
   private String cptyAccountNumber; // Only used if transferAccount is missing and communication and cptyAccount are present

   public AddTransactionItem()
   {
      // Needed for conversion from JSON?
   }


   public AddTransactionItem(Long accid, LocalDate date, String amount, String type, String comment,
         Optional<Long> transferAccount)
   {
      super(accid, date, amount, amount, type, comment, false, -1L, "", "", "");
      this.transferAccount = transferAccount;
   }

   public AddTransactionItem(Long accid, LocalDate date, String amount, String type, String comment,
         Optional<Long> transferAccount, String communication, String cptyAccount, String cptyAccountNumber)
   {
      super(accid, date, amount, amount, type, comment, false, -1L, "", "", "");
      this.transferAccount = transferAccount;
      this.communication = communication;
      this.cptyAccount = cptyAccount;
      this.cptyAccountNumber = cptyAccountNumber;
   }

   public Optional<Long> getTransferAccount()
   {
      return transferAccount;
   }

   public String getCommunication()
   {
      return communication;
   }
//
//   public void setCommunication(String communication)
//   {
//      this.communication = communication;
//   }

   public String getCptyAccount()
   {
      return cptyAccount;
   }

   public void setCptyAccount(String cptyAccount)
   {
      this.cptyAccount = cptyAccount;
   }

   public String getCptyAccountNumber()
   {
      return cptyAccountNumber;
   }

   public void setCptyAccountNumber(String cptyAccountNumber)
   {
      this.cptyAccountNumber = cptyAccountNumber;
   }

}
