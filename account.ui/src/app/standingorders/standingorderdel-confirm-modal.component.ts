// standingorders/standingorderdel-confirm-modal.component.ts
import { Component, inject, Type } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
    selector: 'standingorderdel-confirm',
    imports: [
        MatCardModule,
        MatButtonModule
    ],
    template: `
   <mat-card appearance="outlined">
      <mat-card-header> 
         <mat-card-title>
         <div class="title-card-left">
          Standing Order Delete
         </div>
         <div class="title-card-right">
         <button
            type="button"
            class="btn-close"
            aria-label="Close button"
            aria-describedby="modal-title"
            (click)="dialogRef.close('CANCEL')"
         ></button>
         </div>
         </mat-card-title>      
      </mat-card-header>

      <mat-card-content>
         <p>
            Deleting Standing Order:<br />
            <strong>
            <span style="text-align: center">{{data.sonextpaydate}} {{data.soamount}} {{data.accountname}}: {{data.sodesc}}</span>
         </strong>
         </p>
         <p>
            The Standing Order will be permanently deleted.<br />

            <span class="text-danger">This operation CANNOT be undone.</span>
         </p>
         <p>
            Are you sure you want to delete this Standing Order?
         </p>         
      </mat-card-content>
      <mat-card-actions align='end'>
         <button mat-stroked-button type="reset" (click)="dialogRef.close('CANCEL')">Cancel</button>
         <button mat-flat-button class="mat-error" type="submit" (click)="dialogRef.close('DELETE_OK')">Ok</button>
      </mat-card-actions>
   `,
    styleUrls: ['./standingorders.component.css',
        '../app-material.css'
        //    , '../../sass/account-styles.scss'
        // , '../app.component.css'
    ]
})
export class StandingorderDeleteConfirmDialog {
     data = inject(MAT_DIALOG_DATA);
     constructor(public dialogRef: MatDialogRef<StandingorderDeleteConfirmDialog>
        
     ) { }
}
