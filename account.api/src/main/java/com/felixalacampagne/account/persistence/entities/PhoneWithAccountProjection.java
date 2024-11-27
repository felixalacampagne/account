package com.felixalacampagne.account.persistence.entities;

// WARNING: defualt toString is not allowed. There is no way to define a toString
// for the objects returned from a join query using an interface


// Apparently this is called an interface-based proxy Projection
public interface PhoneWithAccountProjection
{
   PhoneAccount getPhoneAccount();
   String getAccDesc();
   String getAccCode();

   public default String stringify()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("PhoneWithAccountProjection: ");
      // sb.append(this.getClass().getSimpleName()); displays like '$Proxy156'
      sb.append(" phoneAccount:").append(getPhoneAccount().toString());
      sb.append(" accDesc:").append(getAccDesc());
      sb.append(" accCode:").append(getAccCode());
      return sb.toString();
   }
}
