package com.felixalacampagne.account.persistence.entities;

import java.io.Serializable;

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
public class PhoneAccounts implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long PAid;
	private String PAAccessCode;
	private Long PAaccid;
	private String PAdesc;
	private String PALastComm;
	private Long PAmaster;
	private String PAnumber;
	private int PAorder;
	private String paswiftbic;
	private String PAtype;

	public PhoneAccounts() {
	}


	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="PAid", unique=true, nullable=false)
	public Long getPAid() {
		return this.PAid;
	}

	public void setPAid(Long PAid) {
		this.PAid = PAid;
	}


	@Column(name="PAAccessCode", length=80)
	public String getPAAccessCode() {
		return this.PAAccessCode;
	}

	public void setPAAccessCode(String PAAccessCode) {
		this.PAAccessCode = PAAccessCode;
	}


	@Column(name="PAaccid")
	public Long getPAaccid() {
		return this.PAaccid;
	}

	public void setPAaccid(Long PAaccid) {
		this.PAaccid = PAaccid;
	}


	@Column(name="PAdesc", length=255)
	public String getPAdesc() {
		return this.PAdesc;
	}

	public void setPAdesc(String PAdesc) {
		this.PAdesc = PAdesc;
	}


	@Column(name="PALastComm", length=255)
	public String getPALastComm() {
		return this.PALastComm;
	}

	public void setPALastComm(String PALastComm) {
		this.PALastComm = PALastComm;
	}


	@Column(name="PAmaster")
	public Long getPAmaster() {
		return this.PAmaster;
	}

	public void setPAmaster(Long PAmaster) {
		this.PAmaster = PAmaster;
	}


	@Column(name="PAnumber", length=20)
	public String getPAnumber() {
		return this.PAnumber;
	}

	public void setPAnumber(String PAnumber) {
		this.PAnumber = PAnumber;
	}


	@Column(name="PAorder")
	public int getPAorder() {
		return this.PAorder;
	}

	public void setPAorder(int PAorder) {
		this.PAorder = PAorder;
	}


	@Column(length=255)
	public String getPaswiftbic() {
		return this.paswiftbic;
	}

	public void setPaswiftbic(String paswiftbic) {
		this.paswiftbic = paswiftbic;
	}


	@Column(name="PAtype", length=1)
	public String getPAtype() {
		return this.PAtype;
	}

	public void setPAtype(String PAtype) {
		this.PAtype = PAtype;
	}

}
