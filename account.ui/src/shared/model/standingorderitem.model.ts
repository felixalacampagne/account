// standingorderitem.model.ts
export class StandingOrderItem
{
   public soid : number = -1;
   public soamount : string = '';
   public socount : number = -1;
   public sodesc : string = '';
   public soentrydate!: Date;
   public sonextpaydate!: Date;
   public soperiod : string = '';
   public sotfrtype : string = '';
   public accountid : number = -1;
   public accountname : string = '';  
   public token : string = '';
    
   constructor()
   {
       
   }
       
}
