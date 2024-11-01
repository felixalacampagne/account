import { NgbDateParserFormatter, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { DatePipe } from '@angular/common';
import { Injectable } from '@angular/core';
import { isNumber, toInteger, padNumber } from '@ng-bootstrap/ng-bootstrap/util/util';

@Injectable()
export class isoNgbDateParserFormatter extends NgbDateParserFormatter {

// Formats and parses date using the Javascript standard ISO format: yyyy-MM-dd
// and handles the NgbDateStruct used by the datepicker.
constructor(private datePipe: DatePipe) 
{
   super();
}

   // Cannot assign null to an object so must return a date. The default return
   // value is 'today' (I think)
   parse(value: string): NgbDateStruct 
   {
      let d : Date = new Date();
      
      //console.log("isoNgbDateParserFormatter.parse: value=" + value);
      if (value) 
      {
         d = new Date(value);
      }
      let n : NgbDateStruct = {day: d.getDate(), month: d.getMonth() + 1, year: d.getFullYear()};
      //console.log("isoNgbDateParserFormatter.parse: ret=" + n);
      return n;
   }

   format(date: NgbDateStruct): string 
   {
      let d : Date;
      //console.log("isoNgbDateParserFormatter.format: date=" + date);
      if(date == null)
      {
         d = new Date();
      }
      else
      {
         d = new Date(date.year, (date.month) - 1, date.day);
      }
      let ret : string;
      ret = this.datePipe.transform(d, 'yyyy-MM-dd') ?? '';
      //console.log("isoNgbDateParserFormatter.format: ret=" + ret);
      return ret;
   }
}
