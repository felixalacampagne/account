package com.felixalacampagne.account.model;

public class TfrAccountItem
{
   private Long id;
   private Long relatedAccountId;
   private String cptyAccountName;
   private String cptyAccountNumber;
   private String lastCommunication;


   public TfrAccountItem()
   {
   }

   public TfrAccountItem(Long id, Long relatedAccountId, String cptyAccountName, String cptyAccountNumber, String lastCommunication)
   {
      super();
      this.id = id;
      this.relatedAccountId = relatedAccountId;
      this.cptyAccountName = cptyAccountName;
      this.cptyAccountNumber = cptyAccountNumber;
      this.lastCommunication = lastCommunication;
   }

   public Long getId()
   {
      return id;
   }

   public void setId(long id)
   {
      this.id = id;
   }

   public Long getRelatedAccountId()
   {
      return relatedAccountId;
   }

   public void setRelatedAccountId(long relatedAccountId)
   {
      this.relatedAccountId = relatedAccountId;
   }

   public String getCptyAccountName()
   {
      return cptyAccountName;
   }

   public void setCptyAccountName(String cptyAccountName)
   {
      this.cptyAccountName = cptyAccountName;
   }

   public String getCptyAccountNumber()
   {
      return cptyAccountNumber;
   }

   public void setCptyAccountNumber(String cptyAccountNumber)
   {
      this.cptyAccountNumber = cptyAccountNumber;
   }

   public String getLastCommunication()
   {
      return lastCommunication;
   }

   public void setLastCommunication(String lastCommunication)
   {
      this.lastCommunication = lastCommunication;
   }

   @Override
   public String toString()
   {
      return "TfrAccountItem [id=" + id + ", relatedAccountId=" + relatedAccountId + ", cptyAccountNumber=" + cptyAccountNumber + ", cptyAccountName=" + cptyAccountName + ", lastCommunication=" + lastCommunication + "]";
   }

}
