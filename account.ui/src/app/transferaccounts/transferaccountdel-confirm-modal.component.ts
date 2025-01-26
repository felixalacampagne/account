// app/transactions/txndel-confirm-modal.component.ts
import { Component, inject, Type } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
   selector: 'tfraccdel-confirm',
   standalone: true,
   imports:  [MatCardModule ],
   template: `
   <mat-card class="so-card" appearance="outlined">
      <mat-card-header class="so-card-head">
         <mat-card-title>Transfer Account Delete</mat-card-title>
         <button
            type="button"
            class="btn-close"
            aria-label="Close button"
            aria-describedby="modal-title"
            (click)="dialogRef.close('CANCEL')"
         ></button>      
      </mat-card-header>

      <mat-card-content>
         <p>
            <strong>Deleting transfer account:<br />
            <span style="text-align: center">{{data.cptyAccountName}}</span>
         </strong>
         </p>
         <p>
            The transfer account will be permanently deleted.<br />
            <span class="text-danger">This operation can not be undone.</span>
         </p>
         <p>
            Are you sure you want to delete this transfer account?
         </p>         
      </mat-card-content>
      <mat-card-actions align='end'>
         <button type="button" ngbAutofocus class="btn btn-outline-secondary" (click)="dialogRef.close('CANCEL')">Cancel</button>
         <button type="button" class="btn btn-danger" (click)="dialogRef.close('DELETE_OK')">Ok</button>
      </mat-card-actions>
   `,
})
export class TransferAccountDeleteConfirmDialog {
     data = inject(MAT_DIALOG_DATA);
     constructor(public dialogRef: MatDialogRef<TransferAccountDeleteConfirmDialog>
        
     ) { }
}
