// standingorders.component.ts
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { NgbModal, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { DeviceDetectorService } from 'ngx-device-detector';
import { StandingOrderItem } from '../../shared/model/standingorderitem.model';
import { AccountService } from '../../shared/service/account.service';
import { SoEditMatComponent } from '../so-edit-mat/so-edit-mat.component';
import { MatDialog } from '@angular/material/dialog';
import { SoEditMatDialog } from '../so-edit-mat/so-edit-mat.dialog';

export interface DialogData {
   sodata: StandingOrderItem;
 }

@Component({
  selector: 'standingorders',
  standalone: true,
  imports: [CommonModule, SoEditMatComponent],
  templateUrl: './standingorders.component.html',
  styleUrls: ['../../sass/account-styles.scss'
    , '../app.component.css'
    , './standingorders.component.css'],
  })

  export class StandingordersComponent implements OnInit {

  standorders : StandingOrderItem[] = []; 
    
  constructor(private accountService: AccountService,
    private modalService: NgbModal,
    private deviceService: DeviceDetectorService)
    {
       
    }   
   
    ngOnInit(): void {
        this.getStandingorders();
    }

   dialog = inject(MatDialog);



   getStandingorders()
   {
     console.log("StandingordersComponent.getStandingorders: Starting");
         
     this.accountService.getStandingOrders().subscribe({
         next: (res)=>{
               let  ressos : StandingOrderItem[] | undefined;
               ressos = res;

               if(!ressos)
               {
                 console.log("StandingordersComponent.getStandingorders: variable is not initialized");
               }
               else
               {
                 this.standorders = ressos; 
                 console.log("StandingordersComponent.getStandingorder: standorders contains " + this.standorders.length + " items.");
               }
             },
         error: (err)=>{
             console.log("StandingordersComponent.getStandingorders: An error occured during getTransactions subscribe" + err);
             } ,
         complete: ()=>{console.log("StandingordersComponent.getStandingorders: getStandingorders loading completed");}
      });
   
     console.log("StandingordersComponent.getStandingorders :Finished");
   }
   
   editso(so :StandingOrderItem) // maybe needs the modal content
   {
      this.dialog.open(SoEditMatDialog, { data: so } ) //;     // returns MatDialogRef  
      .afterClosed().subscribe(result => {
         console.log("StandingordersComponent.editso: dialog closed: " + JSON.stringify(result, null, 2));
         if(result == 'SUBMIT_COMPLETED')
         {
            this.getStandingorders();
         }
       });
   }

   addso() {
      let newso = new StandingOrderItem();
      this.editso(newso);
   }   
   
   isStandorders()
   {
      return this.standorders.length > 0;
   }
}
