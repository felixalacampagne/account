package com.felixalacampagne.account.persistence.entities;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;



/**
 * The persistent class for the "PhoneAccounts" database table.
 *
 * To map Access CSV to H2 CSV columns
 * PAid;PAtype     ;PAaccid  ;PAnumber;PAdesc ;PAorder;PALastComm   ;PAAccessCode;PAmaster;PASWIFTBIC
 * id  ;accounttype;accountid;code    ;comment;ranking;communication;PAAccessCode;PAmaster;PASWIFTBIC
 *
 * NB. The last three access columns are not implemented for H2
 *
 */
@Entity
@Table(name="phoneaccount")
public class PhoneAccount implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue(strategy=GenerationType.IDENTITY)
   @Column(name="id", nullable=false)
   private Long Id;

   @ManyToOne
   @JoinColumn(name = "accountid", nullable = true)  // PAaccid
   private Account account; // The related account in Account table, if any.
                            // Presence indicates transfer between account is
                            // required.
   @Column(name = "comment")                       // PAdesc
   private String desc;

   @Column(name = "communication")                 // PALastComm
   private String lastComm;

   @Column(name = "code")                          // PAnumber
   private String accountNumber;

   @Column(name = "ranking")                   // PAorder
   private Integer order; // Used to determine the order in the list of the
                          // entries, to put 'my' accounts on top

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

   @Column(name = "accounttype")                     // PAtype
   private String type;

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append(this.getClass().getSimpleName());
      sb.append(" Id:").append(Id);
      sb.append(" accountId:").append(account==null ? "null": account.getId());
      sb.append(" type:").append(type);
      sb.append(" order:").append(order);
      sb.append(" desc:").append(desc);
      sb.append(" lastComm:").append(lastComm);
      sb.append(" accountNumber:").append(accountNumber);
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


   public Account getAccount()
   {
      return this.account;
   }

   public void setAccount(Account account)
   {
      this.account = account;
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
      return Objects.hash(Id, account, accountNumber, desc, lastComm, order, type);
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
      return Objects.equals(Id, other.Id) && Objects.equals(account, other.account) && Objects.equals(accountNumber, other.accountNumber) && Objects.equals(desc, other.desc) && Objects.equals(lastComm, other.lastComm)
            && Objects.equals(order, other.order) && Objects.equals(type, other.type);
   }

   // Convenience methods to access content of Account which may be null
   // null is replaced by values appropriate for the UI
   // Presence of getAccountId confuses the spring conversion of method names to queries so
   // xxxByAccountId is incorrectly interpreted as a field called accountId instead of account.id
   // Returns 0 for a null Account for compatibility with the UI
   public Long getAccId()
   {
      return account==null ? 0 : account.getId();
   }

   public String getAccDesc()
   {
      return account==null ? "" : account.getAccDesc();
   }

}
