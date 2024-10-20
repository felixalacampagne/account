// so-edit-mat/so-edit-mat.component.ts
import { Component, EventEmitter, inject, Injectable, Input, Output, SimpleChanges } from '@angular/core';

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
import { AccountService } from 'src/shared/service/account.service';
import { AccountItem } from 'src/shared/model/accountitem.model';
import { Observable } from 'rxjs';

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
      //console.log("FormatingDateAdapter.format: value=" + value + " parseFormat=" + parseFormat);
      return new Date(value);
   }
   override format(date: Date, displayFormat: Object): string 
   {
      let fmt : string = 'yyyy-MM-dd';
      //console.log("FormatingDateAdapter.format: date=" + date + " displayFormat: " + displayFormat);

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
   imports: [MatCardModule,      
      MatInputModule,
      MatButtonModule,
      MatSelectModule,
      MatDatepickerModule, 
      ReactiveFormsModule],
   changeDetection: ChangeDetectionStrategy.OnPush,
   templateUrl: './so-edit-mat.component.html',
   styleUrls: [
      './so-edit-mat.component.css'
   ]
})

export class SoEditMatComponent 
{
   @Input() origSOitem: StandingOrderItem | undefined;
   @Output() public submittedEvent = new EventEmitter();

  soForm : FormGroup;
  periodTypes : any;
  txnTypes : any;

  accounts: AccountItem[] = [];

   constructor(private accountService: AccountService, private datePipe: DatePipe)
   {
      this.periodTypes = this.accountService.periodTypes;
      this.txnTypes = this.accountService.txnTypes;
      this.soForm = new FormGroup({
         soentrydate: new FormControl('', Validators.required),       // a date picker
         sonextpaydate: new FormControl('', Validators.required),     // a date picker
         soamount: new FormControl(null as unknown as number, 
            [
               Validators.required,
               Validators.pattern(/^-?\d+(\.\d{1,2}){0,1}$/) // decimal with max 2 places only
            ] ),        
         sodesc: new FormControl('', Validators.required), 
         account: new FormControl(null as unknown as AccountItem, Validators.required), // select from dropdown of accounts
         soperiod: new FormControl('', Validators.required), // select from dropdown of period string
         socount: new FormControl(1, 
            [
               Validators.required,
               Validators.pattern(/^\d+$/)  // integer only
            ] ),         
         sotfrtype: new FormControl(this.txnTypes[0], Validators.required)  // dowpdown list of types as shown in transaction
       });
   }

   ngOnInit()
   {
      this.accountService.getAccounts().subscribe({
         next: (res) => {
            
            // debugger;
            if(!res)
            {
               console.log('SoEditMatComponent.ngOnInit: accounts is not initialized');
            }
            else
            {
               this.accounts = res;
               console.log("SoEditMatComponent.ngOnInit: Accounts contains " + this.accounts.length + " items.");
               if(this.origSOitem)
               {
                  this.populateFormFromSO(this.origSOitem);
               }
            }
            },
         error: (err)=>{
            console.log("SoEditMatComponent.ngOnInit: An error occured during getAccounts subscribe: " + JSON.stringify(err, null, 2));
            } ,
         complete: ()=>{console.log("SoEditMatComponent.ngOnInit: getAccounts loading completed");}
      });

      
      console.log("SoEditMatComponent.ngOnInit:Finished");
   }

   ngOnChanges(changes : SimpleChanges)
   {
      for (const propName in changes) 
      {
         const chng = changes[propName];
         const cur  = JSON.stringify(chng.currentValue);
         const prev = JSON.stringify(chng.previousValue);
         // console.log("propName: " + propName + " currentValue: " + cur + " previousValue: " + prev);
         if(propName == "origSOitem")
         {
            // origSOitem is already initialised with the new value
            console.log("ngOnChanges: propName: origSOitem: " + JSON.stringify(this.origSOitem, null, 2));
            if(this.accounts.length > 0 )
            {
               // Angular/typescript sucks when it comes to getting values initialized.
               // This form needs a list of accounts which it can only get asynchronously. 
               // ngOnChanges usually starts before the list is filled so the method which
               // loads the list must call populateFormFromSO when the list is loaded, but it might
               // happen that the list has already been filled so must call populateFormFromSO from
               // here if the list is filled.
               let so : StandingOrderItem = chng.currentValue;
               this.populateFormFromSO(so);
            }
         }
      } 
   }

