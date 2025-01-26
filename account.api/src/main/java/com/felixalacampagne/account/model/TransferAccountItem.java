package com.felixalacampagne.account.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/* Contains the fields required to create and edit a PhoneAccount which aren't shown
 * in the normal usage in the transactions screen.
 * 
 * NB. THe values should be the actual values from the phone account
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferAccountItem extends TfrAccountItem
{
   private Integer order;
   private String type;
   private String token;
   public TransferAccountItem(Long id, Long relatedAccountId, String cptyAccountName, 
         String cptyAccountNumber, String lastCommunication,
         Integer order, String type,
         String token)
   {
      super(id, relatedAccountId, cptyAccountName, cptyAccountNumber, lastCommunication);
      this.order = order;
      this.type = type;
      this.token = token;
   }

   public Integer getOrder()
   {
      return order;
   }

   public String getType()
   {
      return type;
   }
   
   public String toString()
   {
      return "TransferAccountItem [id=" + getId()
            + ", relatedAccountId=" + getRelatedAccountId()
            + ", order=" + order
            + ", type=" + type
            + ", cptyAccountNumber=" + getCptyAccountNumber() 
            + ", cptyAccountName=" + getCptyAccountName() 
            + ", lastCommunication=" + getLastCommunication() + "]";      
   }

   public String getToken()
   {
      return token;
   }
}
