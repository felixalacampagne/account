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
   private String relatedAccountName;
   public TransferAccountItem(Long id, Long relatedAccountId, String cptyAccountName,
         String cptyAccountNumber, String lastCommunication,
         Integer order, String type, String relatedAccountName,
         String token)
   {
      super(id, relatedAccountId, cptyAccountName, cptyAccountNumber, lastCommunication);
      this.order = order;
      this.type = type;
      this.relatedAccountName = relatedAccountName;
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

   @Override
   public String toString()
   {
      return "TransferAccountItem [id=" + getId()
            + ", cptyAccountNumber=" + getCptyAccountNumber()
            + ", cptyAccountName=" + getCptyAccountName()
            + ", lastCommunication=" + getLastCommunication()
            + ", relatedAccountId=" + getRelatedAccountId()
            + ", relatedAccountName=" + relatedAccountName
            + ", order=" + order
            + ", type=" + type
            + "]";
   }

   public String getToken()
   {
      return token;
   }

   public String getRelatedAccountName()
   {
      return relatedAccountName;
   }
}