   onCancel(): void {
      // Only cancels/avoids the form submission if the button type=reset - this is not documented anywhere
      // in the material or react docs - just have to waste hours googling until you stumble on something 
      // which looks like what you are doing and see if it works for you!
      console.log("SoEditMatComponent.onCancel:");
      this.submittedEvent.emit('CANCELLED');
   }

  onSubmit(): void {

   console.log("SoEditMatComponent.onSubmit: form values: " + JSON.stringify(this.soForm.value, null, 2));
   let fmt = 'yyyy-MM-dd';
   let ed : Date = new Date(this.soForm.value.soentrydate); // ISO date format, ie. YYYY-MM-DD
   let pd : Date = new Date(this.soForm.value.sonextpaydate);    
   let so : StandingOrderItem = new StandingOrderItem();
   so.soid = this.origSOitem?.soid ?? -1;
   so.token = this.origSOitem?.token ?? '';

   so.sodesc = "" + this.soForm.value.sodesc;
   so.accountid = this.soForm.value.account?.id ?? -1;
   so.accountname = this.soForm.value.account?.name ?? '';
   so.soamount = "" + this.soForm.value.soamount; // TODO: use number for StandingOrderItem amount
   so.socount = this.soForm.value.socount ?? -1;
   so.soentrydate = this.datePipe.transform(ed, fmt) ?? '';
   so.sonextpaydate = this.datePipe.transform(pd, fmt) ?? '';
   so.soperiod = "" + this.soForm.value.soperiod;
   so.sotfrtype = "" + this.soForm.value.sotfrtype;
   console.log("SoEditMatComponent.onSubmit: orig SO: " + JSON.stringify(this.origSOitem, null, 2) + " updated: " + JSON.stringify(so, null, 2));

   let put : Observable<string>;
   if(so.soid < 1)
   {
      put = this.accountService.addStandingOrder(so);
   }
   else
   {
      put = this.accountService.updateStandingOrder(so);
   }

   put.subscribe({
      next: (res)=>{
            console.log("SoEditMatComponent.onSubmit: response: " + JSON.stringify(res, null, 2) );
          },
      error: (err)=>{
         console.log("SoEditMatComponent.onSubmit: error: " + JSON.stringify(err, null, 2) );
          } ,
      complete: ()=>{
         console.log("SoEditMatComponent.onSubmit: complete " );
            // This should trigger closing of a parent modal
         this.submittedEvent.emit('SUBMIT_COMPLETED');
      }
   });
   
  }

   populateFormFromSO(so : StandingOrderItem)
   {
      let ed : Date = so.soentrydate ? new Date(so.soentrydate) : new Date();
      let pd : Date = so.sonextpaydate ? new Date(so.sonextpaydate) : new Date();
      let acc = this.accounts.find(a => (a.id == so.accountid));
      // console.log("SoEditMatComponent.populateFormFromSO: accountid:" + so.accountid + " account:" + JSON.stringify(acc, null, 2)) 
      let amt = so.soamount ? parseFloat(so.soamount) : null;
      this.soForm.setValue({
        sodesc : so.sodesc, 
        soentrydate : this.datePipe.transform(ed, 'yyyy-MM-dd') ?? '',
        sonextpaydate : this.datePipe.transform(pd, 'yyyy-MM-dd') ?? '',
        soamount : amt, // TODO: use number for StandingOrderItem amount
        account : acc ?? null,
        soperiod : "" + so.soperiod,
        socount :  so.socount,
        sotfrtype : so.sotfrtype
      });
  }  
}
