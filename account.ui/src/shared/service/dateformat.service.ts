import { DatePipe } from "@angular/common";
import { Injectable } from "@angular/core";

const dateFormatJson: string = 'yyyy-MM-dd';
const dateFormatList: string = 'dd/MM/yy';
const dateFormatPicker: string = 'dd/MM/yyyy';

@Injectable()
export class DateformatService 
{

   constructor(private datePipe: DatePipe)
   {
   }

   public listFormat(jsondate : string) : string
   {
      return this.datePipe.transform(jsondate, dateFormatList) ?? '';   
   }

   public jsonFormat(date : Date) : string
   {
      return this.datePipe.transform(date, dateFormatJson) ?? '';
   }

   public pickerFormat(date : Date) : string
   {
      return this.datePipe.transform(date, dateFormatPicker) ?? '';   
   }

   // Unbelievable, no inbuilt Javascript, Typescript or Angular date parsing functionality
   // Seems Date.parse will handle YYYY-MM-DD and something like 'DD Mmmmmmm YYYY' OK
   // completely forks up 'DD/MM/YYYY'.
   // Apparently there are libraries which might help but it appears that using regex 
   // is the generally accepted way....
   public parseDateString(datestr : string) : Date
   {
      let date : Date = new Date();

      // Strictly speaking the separator should be the same between d and m and m and y
      // but don't know how to do that in a regex, yet!  Or maybe I'll find a library instead
      const datePatternA = /^(\d{1,2})[-./](\d{1,2})[-./](\d{4})$/;
      const datePatternB = /^(\d{1,2})[-./](\d{1,2})[-./](\d{2})$/;
      let ma = datePatternA.exec(datestr);
      let m : number;
      let d : number;
      let y : number;
      if(ma)
      {
         d = +ma[1];
         m = +ma[2];
         y = +ma[3];
         date = new Date(y, m-1, d);
      }
      else
      {
         ma = datePatternB.exec(datestr);
         if(ma)
         {
            d = +ma[1];
            m = +ma[2];
            y = +ma[3] + 2000;            
            date = new Date(y, m-1, d);
         }
         else
         {
            // Let Date have a chance at parsing it - it might be one of the accepted long formats
            date = new Date(datestr);
         }
      }   
      return date;   
   }
}