// so-edit-mat/so-edit-mat.component.ts
import { Component, inject, Injectable } from '@angular/core';

import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatSelectModule } from '@angular/material/select';
import { StandingOrderItem } from 'src/shared/model/standingorderitem.model';
import { DateAdapter, MAT_DATE_FORMATS, MatDateFormats, NativeDateAdapter } from '@angular/material/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { DatePipe } from '@angular/common';

@Injectable()
export class FormatingDateAdapter extends NativeDateAdapter 
{
   // The POS angular18 documentation says that providing a MatDateFormats is the
   // way to get the correct format displayed by the MatDatepickerModule.
   // That is a typical angular load of rowlocks - the 'provide MAT_DATE_FORMATS'
   // command is completely ignored. Obviously google contains a mountain of shirt
   // about how to get the correct format displayed, 99.99999% of which is bullshirt,
   // at least for angular18. I finally resorted to extending the NativeDateAdapter
   // with the methods that were required for the bootstrap datepicker and lo and behold
   // it actually started to forking well work! Then discovered that the format for the
   // date from the custom MAT_DATE_FORMATS was actually being supplied as a parameter
   // so in other words I'd implemented what NativeDateAdapter should have been doing in the
   // first place - yet another fours wasted away for angular and it's shirty inability
   // to provide the very basic of basic functionality such as a correctly formatted date.

   constructor(private datePipe: DatePipe) 
   {
      super();
      
   }
   override parse(value: any, parseFormat: any): Date {
      console.log("FormatingDateAdapter.format: value=" + value + " parseFormat=" + parseFormat);
       return new Date(value);
   }
   override format(date: Date, displayFormat: Object): string 
   {
      let fmt : string = 'yyyy-MM-dd';
      console.log("FormatingDateAdapter.format: date=" + date + " displayFormat: " + displayFormat);

      if(displayFormat)
      {
         fmt = "" + displayFormat;
      }
      
      let ret : string;
      ret = this.datePipe.transform(date, fmt) ?? '';
      //console.log("FormatingDateAdapter.format: ret=" + ret);
      return ret;
   }
}

export const ISO_DATE_FORMAT : MatDateFormats = {
   parse: {
     dateInput: 'yyyy-MM-dd',
   },
   display: {
     dateInput: 'yyyy-MM-dd',
     monthYearLabel: 'MMM yyyy',
     dateA11yLabel: 'LL',
     monthYearA11yLabel: 'MMMM yyyy'
   },
 };

@Component({
   selector: 'so-edit-mat',
   standalone: true,
   providers: [
      {provide: DateAdapter, useClass: FormatingDateAdapter},
      {provide: MAT_DATE_FORMATS, useValue: ISO_DATE_FORMAT}
   ],
   imports: [MatCardModule,      MatInputModule,
      MatButtonModule,
      MatSelectModule,
      MatDatepickerModule, 
      ReactiveFormsModule],
   changeDetection: ChangeDetectionStrategy.OnPush,
   templateUrl: './so-edit-mat.component.html',
   styleUrl: './so-edit-mat.component.css'
})

export class SoEditMatComponent {
//   private fb = inject(FormBuilder);
//   soForm = this.fb.group({
//       soentrydate: [null, Validators.required],       // a date picker
//       sonextpaydate: [null, Validators.required],     // a date picker, probably not worth it
//       soamount: [null, Validators.required],        // numerics only
//       sodesc: [null, Validators.required],          // the memo field, free format
//       accountname: [null, Validators.required],     // select from dropdown of accounts?
//       soperiod: [null, Validators.required],        // select from dropdown of period types
//       socount: [null, Validators.compose([
//          Validators.required, 
//          Validators.pattern("\\d*"), 
//          Validators.minLength(1), 
//          Validators.maxLength(3)])
//        ],         // integer only
//       sotfrtype: [null, Validators.required],        // dowpdown list of types as shown in transaction
//   });

// Need to share this with transactions component
public txnTypes: string[] = [
   "BC",
   "AWAL",
   "ITFR",
   "INET",
   "PPAL",
   "CARD",
   "QRMP",
   "DDBT",
   "INT",
   "TFR",
   "ZOOM"
  ];

  soForm = new FormGroup({
   soentrydate: new FormControl(new Date(), Validators.required),       // a date picker
   sonextpaydate: new FormControl(new Date(), Validators.required),     // a date picker
   soamount: new FormControl('', Validators.required),        // numerics only
   sodesc: new FormControl('', Validators.required),          // the memo field, free format
   accountname: new FormControl('', Validators.required),     // select from dropdown of accounts?
   soperiod: new FormControl('', Validators.required),        // select from dropdown of period types
   socount: new FormControl('', Validators.required),         // integer only
   sotfrtype: new FormControl(this.txnTypes[0], Validators.required)        // dowpdown list of types as shown in transaction
 });

  
  periodTypes = [
   {period: "D", desc: "Day"},
   {period: "W", desc: "Week"},
   {period: "M", desc: "Month"},
   {period: "Y", desc: "Year"}
  ];

  
  onSubmit(): void {

   console.log("onSubmit: form values: " + JSON.stringify(this.soForm, null, 2));
   let so : StandingOrderItem = new StandingOrderItem();
   so.sodesc = "descriptive text";
   so.accountname = "An Account";
   so.soamount = "100.99";
   so.socount = 2;
   so.soentrydate = "2024-11-02";
   so.sonextpaydate = "2024-11-05";
   so.soperiod = "M";
   so.sotfrtype = 'TEST';

   this.populateFormFromSO(so);
  }
  populateFormFromSO(so : StandingOrderItem)
  {
     let ed : Date = new Date(so.soentrydate); // ISO date format, ie. YYYY-MM-DD
   //   let entryDate = {day: d.getDate(), month: d.getMonth()+1, year: d.getFullYear()}; 
     let pd = new Date(so.sonextpaydate); 
   //   let payDate = {day: d.getDate(), month: d.getMonth()+1, year: d.getFullYear()};             
     this.soForm.setValue({
        sodesc : so.sodesc, 
        soentrydate : ed,
        sonextpaydate : pd,
        soamount : so.soamount,
        accountname : so.accountname,
        soperiod : "" + so.soperiod,
        socount : "" + so.socount,
        sotfrtype : so.sotfrtype
     });
  }  
}
