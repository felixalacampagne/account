package com.felixalacampagne.account.model;

public class AccountItem
{
   private long id;
   private String name;
   private String statementref;

   public AccountItem() 
   {
   }

   public AccountItem(long id, String name, String statementref)
   {
      this.id = id;
      this.name = name;
      this.statementref = statementref;
   }

   public long getId()
   {
      return id;
   }

//   public void setId(long id)
//   {
//      this.id = id;
//   }

   public String getName()
   {
      return name;
   }

//   public void setName(String name)
//   {
//      this.name = name;
//   }

   public String getStatementref()
   {
      return statementref;
   }


   @Override
   public String toString()
   {
      return "AccountItem [id=" + id 
            + ", name=" + name 
            + ", statementref=" + statementref
            + "]";
   }
}
