// src\app\standingorder-editor\standingorder-editor.component.ts
// This component is going to try to use 'reactive' forms
// The hope is that it will be displayed as a modal above the standingorder list
// and used to add or update standingorder items.

import { Component } from '@angular/core';
import { FormGroup, FormControl, ReactiveFormsModule } from '@angular/forms';
import { StandingOrderItem } from '../../shared/model/standingorderitem.model';
@Component({
  selector: 'standingorder-editor',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './standingorder-editor.component.html',
  styleUrl: './standingorder-editor.component.css'
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
    soentrydate: new FormControl(''),     // a date picker, probably not worth it
    sonextpaydate: new FormControl(''),   // a date picker, probably not worth it
    soamount: new FormControl(''),        // numerics only
    sodesc: new FormControl(''),          // the memo field, free format
    accountname: new FormControl(''),     // select from dropdown of accounts?
    soperiod: new FormControl(''),        // select from dropdown of period types
    socount: new FormControl(''),         // integer only
    sotfrtype: new FormControl('')        // dowpdown list of types as shown in transaction
  });


  onSubmit() {
    console.warn(this.soForm.value.sodesc);  
  }  
}
