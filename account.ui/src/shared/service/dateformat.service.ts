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


}