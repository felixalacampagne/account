package com.felixalacampagne.account.persistence.entities;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;



/**
 * The persistent class for the "PhoneAccounts" database table.
 *
 */
@Entity
@Table(name="PhoneAccounts")
//@NamedQuery(name="PhoneAccount.findAll", query="SELECT p FROM PhoneAccount p")
public class PhoneAccount implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "PAid", insertable = false)
   private Long Id;

//   @Column(name = "PAAccessCode", nullable = true)
//   private String accessCode; // No longer relevant. I think this was the
//                              // password for the CBC phone banking

   @Column(name = "PAaccid", nullable = true)
   private Long accountId; // The related account in Account table, if any.
                           // Presence indicates transfer between account is
                           // required.

   @Column(name = "PAdesc")
   private String desc;

   @Column(name = "PALastComm")
   private String lastComm;

//   @Column(name = "PAmaster", nullable = true)
//   private Integer master; // No longer relevant, was used to indicate which
//                           // accounts worked with CBC phone banking

   @Column(name = "PAnumber")
   private String accountNumber;

   @Column(name = "PAorder")
   private Integer order; // Used to determine the order in the list of the
                          // entries, to put 'my' accounts on top

//   @Column(name = "PASWIFTBIC", nullable = true)
//   private String bic; // No longer required, was previously needed with IBAN
//                       // account numbers

   // C: Current account - a CBC Phone account that can make transfers to
   // non-CBC accounts
   // E: Savings account - CBC savings account which can only transfer to other
   // CBC accounts, eg. C accounts
   // S: Shadow account - a non-CBC Phone third party account which has a
   // 'shadow' in Account, only transfers to it were possible via
   // CBC Phone, transfers required updating both the source and destination
   // Accounts.
   // O: other account - third party accounts
   //
   // The distinction between C,E and S is now largely irrelevant as CBC Phone
   // is no longer used.
   // 'E' may be relevant since transfers from savings accounts are usually only
   // allowed into the related current account. There is
   // currently no way to know which current account is related to which savings
   // account.
   //
   // For the new version a list of suitable transfer accounts will be displayed,
   // with the order determined by the 'order' and 'desc' fields.
   // order=255 means no display - should just delete the entries since PhoneTransaction
   // will no longer be used.
   // The display list should not include the account from which the transfer is being made

   @Column(name = "PAtype")
   private String type;

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append(this.getClass().getSimpleName());
      sb.append(" Id:").append(Id);
      sb.append(" accountId:").append(accountId);
      sb.append(" type:").append(type);
      // sb.append(" master:").append(master);
      sb.append(" order:").append(order);
      sb.append(" desc:").append(desc);
      sb.append(" lastComm:").append(lastComm);
      sb.append(" accountNumber:").append(accountNumber);
      // sb.append(" accessCode:").append(accessCode);
      return sb.toString();
   }

   public PhoneAccount()
   {
   }

   public Long getId()
   {
      return this.Id;
   }

   public void setId(Long Id)
   {
      this.Id = Id;
   }

//   public String getAccessCode()
//   {
//      return this.accessCode;
//   }
//
//   public void setAccessCode(String accessCode)
//   {
//      this.accessCode = accessCode;
//   }

   public Long getAccountId()
   {
      return this.accountId;
   }

   public void setAccountId(Long accountId)
   {
      this.accountId = accountId;
   }

   public String getDesc()
   {
      return this.desc;
   }

   public void setDesc(String desc)
   {
      this.desc = desc;
   }

   public String getLastComm()
   {
      return this.lastComm == null ? "" : this.lastComm;
   }

   public void setLastComm(String lastComm)
   {
      this.lastComm = lastComm;
   }

//   public int getMaster()
//   {
//      return this.master == null ? 0 : this.master;
//   }
//
//   public void setMaster(int master)
//   {
//      this.master = master;
//   }

   public String getAccountNumber()
   {
      return this.accountNumber;
   }

   public void setAccountNumber(String accountNumber)
   {
      this.accountNumber = accountNumber;
   }

   public int getOrder()
   {
      return this.order == null ? 0 : this.order;
   }

   public void setOrder(int order)
   {
      this.order = order;
   }

//   public String getBic()
//   {
//      return this.bic;
//   }
//
//   public void setBic(String bic)
//   {
//      this.bic = bic;
//   }

   public String getType()
   {
      return this.type;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(Id, accountId, accountNumber, desc, lastComm, order, type);
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      PhoneAccount other = (PhoneAccount) obj;
      return Objects.equals(Id, other.Id) && Objects.equals(accountId, other.accountId) && Objects.equals(accountNumber, other.accountNumber) && Objects.equals(desc, other.desc) && Objects.equals(lastComm, other.lastComm)
            && Objects.equals(order, other.order) && Objects.equals(type, other.type);
   }

}
