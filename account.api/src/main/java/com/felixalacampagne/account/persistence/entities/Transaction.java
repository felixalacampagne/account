package com.felixalacampagne.account.persistence.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


/**
 * The persistent class for the "transaction" database table.
 *
 */
@Entity
@Table(name="Transaction")
//@NamedQueries({
//@NamedQuery(name="Transaction.findAll", query="SELECT t FROM Transaction t"),
//@NamedQuery(name=Transaction.TRANSACTION_ALLFORACCOUNT, query="SELECT t FROM Transaction t where t.accountId=:accId order by t.date desc")
//})
public class Transaction implements Serializable
{
//   public static final String TRANSACTION_ALLFORACCOUNT="Transaction.findByAccountId";
   private static final long serialVersionUID = 1L;

   @Id // Mandatory

   // .AUTO gives UcanaccessSQLException: UCAExc:::5.0.1 user lacks privilege or object not found: HIBERNATE_SEQUENCE
   // TABLE same as AUTO
   // SEQUENCE same as AUTO
   // IDENTITY gives weird error: class org.hsqldb.jdbc.JDBCStatement cannot be cast to class java.sql.PreparedStatement
   //
   @GeneratedValue(strategy=GenerationType.IDENTITY)
   @Column(name="sequence")
   private long sequence;

   // Spring JPA seems to be putting underscores into the column names: needs a setting in application.properties to stop the underscores
   @Column(name="AccountId")
   private Long accountId;

   @Column(name="Balance")
   private BigDecimal balance;

   @Column(name="Checked")
   private boolean checked;

   @Column(name="CheckedBalance")
   private BigDecimal checkedBalance;

   @Column(name="Comment")
   private String comment;

   @Column(name="Credit")
   private BigDecimal credit;

   @Column(name="Date")
   private LocalDate date;

   @Column(name="Debit")
   private BigDecimal debit;

   @Column(name="SortedBalance")
   private BigDecimal sortedBalance;

   @Column(name="Stid")
   private String stid;

   @Column(name="Type")
   private String type;

   public Transaction() {
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.getClass().getName());
      sb.append(" sequence:").append(sequence);
      sb.append(" accountId:").append(accountId);
      sb.append(" balance:").append(balance);
      sb.append(" checked:").append(checked);
      sb.append(" checkedBalance:").append(checkedBalance);
      sb.append(" comment:").append(comment);
      sb.append(" credit:").append(credit);
      sb.append(" date:").append(date);
      sb.append(" debit:").append(debit);
      sb.append(" sortedBalance:").append(sortedBalance);
      sb.append(" stid:").append(stid);
      sb.append(" type:").append(type);
      return sb.toString();
    }

   public long getSequence() {
      return this.sequence;
   }

   public void setSequence(long sequence) {
      this.sequence = sequence;
   }

   public Long getAccountId() {
      return this.accountId;
   }

   public void setAccountId(Long accountId) {
      this.accountId = accountId;
   }

   public BigDecimal getBalance() {
      return this.balance;
   }

   public void setBalance(BigDecimal balance) {
      this.balance = balance;
   }

   public boolean getChecked() {
      return this.checked;
   }

   public void setChecked(boolean checked) {
      this.checked = checked;
   }

   public BigDecimal getCheckedBalance() {
      return this.checkedBalance;
   }

   public void setCheckedBalance(BigDecimal checkedBalance) {
      this.checkedBalance = checkedBalance;
   }

   public String getComment() {
      return this.comment;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }

   public BigDecimal getCredit() {
      return this.credit;
   }

   public void setCredit(BigDecimal credit) {
      this.credit = credit;
   }

   public LocalDate getDate() {
      return this.date;
   }

   public void setDate(LocalDate date) {
      this.date = date;
   }

   public BigDecimal getDebit() {
      return this.debit;
   }

   public void setDebit(BigDecimal debit) {
      this.debit = debit;
   }

   public BigDecimal getSortedBalance() {
      return this.sortedBalance;
   }

   public void setSortedBalance(BigDecimal sortedBalance) {
      this.sortedBalance = sortedBalance;
   }

   public String getStid() {
      return this.stid;
   }

   public void setStid(String stid) {
      this.stid = stid;
   }

   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type;
   }

}
