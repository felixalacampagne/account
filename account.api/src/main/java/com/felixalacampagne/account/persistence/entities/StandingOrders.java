package com.felixalacampagne.account.persistence.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
	private Long SOAccId;
	private BigDecimal SOAmount;
	private short SOCount;
	private String SODesc;
	private Timestamp SOEntryDate;
	private Timestamp SONextPayDate;
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


	@Column(name="SOAccId")
	public Long getSOAccId() {
		return this.SOAccId;
	}

	public void setSOAccId(Long SOAccId) {
		this.SOAccId = SOAccId;
	}


	@Column(name="SOAmount", precision=100, scale=4)
	public BigDecimal getSOAmount() {
		return this.SOAmount;
	}

	public void setSOAmount(BigDecimal SOAmount) {
		this.SOAmount = SOAmount;
	}


	@Column(name="SOCount")
	public short getSOCount() {
		return this.SOCount;
	}

	public void setSOCount(short SOCount) {
		this.SOCount = SOCount;
	}


	@Column(name="SODesc", length=255)
	public String getSODesc() {
		return this.SODesc;
	}

	public void setSODesc(String SODesc) {
		this.SODesc = SODesc;
	}


	@Column(name="SOEntryDate")
	public Timestamp getSOEntryDate() {
		return this.SOEntryDate;
	}

	public void setSOEntryDate(Timestamp SOEntryDate) {
		this.SOEntryDate = SOEntryDate;
	}


	@Column(name="SONextPayDate")
	public Timestamp getSONextPayDate() {
		return this.SONextPayDate;
	}

	public void setSONextPayDate(Timestamp SONextPayDate) {
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

}