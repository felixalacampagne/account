// app/transactions/txndel-confirm-modal.component.ts
import { Component, inject, Type } from '@angular/core';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
	selector: 'txndel-confirm',
	standalone: true,
	template: `
		<div class="modal-header">
			<h4 class="modal-title" id="modal-title">Transaction deletion</h4>
			<button
				type="button"
				class="btn-close"
				aria-label="Close button"
				aria-describedby="modal-title"
				(click)="modal.dismiss('CANCEL')"
			></button>
		</div>
		<div class="modal-body">
			<p>
				<strong>Are you sure you want to delete this transaction?</strong>
			</p>
			<p>
				The transaction will be permanently deleted.
				<span class="text-danger">This operation can not be undone.</span>
			</p>
		</div>
		<div class="modal-footer">
			<button type="button" ngbAutofocus class="btn btn-outline-secondary" (click)="modal.dismiss('CANCEL')">Cancel</button>
			<button type="button" class="btn btn-danger" (click)="modal.close('OK')">Ok</button>
		</div>
	`,
})
export class TxnDelConfirmDialog {
	modal = inject(NgbActiveModal);
}
