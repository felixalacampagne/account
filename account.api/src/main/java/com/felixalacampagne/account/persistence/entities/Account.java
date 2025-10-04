package com.felixalacampagne.account.persistence.entities;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;


/**
 * The persistent class for the "account" database table.
 * To map Access CSV to H2 CSV columns
 * acc_id;acc_code;acc_desc   ;acc_addr;acc_tel;acc_curr;acc_fmt       ;acc_sid     ;acc_order;acc_swiftbic
 * id    ;code    ;description;address ;contact;currency;currencyformat;statementref;ranking  ;swiftbic
 */
@Entity
@Table(name="account")
public class Account implements Serializable {
   public static final String ACCOUNT_ALLACTIVE = "Account.findActive";
   private static final long serialVersionUID = 1L;

   // WARNING: columns names have been changed to more readable names, entity properties are the same as for account

   // Actually changing the column names is a bad idea if the idea is to import table data generated
   // by an export from the Access DB as the import data will have the wrong names. Probably not
   // hard to edit the import files - the sequence update needs to be added anyway.

   // Identity works fine for starting from an empty DB however the HS DB will need
   // to be populated with records where the ID is already set, and is referenced as a foreign key
   // in other tables. So far I have not found a way to force the 'IDENTITY' ids to start from a specific
   // value. A workaround might be to explicitly specify that the primary key is generated from a
   // sequence and start that sequence well above the values in data to be imported.
   // That doesn't work. The INSERTs in data.sql without the acc_id value cause
   // JdbcSQLIntegrityConstraintViolationException: NULL not allowed for column "ACC_ID".
   // Actually that probably doesn't matter since all of the data to be migrated will have an ID.
   // The issue arises when a new record is added after the import when the auto-id will need to be
   // greater than the max. value in the imported data.

   // Maybe could drop and recreate the sequence with the max id? This can be done in the data.sql


   // @GeneratedValue(strategy=GenerationType.IDENTITY)
   @Id
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_seq_gen")
   @SequenceGenerator(initialValue = 1, name = "account_seq_gen", sequenceName = "account_seq", allocationSize = 1)
   @Column(name="id")             // acc_id
   private Long id;

   @Column(name="address")        // acc_addr
   private String accAddr;

   @Column(name="code")           // acc_code
   private String accCode;

   @Column(name="currency")       // acc_curr
   private String accCurr;

   @Column(name="description")    // acc_desc
   private String accDesc;

   @Column(name="currencyformat") // acc_fmt
   private String accFmt;

   @Column(name="ranking")        // acc_order : 'order' can't be used as it's an sql keyword
   private Long accOrder;

   @Column(name="statementref")   // acc_sid
   private String accSid;

   @Column(name="swiftbic")       // acc_swiftbic
   private String accSwiftbic;

   @Column(name="contact")        // acc_tel : could be a URL or email
   private String accTel;

   public Account() {
   }
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.getClass().getName());
      sb.append(" accId:").append(id);
      sb.append(" accAddr:").append(accAddr);
      sb.append(" accCode:").append(accCode);
      sb.append(" accCurr:").append(accCurr);
      sb.append(" accDesc:").append(accDesc);
      sb.append(" accFmt:").append(accFmt);
      sb.append(" accOrder:").append(accOrder);
      sb.append(" accSid:").append(accSid);
      sb.append(" accSwiftbic:").append(accSwiftbic);
      sb.append(" accTel:").append(accTel);
      return sb.toString();
    }

   public Long getAccId() {  // for backward compatibility
      return this.id;
   }

   public void setAccId(Long accId) {  // for backward compatibility
      this.id = accId;
   }

   public Long getId() {
      return this.id;
   }

   public void setId(Long accId) {
      this.id = accId;
   }


   public String getAccAddr() {
      return this.accAddr;
   }

   public void setAccAddr(String accAddr) {
      this.accAddr = accAddr;
   }

   public String getAccCode() {
      return this.accCode;
   }

   public void setAccCode(String accCode) {
      this.accCode = accCode;
   }

   public String getAccCurr() {
      return this.accCurr;
   }

   public void setAccCurr(String accCurr) {
      this.accCurr = accCurr;
   }

   public String getAccDesc() {
      return this.accDesc;
   }

   public void setAccDesc(String accDesc) {
      this.accDesc = accDesc;
   }

   public String getAccFmt() {
      return this.accFmt;
   }

   public void setAccFmt(String accFmt) {
      this.accFmt = accFmt;
   }

   public Long getAccOrder() {
      return this.accOrder;
   }

   public void setAccOrder(Long accOrder) {
      this.accOrder = accOrder;
   }

   public String getAccSid() {
      return this.accSid;
   }

   public void setAccSid(String accSid) {
      this.accSid = accSid;
   }

   public String getAccSwiftbic() {
      return this.accSwiftbic;
   }

   public void setAccSwiftbic(String accSwiftbic) {
      this.accSwiftbic = accSwiftbic;
   }

   public String getAccTel() {
      return this.accTel;
   }

   public void setAccTel(String accTel) {
      this.accTel = accTel;
   }
   @Override
   public int hashCode()
   {
      return Objects.hash(accAddr, accCode, accCurr, accDesc, accFmt, id, accOrder, accSid, accSwiftbic, accTel);
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
      Account other = (Account) obj;
      return Objects.equals(accAddr, other.accAddr) && Objects.equals(accCode, other.accCode) && Objects.equals(accCurr, other.accCurr) && Objects.equals(accDesc, other.accDesc) && Objects.equals(accFmt, other.accFmt)
            && Objects.equals(id, other.id) && Objects.equals(accOrder, other.accOrder) && Objects.equals(accSid, other.accSid) && Objects.equals(accSwiftbic, other.accSwiftbic) && Objects.equals(accTel, other.accTel);
   }


}
