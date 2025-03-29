// src/app/account-edit-mat/account-edit-mat.component.ts
import { Component, EventEmitter, Input, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { takeUntil, Observable } from 'rxjs';
import { AccountDetail } from 'src/shared/model/accountdetail.model';
import { AccountService } from 'src/shared/service/account.service';

@Component({
   selector: 'account-edit-mat',
   imports: [
      MatCardModule,
      MatInputModule,
      MatButtonModule,
      MatSelectModule,
      MatIconModule,
      ReactiveFormsModule
   ],
   templateUrl: './account-edit-mat.component.html',
   styleUrls: [
      './account-edit-mat.component.css',
      '../app-material.css'
   ]
})
export class AccountEditMatComponent implements OnInit {
   @Input() origItem: AccountDetail | undefined;
   @Output() public submittedEvent = new EventEmitter();

   taForm : FormGroup;
   constructor(private accountService: AccountService)
   {
      this.taForm = new FormGroup({
      name: new FormControl('', Validators.required), 
      address: new FormControl('', Validators.required), 
      code: new FormControl('', Validators.required), 
      currency: new FormControl('', Validators.required), 
      format: new FormControl('', Validators.nullValidator), 
      telephone: new FormControl('', Validators.nullValidator), 
      statementref: new FormControl('', Validators.nullValidator),
      order: new FormControl(1, 
      [
         Validators.required,
         Validators.pattern(/^\d+$/)  // integer only
      ] )
      });
   }

   ngOnInit(): void {
      console.log("ngOnInit: nothign to do yet");
   }

   ngOnChanges(changes : SimpleChanges)
   {
      for (const propName in changes) 
      {
         const chng = changes[propName];
         const cur  = JSON.stringify(chng.currentValue);
         const prev = JSON.stringify(chng.previousValue);
         // console.log("propName: " + propName + " currentValue: " + cur + " previousValue: " + prev);
         if(propName == "origItem")
         {
            // origItem is already initialised with the new value
            console.log("ngOnChanges: propName: origItem: " + JSON.stringify(this.origItem));
            let item : AccountDetail = chng.currentValue;
            this.populateForm(item);
         }
      } 
   }

   onCancel(): void {
      // Only cancels/avoids the form submission if the button type=reset - this is not documented anywhere
      // in the material or react docs - just have to waste hours googling until you stumble on something 
      // which looks like what you are doing and see if it works for you!
      console.log("onCancel:");
      this.submittedEvent.emit('CANCELLED');
   }

   onSubmit(): void 
   {
      console.log("onSubmit: form values: " + JSON.stringify(this.taForm.value));
      
      let ta : AccountDetail = new AccountDetail();
      ta.id = this.origItem?.id ?? -1;
      ta.token = this.origItem?.token ?? '';
      ta.name = "" + this.taForm.value.name;
      ta.code = "" + this.taForm.value.code;
      ta.address = "" + this.taForm.value.address;
      ta.currency = "" + this.taForm.value.currency;
      ta.format = "" + this.taForm.value.format;
      ta.order = this.taForm.value.order;
      ta.bic = "";
      ta.telephone = this.taForm.value.telephone;
      ta.statementref = this.taForm.value.statementref;

      console.log("onSubmit: orig: " + JSON.stringify(this.origItem) + " updated: " + JSON.stringify(takeUntil));

      let put : Observable<string>;
      if(ta.id < 1)
      {
         put = this.accountService.addAccount(ta);
      }
      else
      {
         put = this.accountService.updateAccount(ta);
      }

      put.subscribe({
         next: (res)=>{
               console.log("onSubmit: response: " + JSON.stringify(res) );
            },
         error: (err)=>{
            console.log("onSubmit: error: " + JSON.stringify(err) );
            } ,
         complete: ()=>{
            console.log("onSubmit: complete " );
               // This should trigger closing of a parent modal
            this.submittedEvent.emit('SUBMIT_COMPLETED');
            this.accountService.notifyAccountModified(ta.id);
         }
      });
   
   }

   canDelete() : boolean
   {
      if(this.origItem)
      {
         if(this.origItem.id > 0)
         {
            return true;
         }
      }
      return false;
   }

   onDelete()
   {
      console.log("onDelete: start");
      this.submittedEvent.emit('SUBMIT_DELETE');
      console.log("onDelete: finish");
   }


   populateForm(ta : AccountDetail)
   {
      console.log("populateForm: ta:" +  JSON.stringify(ta));
      this.taForm.setValue({
         name : ta.name ?? "",
         address : ta.address ?? "",
         code : ta.code ?? "",
         currency : ta.currency ?? "",
         format : ta.format ?? "",
         telephone : ta.telephone ?? "",
         statementref : ta.statementref ?? "",
         order : ta.order,
      });
  }     
}
