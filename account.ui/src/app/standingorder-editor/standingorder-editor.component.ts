// src\app\standingorder-editor/standingorder-editor.component.ts
// This component is going to try to use 'reactive' forms
// The hope is that it will be displayed as a modal above the standingorder list
// and used to add or update standingorder items.


// Mysteries to be solved (requiring hours to be wasted):
// - how to link the datepicker input to the formcontrol value - think I've figured this one out.
// - why does the scss cause the labels to be in the wrong place - absolutely no clue
// - how to display the editor as a modal above the so list
// - how to populate the dropdown list fields, eg. Period, Type
// - how to require numeric/decimal values for repeat/amount
//
// such trivial things yet they will take weeks of guessing, googling, more guessing and hoping before
// they are in a usable form!!!
import { Component } from '@angular/core';
import { FormGroup, FormControl, ReactiveFormsModule } from '@angular/forms';
import { StandingOrderItem } from '../../shared/model/standingorderitem.model';
import { AccountService } from 'src/shared/service/account.service';
import { NgbDateParserFormatter, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { isoNgbDateParserFormatter } from 'src/shared/datepickformatter';
@Component({
  selector: 'standingorder-editor',
  standalone: true,
  imports: [ReactiveFormsModule, NgbModule],
  templateUrl: './standingorder-editor.component.html',

  // '../../sass/account-styles.scss', for unknown reasons the scss causes the labels toappear above the inputs instead on the same line
  styleUrls: [ '../app.component.css', './standingorder-editor.component.css'
             //  , '../../sass/account-styles.scss'
  ],
  providers: [{provide: NgbDateParserFormatter, useClass: isoNgbDateParserFormatter}]
})
export class StandingorderEditorComponent {
  editso : StandingOrderItem = new StandingOrderItem();
  
  // see https://angular.dev/guide/forms/reactive-forms
  // So this is the 'reactive' forms way of doing it. Naturally enough the Angular
  // examples give no clue how to use this in a real life scenario where a data model
  // is being editing and there is supposed to be some transfer of data between the
  // controls and the data object.
  //
  // It is not possible to use the fields of a data object as the property assigned
  // to a formcontrol so I guess the values of the form must be manually copied to
  // the data object and also from the data object when performing an update.
  // In other words it's really hard to see what the big deal of this is - seems
  // much easier to do it the same way as from transactions...
  //
  // I suppose it avoids the plethora of various different types of brakets needing
  // to be sprinkled all over the HTML so maybe its worth persevering with it... will
  // have to try to find some useful examples though...
  soForm = new FormGroup({
    soentrydate: new FormControl(),       // a date picker
    sonextpaydate: new FormControl(),     // a date picker, probably not worth it
    soamount: new FormControl(''),        // numerics only
    sodesc: new FormControl(''),          // the memo field, free format
    accountname: new FormControl(''),     // select from dropdown of accounts?
    soperiod: new FormControl(''),        // select from dropdown of period types
    socount: new FormControl(''),         // integer only
    sotfrtype: new FormControl('')        // dowpdown list of types as shown in transaction
  });

   constructor(private accountService: AccountService)
   {
   
   }

   populateFormFromSO(so : StandingOrderItem)
   {
      let d : Date = new Date(so.soentrydate); // ISO date format, ie. YYYY-MM-DD
      let entryDate = {day: d.getDate(), month: d.getMonth()+1, year: d.getFullYear()}; 
      d = new Date(so.sonextpaydate); 
      let payDate = {day: d.getDate(), month: d.getMonth()+1, year: d.getFullYear()};             
      this.soForm.setValue({
         sodesc : so.sodesc, 
         soentrydate : entryDate,
         sonextpaydate : payDate,
         soamount : so.soamount,
         accountname : so.accountname,
      //NB Looks like the FormControls only accept strings
         soperiod : "" + so.soperiod,
         socount : "" + so.socount,
         sotfrtype : so.sotfrtype
      });
   }

  onSubmit() {
    console.warn(this.soForm.value.sodesc); 

    let so : StandingOrderItem = new StandingOrderItem();
    so.sodesc = "descriptive text";
    so.accountname = "An Account";
    so.soamount = "100.99";
    so.socount = 2;
    so.soentrydate = new Date("2024-11-02");
    so.sonextpaydate = new Date("2024-11-05");
    so.soperiod = "M";
    so.sotfrtype = 'TEST';

    this.populateFormFromSO(so);

  }  
}
