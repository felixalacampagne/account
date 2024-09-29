package com.felixalacampagne.account.persistence.entities;

import java.io.Serializable;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


/**
 * The persistent class for the "PhoneTrans" database table.
 *
 */
@Entity
@Table(name="PhoneTrans")
//@NamedQuery(name="PhoneTrans.findAll", query="SELECT p FROM PhoneTrans p")
public class PhoneTrans implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long PTid;
	private String PTaccom;
	private String PTamount;
	private String PTcomm;
	private Long PTdstpaid;
	private String PTErrStatus;
	private Timestamp PTPayDate;
	private Timestamp PTSentDate;
	private Long PTsrcpaid;
	private Timestamp PTTransDate;

	public PhoneTrans() {
	}


	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="PTid", unique=true, nullable=false)
	public Long getPTid() {
		return this.PTid;
	}

	public void setPTid(Long PTid) {
		this.PTid = PTid;
	}


	@Column(name="PTaccom", length=255)
	public String getPTaccom() {
		return this.PTaccom;
	}

	public void setPTaccom(String PTaccom) {
		this.PTaccom = PTaccom;
	}


	@Column(name="PTamount", length=40)
	public String getPTamount() {
		return this.PTamount;
	}

	public void setPTamount(String PTamount) {
		this.PTamount = PTamount;
	}


	@Column(name="PTcomm", length=80)
	public String getPTcomm() {
		return this.PTcomm;
	}

	public void setPTcomm(String PTcomm) {
		this.PTcomm = PTcomm;
	}


	@Column(name="PTdstpaid")
	public Long getPTdstpaid() {
		return this.PTdstpaid;
	}

	public void setPTdstpaid(Long PTdstpaid) {
		this.PTdstpaid = PTdstpaid;
	}


	@Column(name="PTErrStatus", length=255)
	public String getPTErrStatus() {
		return this.PTErrStatus;
	}

	public void setPTErrStatus(String PTErrStatus) {
		this.PTErrStatus = PTErrStatus;
	}


	@Column(name="PTPayDate")
	public Timestamp getPTPayDate() {
		return this.PTPayDate;
	}

	public void setPTPayDate(Timestamp PTPayDate) {
		this.PTPayDate = PTPayDate;
	}


	@Column(name="PTSentDate")
	public Timestamp getPTSentDate() {
		return this.PTSentDate;
	}

	public void setPTSentDate(Timestamp PTSentDate) {
		this.PTSentDate = PTSentDate;
	}


	@Column(name="PTsrcpaid")
	public Long getPTsrcpaid() {
		return this.PTsrcpaid;
	}

	public void setPTsrcpaid(Long PTsrcpaid) {
		this.PTsrcpaid = PTsrcpaid;
	}


	@Column(name="PTTransDate")
	public Timestamp getPTTransDate() {
		return this.PTTransDate;
	}

	public void setPTTransDate(Timestamp PTTransDate) {
		this.PTTransDate = PTTransDate;
	}

}
