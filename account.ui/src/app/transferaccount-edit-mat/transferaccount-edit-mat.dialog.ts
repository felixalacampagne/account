import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogContent, MatDialogRef } from '@angular/material/dialog';
import { TransferAccountEditMatComponent } from './transferaccount-edit-mat.component';

@Component({
    selector: 'transferaccount-edit-mat-dialog',
    imports: [TransferAccountEditMatComponent, MatDialogContent],
    templateUrl: './transferaccount-edit-mat.dialog.html',
    styleUrls: ['./transferaccount-edit-mat.component.css'
        //    , '../../sass/account-styles.scss'
        // , '../app.component.css'
    ]
})
export class TransferaccountEditMatDialog {

     data = inject(MAT_DIALOG_DATA);
     constructor(public dialogRef: MatDialogRef<TransferaccountEditMatDialog>) { }

     closeDialog(event: any)
     {
        console.log("closeDialog: request to close received: " + JSON.stringify(event, null, 2));
        this.dialogRef.close(event);      
     }  
}
