package com.felixalacampagne.account.persistence.entities;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;


/**
 * The persistent class for the "PhoneTrans" database table.
 *
 * To map Access CSV to H2 CSV columns
 * PTid;PTPayDate;PTsrcpaid           ;PTdstpaid              ;PTamount;PTcomm       ;PTaccom;PTSentDate;PTTransDate    ;PTErrStatus
 * id  ;paydate  ;senderphoneaccountid;recipientphoneaccountid;amount  ;communication;comment;sentdate  ;transactiondate;errorstatus
 *
 */
@Entity
@Table(name="phonetransaction")
public class PhoneTrans implements Serializable {
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue(strategy=GenerationType.IDENTITY)
   @Column(name="id", nullable=false)          // PTid
   private Long PTid;

   @Column(name="comment")                     // PTaccom
   private String PTaccom;

   @Column(name="amount")                      // PTamount
   private String PTamount;

   @Column(name="communication")               // PTcomm
   private String PTcomm;

   @Column(name="recipientphoneaccountid")     // PTdstpaid
   private Long PTdstpaid;

   @Column(name="senderphoneaccountid")        // PTsrcpaid
   private Long PTsrcpaid;

   @Column(name="paydate")                     // PTPayDate
   private LocalDate PTPayDate;

   @Column(name="sentdate")                    // PTSentDate
   private LocalDate PTSentDate;

   @Column(name="transactiondate")             // PTTransDate
   private LocalDate PTTransDate;

   @Column(name="errorstatus")                 // PTErrStatus
   private String PTErrStatus;

   public PhoneTrans() {
   }



   public Long getPTid() {
      return this.PTid;
   }

   public void setPTid(Long PTid) {
      this.PTid = PTid;
   }


   public String getPTaccom() {
      return this.PTaccom;
   }

   public void setPTaccom(String PTaccom) {
      this.PTaccom = PTaccom;
   }



   public String getPTamount() {
      return this.PTamount;
   }

   public void setPTamount(String PTamount) {
      this.PTamount = PTamount;
   }



   public String getPTcomm() {
      return this.PTcomm;
   }

   public void setPTcomm(String PTcomm) {
      this.PTcomm = PTcomm;
   }



   public Long getPTdstpaid() {
      return this.PTdstpaid;
   }

   public void setPTdstpaid(Long PTdstpaid) {
      this.PTdstpaid = PTdstpaid;
   }



   public String getPTErrStatus() {
      return this.PTErrStatus;
   }

   public void setPTErrStatus(String PTErrStatus) {
      this.PTErrStatus = PTErrStatus;
   }



   public LocalDate getPTPayDate() {
      return this.PTPayDate;
   }

   public void setPTPayDate(LocalDate PTPayDate) {
      this.PTPayDate = PTPayDate;
   }



   public LocalDate getPTSentDate() {
      return this.PTSentDate;
   }

   public void setPTSentDate(LocalDate PTSentDate) {
      this.PTSentDate = PTSentDate;
   }



   public Long getPTsrcpaid() {
      return this.PTsrcpaid;
   }

   public void setPTsrcpaid(Long PTsrcpaid) {
      this.PTsrcpaid = PTsrcpaid;
   }



   public LocalDate getPTTransDate() {
      return this.PTTransDate;
   }

   public void setPTTransDate(LocalDate PTTransDate) {
      this.PTTransDate = PTTransDate;
   }

}
