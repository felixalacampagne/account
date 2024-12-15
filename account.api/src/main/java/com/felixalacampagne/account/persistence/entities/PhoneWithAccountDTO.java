package com.felixalacampagne.account.persistence.entities;

import java.util.Objects;

public class PhoneWithAccountDTO
{
// Wanted to add a toString method to easily print the found records using a join query.
// Unfortunately it is not possible to provide a default toString on an interface.
// According to Google it should be possible to use a class as the query return type
// but naturally the reality is completely different. Initially every combination of getter, constructor etc. I
// tried resulted in:
// org.springframework.core.convert.ConverterNotFoundException: No converter found capable of converting from type [org.springframework.data.jpa.repository.query.AbstractJpaQuery$TupleConverter$TupleBackedMap] to type [com.felixalacampagne.account.persistence.entities.PhoneWithAccountType]
// and there is absolutely nothing in the spring docs or SO, for that matter, regarding how to supply a converter.
//
// It seems that the spring documentation, as always, is only telling a teeny tiny fraction of the full story.
// To use a simple class as the query return type it is necessary to put the class INSIDE the @Query which really sucks!,
// eg. select new PhoneWithAccountDTO(p as phoneAccount, a.accDesc as accDesc, a.accCode as accCode).
// The class must also implement equals/hashCode (as should the Entities).


PhoneAccount phoneAccount;
String accDesc;
String accCode;

    public PhoneWithAccountDTO(PhoneAccount phoneAccount, String accDesc, String accCode)
    {
       this.phoneAccount = phoneAccount;
       this.accDesc = accDesc;
       this.accCode = accCode;
    }

    @Override
    public  String toString()
    {
       StringBuilder sb = new StringBuilder();
       sb.append(this.getClass().getSimpleName());
       sb.append(" phoneAccount:").append(phoneAccount);
       sb.append(" accDesc:").append(getAccDesc());
       sb.append(" accCode:").append(getAccCode());
       return sb.toString();
    }

    public String getAccDesc()
    {
       return accDesc;
    }

    public String getAccCode()
    {
       return accCode;
    }

   @Override
   public int hashCode()
   {
      return Objects.hash(accCode, accDesc, phoneAccount);
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      PhoneWithAccountDTO other = (PhoneWithAccountDTO) obj;
      return Objects.equals(accCode, other.accCode) && Objects.equals(accDesc, other.accDesc) && Objects.equals(phoneAccount, other.phoneAccount);
   }


 }

