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
      // Wanted to parse the aa/bb/yyyy format date according to local custom, ie. dd/mm/yyyy,
      // so needed to determine the local date format... yet another trivial, everyday thing, which
      // Angular/Javascript manages to fork up. Angular has a method which seems to perfect... 
      // except that it is deprecated! The adivce is to use Intl.DateTimeFormat - but they provide 
      // no link to documentation and whenyou find it it turns out it doesn't return anything 
      // like the expected string representation of the date format. Eventually I discovered the 
      // trick in getDateFormatString on StackOverflow which gets a Date as its component parts and 
      // uses the part name to derive a format. The format is then used to map the results of the
      // regex into the appropriate day/month values.
      //
      // I thought it would better to only accept a date when the same separator is used. 
      // Tried using
      //    /^(\d{1,2})(?<sep>[-.\/])(\d{1,2}?)\k<sep>(\d{4})?$/
      // which works. Note that using the lookback '\k' forces a capturing group to
      // be used for the initial separator which means the match array must be filtered to
      // remove the spurious value - I could not figure out a way around this.
      //
      // In the end I decided not to require the separators to be the same because it turns out that
      // Date will successfully parse such a value but, of course, does it with the wrong value for the month
      // which is dangerous since I might not notice that 9/11/2021 has become 11/9/2021.
      //
      // NB. The regex tester site I used said that the '/' in the separator character classs ('[-./]')
      // must be escaped however the Javascript reference states:
      //    The lexical grammar does a very rough parse of regex literals, so that it does not end the regex 
      //    literal at a / character which appears within a character class. This means /[/]/ is valid without 
      //    needing to escape the /.
      // [https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Regular_expressions/Character_class]
      // Also, for Javascript, the name of the capture group MUST be surrounded by angle brakets, '<name>',
      // single quotes are not valid.
      const datePatternA = /^(\d{1,2})[-./](\d{1,2})[-./](\d{4})$/;
      const datePatternB = /^(\d{1,2})[-./](\d{1,2})[-./](\d{2})$/;
      const datePatternC = /^((?:21|20|19)\d{2})(\d{2})(\d{2})$/; // YYYYMMDD (for my lifespan + a bit)
      const datePatternD = /^(\d{2})(\d{2})((?:21|20|19)\d{2})$/; // DDMMYYYY (for my lifespan + a bit)
      let ma = datePatternA.exec(datestr);

      // OK so this is not the most elegant way to do it... but more concerned with getting it to work than beautification,
      // problem is the mapping of the array to the date is not consistent
      // I guess I could pass the format array into mapArrayToDate,
      // the regexes could be an array of objects with regex and datefmt, then I could loop...
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
            ma = datePatternC.exec(datestr);
            if(ma)
            {
               date = new Date(+ma[1], +ma[2]-1, +ma[3]);
            }
            else
            {
               ma = datePatternD.exec(datestr);
               if(ma)
               {
                  date = new Date(+ma[3], +ma[2]-1, +ma[1]);
               }
               else
               {
                  // Let Date have a chance at parsing it - it might be one of the accepted long formats
                  date = new Date(datestr);
               }
            }
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
