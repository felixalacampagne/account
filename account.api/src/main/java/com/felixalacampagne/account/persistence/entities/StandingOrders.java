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
 * The persistent class for the "StandingOrders" database table.
 *
 */
@Entity
@Table(name="StandingOrders")
//@NamedQuery(name="StandingOrder.findAll", query="SELECT s FROM StandingOrder s")
public class StandingOrders implements Serializable {
   private static final long serialVersionUID = 1L;
   private Long SOid;

//   private Long SOAccId;
   private Account account;
   private BigDecimal SOAmount;
   private Long SOCount;
   private String SODesc;
   private LocalDate SOEntryDate;
   private LocalDate SONextPayDate;
   private String SOPeriod;
   private String SOTfrType;

   public StandingOrders() {
   }


   @Id
   @GeneratedValue(strategy=GenerationType.IDENTITY)
   @Column(name="SOid", unique=true, nullable=false)
   public Long getSOid() {
      return this.SOid;
   }

   public void setSOid(Long SOid) {
      this.SOid = SOid;
   }

   @Column(name="SOAmount", precision=100, scale=4)
   public BigDecimal getSOAmount() {
      return this.SOAmount;
   }

   public void setSOAmount(BigDecimal SOAmount) {
      this.SOAmount = SOAmount;
   }


   @Column(name="SOCount")
   public Long getSOCount() {
      return this.SOCount;
   }

   public void setSOCount(Long i) {
      this.SOCount = i;
   }


   @Column(name="SODesc", length=255)
   public String getSODesc() {
      return this.SODesc;
   }

   public void setSODesc(String SODesc) {
      this.SODesc = SODesc;
   }


   @Column(name="SOEntryDate")
   public LocalDate getSOEntryDate() {
      return this.SOEntryDate;
   }

   public void setSOEntryDate(LocalDate SOEntryDate) {
      this.SOEntryDate = SOEntryDate;
   }


   @Column(name="SONextPayDate")
   public LocalDate getSONextPayDate() {
      return this.SONextPayDate;
   }

   public void setSONextPayDate(LocalDate SONextPayDate) {
      this.SONextPayDate = SONextPayDate;
   }


   @Column(name="SOPeriod", length=1)
   public String getSOPeriod() {
      return this.SOPeriod;
   }

   public void setSOPeriod(String SOPeriod) {
      this.SOPeriod = SOPeriod;
   }


   @Column(name="SOTfrType", length=4)
   public String getSOTfrType() {
      return this.SOTfrType;
   }

   public void setSOTfrType(String SOTfrType) {
      this.SOTfrType = SOTfrType;
   }

   //uni-directional many-to-one association to AccountX
   @ManyToOne
   @JoinColumn(name = "SOAccId", nullable = false)
   public Account getAccount() {
      return this.account;
   }

   public void setAccount(Account account) {
      this.account = account;
   }

   @Override
   public String toString()
   {
      return "StandingOrders [SOid=" + SOid + ", SODesc=" + SODesc + ", SOEntryDate=" + SOEntryDate + ", SOAmount=" + SOAmount + ", SONextPayDate=" + SONextPayDate + ", SOTfrType=" + SOTfrType + ", SOPeriod=" + SOPeriod + ", SOCount="
            + SOCount + ", SOAccId=" + getAccount().getAccId() + "]";
   }


   @Override
   public int hashCode()
   {
      return Objects.hash(SOAmount, SOCount, SODesc, SOEntryDate, SONextPayDate, SOPeriod, SOTfrType, SOid, account);
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
      StandingOrders other = (StandingOrders) obj;
      return Objects.equals(SOAmount, other.SOAmount) && Objects.equals(SOCount, other.SOCount) && Objects.equals(SODesc, other.SODesc) && Objects.equals(SOEntryDate, other.SOEntryDate) && Objects.equals(SONextPayDate, other.SONextPayDate)
            && Objects.equals(SOPeriod, other.SOPeriod) && Objects.equals(SOTfrType, other.SOTfrType) && Objects.equals(SOid, other.SOid) && Objects.equals(account, other.account);
   }


}
