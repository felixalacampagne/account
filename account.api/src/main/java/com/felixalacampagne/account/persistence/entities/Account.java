package com.felixalacampagne.account.persistence.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;


/**
 * The persistent class for the "account" database table.
 *
 */
@Entity
@Table(name="account")
public class Account implements Serializable {
   public static final String ACCOUNT_ALLACTIVE = "Account.findActive";
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue(strategy=GenerationType.IDENTITY)
   @Column(name="acc_id")
   private Long accId;

   @Column(name="acc_addr")
   private String accAddr;

   @Column(name="acc_code")
   private String accCode;

   @Column(name="acc_curr")
   private String accCurr;

   @Column(name="acc_desc")
   private String accDesc;

   @Column(name="acc_fmt")
   private String accFmt;

   @Column(name="acc_order")
   private Long accOrder;

   @Column(name="acc_sid")
   private String accSid;

   @Column(name="acc_swiftbic")
   private String accSwiftbic;

   @Column(name="acc_tel")
   private String accTel;

   public Account() {
   }
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.getClass().getName());
      sb.append(" accId:").append(accId);
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

   public Long getAccId() {
      return this.accId;
   }

   public void setAccId(Long accId) {
      this.accId = accId;
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


}
