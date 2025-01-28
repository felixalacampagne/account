
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
    providers: [],
    imports: [MatDialogContent, SoEditMatComponent],
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './so-edit-mat.dialog.html',
    styleUrls: [
        './so-edit-mat.component.css'
    ]
})

export class SoEditMatDialog 
{
   data = inject(MAT_DIALOG_DATA);
   constructor(public dialogRef: MatDialogRef<SoEditMatDialog>
      
   ) { }

   // WARNING: To get the SoEditMatComponent content to fill the dialog box the
   // .mat-mdc-dialog-content max-height style had to be overriden.
   ngOnInit() 
   {

   } 
   
   closeDialog(event: any)
   {
      console.log("SoEditMatDialog: request to close received: " + JSON.stringify(event, null, 2));
      this.dialogRef.close(event);      
   }
}
