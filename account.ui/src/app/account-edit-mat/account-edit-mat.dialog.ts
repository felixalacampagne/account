import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogContent, MatDialogRef } from '@angular/material/dialog';
import { AccountEditMatComponent } from './account-edit-mat.component';

@Component({
    selector: 'account-edit-mat-dialog',
    imports: [AccountEditMatComponent, MatDialogContent],
   //  templateUrl: './account-edit-mat.dialog.html',
   template: `
<mat-dialog-content>
   <account-edit-mat (submittedEvent)="closeDialog($event)" [origItem]="data"></account-edit-mat>
</mat-dialog-content>
`,   
    styleUrls: ['./account-edit-mat.component.css'
        //    , '../../sass/account-styles.scss'
        // , '../app.component.css'
    ]
})
export class AccountEditMatDialog {

     data = inject(MAT_DIALOG_DATA);
     constructor(public dialogRef: MatDialogRef<AccountEditMatDialog>) { }

     closeDialog(event: any)
     {
        console.log("closeDialog: request to close received: " + JSON.stringify(event, null, 2));
        this.dialogRef.close(event);      
     }  
}
