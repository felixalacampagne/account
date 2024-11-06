// src/shared/service/dateformat.service.ts
import { DatePipe } from "@angular/common";
import { Injectable } from "@angular/core";

const dateFormatJson: string = 'yyyy-MM-dd';
const dateFormatList: string = 'dd/MM/yy';
const dateFormatPicker: string = 'dd/MM/yyyy';

@Injectable()
export class DateformatService 
{
datefmt : string [];
   constructor(private datePipe: DatePipe)
   {
      this.datefmt = this.getDateFormatString();
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
      // but don't know how to do that in a regex, yet!  Or maybe I'll find a library instead.
      // It would be a nice touch to be able to interpret the d and m according to the local
      // format. Angular has a method which seems to fit the bill... except that it is deprecated!
      // The adivce is to use Intl.DateTimeFormat - but they provide no link to documentation
      // and it doesn't return the expected string representation of the date format. Of course
      // Intl.DateTimeFormat does nothing like what is required, it has nothing which returns
      // the date format. Eventually I discovered the trick in getDateFormatString on StackOverflow 
      // which gets a Date as its component parts and use the part name to construct a format.
      const datePatternA = /^(\d{1,2})[-./](\d{1,2})[-./](\d{4})$/;
      const datePatternB = /^(\d{1,2})[-./](\d{1,2})[-./](\d{2})$/;
      let ma = datePatternA.exec(datestr);

      if(ma)
      {
         date = this.mapArrayToDate(ma);
      }
      else
      {
         ma = datePatternB.exec(datestr);
         if(ma)
         {
            date = this.mapArrayToDate(ma);
         }
         else
         {
            // Let Date have a chance at parsing it - it might be one of the accepted long formats
            date = new Date(datestr);
         }
      }   
      return date;   
   }

   // Hacky method to map result of regex to Date using the
   // date format in datefmt.
   // NB. If the year value is less than 3 characters it is assumed to
   // be a year value in 2000. This wont work if the input array 
   // is changed to numbers - conversion will need to be done prior to calling mapArrayToDate.
   // WARNING: assumes index of datevals starts at 1 (ie. result of regex)
   mapArrayToDate(datevals : string []) : Date
   {
      const map = new Map();
      map.set(this.datefmt[0], datevals[1]);     
      map.set(this.datefmt[1], datevals[2]);     
      map.set(this.datefmt[2], datevals[3]); 

      let m : number = +map.get('month');
      let d : number = +map.get('day');
      let y : number = +map.get('year');    
      if(map.get('year').length < 3)
      {
         y = y + 2000;
      }
      return new Date(y, m-1, d);   
   }

   getDateFormatString(lang = 'default') :string[] {
      const formatObj : Intl.DateTimeFormatPart [] = new Intl.DateTimeFormat(lang).formatToParts(new Date());
      console.log("getDateFormatString: formatObj:" + JSON.stringify(formatObj, null, 2));
      return formatObj
        .filter(o => {
            switch (o.type) 
            {
               case "day":
               case "month":
               case "year":
                  return true;
               default:
                  return false;
            }
         })
        .map(obj => obj.type)
        ;
    }   
}