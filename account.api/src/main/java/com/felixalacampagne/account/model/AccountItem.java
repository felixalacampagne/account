package com.felixalacampagne.account.model;

public class AccountItem
{
	long id;
	String name;

	public AccountItem() 
	{
	}
	
	public AccountItem(long id, String name)
	{
		this.id = id;
		this.name = name;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setNamec(String name)
	{
		this.name = name;
	}
}
