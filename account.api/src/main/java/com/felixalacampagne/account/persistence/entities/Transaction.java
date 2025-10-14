package com.felixalacampagne.account.persistence.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
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
 * The persistent class for the "transaction" database table.
 *
 * To map Access CSV to H2 CSV columns
 * sequence;AccountId;Date           ;Type           ;Comment;Checked;Credit;Debit;Balance;CheckedBalance;SortedBalance;Stid
 * id     ;accountid ;transactiondate;transactiontype;comment;checked;credit;debit;balance;checkedbalance;sortedbalance;statementref
 */
@Entity
@Table(name="transaction")
public class Transaction implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue(strategy=GenerationType.IDENTITY)
   @Column(name="id", nullable=false)                // sequence
   private long sequence;

   @ManyToOne
   @JoinColumn(name = "accountid", nullable = false) // SOAccId
   private Account account;

   @Column(name="balance")                           // Balance
   private BigDecimal balance;

   @Column(name="checked")                           // Checked
   private boolean checked;

   @Column(name="checkedbalance")                    // CheckedBalance
   private BigDecimal checkedBalance;

   @Column(name="comment")                           // Comment
   private String comment;

   @Column(name="credit")                            // Credit
   private BigDecimal credit;

   @Column(name="transactiondate")                   // Date
   private LocalDate date;

   @Column(name="debit")                             // Debit
   private BigDecimal debit;

   @Column(name="sortedbalance")                     // SortedBalance
   private BigDecimal sortedBalance;

   @Column(name="statementref")                      // Stid
   private String stid;

   @Column(name="transactiontype")                   // Type
   private String type;

   public Transaction() {
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.getClass().getSimpleName());
      sb.append(" sequence:").append(sequence);
      sb.append(" accountId:").append(account.getAccId());
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

   public Account getAccount() {
      return this.account;
   }

   public void setAccount(Account account) {
      this.account = account;
   }

   // public Long getAccountId() {
   //    return this.accountId;
   // }
   //
   // public void setAccountId(Long accountId) {
   //    this.accountId = accountId;
   // }

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

   @Override
   public int hashCode()
   {
      return Objects.hash(account.getAccId(), balance, checked, checkedBalance, comment, credit, date, debit, sequence, sortedBalance, stid, type);
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
      Transaction other = (Transaction) obj;
      return Objects.equals(account.getAccId(), other.account.getAccId()) && Objects.equals(balance, other.balance) && checked == other.checked && Objects.equals(checkedBalance, other.checkedBalance) && Objects.equals(comment, other.comment)
            && Objects.equals(credit, other.credit) && Objects.equals(date, other.date) && Objects.equals(debit, other.debit) && sequence == other.sequence && Objects.equals(sortedBalance, other.sortedBalance)
            && Objects.equals(stid, other.stid) && Objects.equals(type, other.type);
   }

}
