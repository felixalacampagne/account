package com.felixalacampagne.account.persistence.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;



/**
 * The persistent class for the "PhoneAccounts" database table.
 * 
 */
@Entity
@Table(name="PhoneAccounts")
//@NamedQuery(name="PhoneAccount.findAll", query="SELECT p FROM PhoneAccount p")
public class PhoneAccount implements Serializable 
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="PAid", insertable=false)
	private Long Id;

	@Column(name="PAAccessCode")
	private String accessCode;

	@Column(name="PAaccid")
	private Long accountId;

	@Column(name="PAdesc")
	private String desc;

	@Column(name="PALastComm")
	private String lastComm;

	@Column(name="PAmaster")
	private int master;

	@Column(name="PAnumber")
	private String accountNumber;

	@Column(name="PAorder")
	private int order;

	@Column(name="PASWIFTBIC")
	private String bic;

	@Column(name="PAtype")
	private String type;

	@Override
	public String toString() 
	{
	   StringBuilder sb = new StringBuilder();
	   sb.append(this.getClass().getName());
       sb.append(" Id:").append(Id);
       sb.append(" accountId:").append(accountId);
       sb.append(" type:").append(type);
       sb.append(" master:").append(master);
       sb.append(" order:").append(order);
       sb.append(" desc:").append(desc);
       sb.append(" lastComm:").append(lastComm);
       sb.append(" accountNumber:").append(accountNumber);
       sb.append(" accessCode:").append(accessCode);
       return sb.toString();
	}
	
	public PhoneAccount() {
	}

	public Long getId() {
		return this.Id;
	}

	public void setId(Long Id) {
		this.Id = Id;
	}

	public String getAccessCode() {
		return this.accessCode;
	}

	public void setAccessCode(String accessCode) {
		this.accessCode = accessCode;
	}

	public Long getAccountId() {
		return this.accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public String getDesc() {
		return this.desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getLastComm() {
		return this.lastComm;
	}

	public void setLastComm(String lastComm) {
		this.lastComm = lastComm;
	}

	public int getMaster() {
		return this.master;
	}

	public void setMaster(int master) {
		this.master = master;
	}

	public String getAccountNumber() {
		return this.accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public int getOrder() {
		return this.order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getBic() {
		return this.bic;
	}

	public void setBic(String bic) {
		this.bic = bic;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

}