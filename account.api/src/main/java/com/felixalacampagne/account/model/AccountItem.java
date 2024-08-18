package com.felixalacampagne.account.model;

public class AccountItem
{
	long acc_id;
	String acc_desc;

	public AccountItem() 
	{
	}
	
	public AccountItem(long id, String desc)
	{
		this.acc_id = id;
		this.acc_desc = desc;
	}

	public long getAcc_id()
	{
		return acc_id;
	}

	public void setAcc_id(long acc_id)
	{
		this.acc_id = acc_id;
	}

	public String getAcc_desc()
	{
		return acc_desc;
	}

	public void setAcc_desc(String acc_desc)
	{
		this.acc_desc = acc_desc;
	}
}
