// src/app/transferaccount-edit-mat/transferaccount-edit-mat.component.ts
import { Component, ChangeDetectionStrategy, Output, EventEmitter, SimpleChanges, Input } from "@angular/core";
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { Observable, takeUntil } from "rxjs";
import { AccountItem } from "src/shared/model/accountitem.model";
import { TransferAccountItem } from "src/shared/model/transferaccountitem.model";
import { AccountService } from "src/shared/service/account.service";

// More or less blindly copied from the standing order edit component
// I think the idea of having a seperate component with the actual entry
// fields and the dialog component was to enable the editor to be
// more eaisly displayed in different ways if necessary. Not really sure
// that it is worth the effort but I've copied it anyway since I don't
// really fully grasp what is going on with the dialog and data going from one
// place to another etc....
@Component({
   selector: 'transferaccount-edit-mat',
   standalone: true,
   providers: [

   ],
   imports: [MatCardModule,      
      MatInputModule,
      MatButtonModule,
      MatSelectModule,
      MatIconModule,
      ReactiveFormsModule
      // , CommonModule // for *ngIf etc.
   ],
   changeDetection: ChangeDetectionStrategy.OnPush,
   templateUrl: './transferaccount-edit-mat.component.html',
   styleUrls: [ './transferaccount-edit-mat.component.css'
      // , '../../styles.css'
   //    , '../../sass/account-styles.scss'
   
]
})

export class TransferAccountEditMatComponent 
{
   @Input() origItem: TransferAccountItem | undefined;
   @Output() public submittedEvent = new EventEmitter();

   taForm : FormGroup;

   accounts: AccountItem[] = [];

   constructor(private accountService: AccountService)
   {

       this.taForm = new FormGroup({
         // TODO: Whatever this is for transfer account
         cptyAccountName: new FormControl('', Validators.required), 
         cptyAccountNumber: new FormControl('', Validators.required), 
         lastCommunication: new FormControl('', Validators.nullValidator), 
         relatedAccount: new FormControl(null as unknown as AccountItem, Validators.nullValidator), // select from dropdown of accounts
         order: new FormControl(1, 
            [
               Validators.required,
               Validators.pattern(/^\d+$/)  // integer only
            ] ),  
        });
   }

   relatedAccountChange(value : AccountItem) 
   {
      console.log("relatedAccountChange: value:" + JSON.stringify(value));
      if(this.origItem)
      {
         if(this.origItem.id < 1)
         {
            console.log("relatedAccountChange: set cptyAccountName:" + value.name);
            this.taForm.patchValue({
               cptyAccountName : value.name
            });            
         }
      }
   }


   ngOnInit()
   {
      this.accountService.getAccounts().subscribe({
         next: (res) => {
            
            // debugger;
            if(!res)
            {
               console.log('ngOnInit: accounts is not initialized');
            }
            else
            {
               let noacc :AccountItem[] = [new AccountItem(0, "3rd-party")];
               this.accounts = noacc.concat(res);
               console.log("ngOnInit: Accounts contains " + this.accounts.length + " items.");

               if(this.origItem)
               {
                  this.populateForm(this.origItem);
               }
            }
            },
         error: (err)=>{
            console.log("ngOnInit: An error occured during getAccounts subscribe: " + JSON.stringify(err));
            } ,
         complete: ()=>{console.log("ngOnInit: getAccounts loading completed");}
      });

      
      console.log("ngOnInit:Finished");
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
            if(this.accounts.length > 0 )
            {
               // Angular/typescript sucks when it comes to getting values initialized.
               // This form needs a list of accounts which it can only get asynchronously. 
               // ngOnChanges usually starts before the list is filled so the method which
               // loads the list must call populateFormFromSO when the list is loaded, but it might
               // happen that the list has already been filled so must call populateFormFromSO from
               // here if the list is filled.
               let item : TransferAccountItem = chng.currentValue;
               this.populateForm(item);
            }
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
      
      let ta : TransferAccountItem = new TransferAccountItem();
      ta.id = this.origItem?.id ?? -1;
      ta.token = this.origItem?.token ?? '';
      ta.cptyAccountName = "" + this.taForm.value.cptyAccountName;
      ta.cptyAccountNumber = "" + this.taForm.value.cptyAccountNumber;
      ta.lastCommunication = "" + this.taForm.value.lastCommunication;
      ta.order = this.taForm.value.order;
      ta.relatedAccountId = this.taForm.value.relatedAccount.id ?? 0;
      ta.type = (ta.relatedAccountId < 1) ? "O" : "R"; // rel accs are now either 'O' or 'R' (O=Other, R=related account)
      console.log("onSubmit: orig: " + JSON.stringify(this.origItem) + " updated: " + JSON.stringify(takeUntil));

      let put : Observable<string>;
      if(ta.id < 1)
      {
         put = this.accountService.addTransferAccount(ta);
      }
      else
      {
         put = this.accountService.updateTransferAccount(ta);
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


   populateForm(ta : TransferAccountItem)
   {
      let acc : AccountItem = this.accounts.find(a => a.id==ta.relatedAccountId) ?? this.accounts[0]; // Assumes '3rd party' is always first
      console.log("populateForm: relatedAccountId:" + ta.relatedAccountId + " relatedAccount:"  + JSON.stringify(acc));
      this.taForm.setValue({
         cptyAccountName : ta.cptyAccountName,
         cptyAccountNumber : ta.cptyAccountNumber,
         lastCommunication : ta.lastCommunication,
         order : ta.order,
         relatedAccount : acc
      });
  }  
}
