
// app/so-edit-mat/so-edit-mat.dialog.ts
import { ChangeDetectionStrategy } from '@angular/core';
import { Component, inject } from "@angular/core";
import {
   MatDialog,
   MAT_DIALOG_DATA,
   MatDialogTitle,
   MatDialogContent,
   MatDialogRef,
 } from '@angular/material/dialog';
import { SoEditMatComponent } from './so-edit-mat.component';

@Component({
   selector: 'so-edit-mat-dialog',
   standalone: true,
   providers: [
   ],
   imports: [MatDialogContent, SoEditMatComponent],
   changeDetection: ChangeDetectionStrategy.OnPush,
   templateUrl: './so-edit-mat.dialog.html',
   styleUrl: './so-edit-mat.component.css'
})

export class SoEditMatDialog 
{
   data = inject(MAT_DIALOG_DATA);
   constructor(public dialogRef: MatDialogRef<any>) { }

   // The dialog displays with the selected SO populating the fields.
   // TODO: The dialog does not close when the submit button is clicked
   // TODO: transmit the new/updated SO to the server
   // TODO: refresh the SO list if SO was created/updated
   //
   // The first is a major problem - no examples consider the concept of making the
   // input form a reusable component so they all mix up the modal and the form stuff.
   // Might need to resort to doing the update from the modal which means will
   // need a way to get the data from the component to here. Ugh! Why is this shirt to
   // difficult to make usable?!

   // WARNING: To get the SoEditMatComponent content to fill the dialog box the
   // .mat-mdc-dialog-content max-height style had to be overriden.
   ngOnInit() 
   {

   }   
}
