import { Component, ElementRef, Input, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { CommonModule, JsonPipe } from '@angular/common';
import { EPCtransaction } from 'src/shared/model/epctransaction.model';
import { AccountService } from 'src/shared/service/account.service';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

@Component({
    selector: 'qrcodepayer',
    imports: [CommonModule],
    templateUrl: './qrcodepayer.component.html',
    styleUrl: './qrcodepayer.component.css'
})
export class QrcodepayerComponent {

@Input() epctrans: EPCtransaction = new EPCtransaction();
@ViewChild('qrcodeimg') qrcodeimg: ElementRef<any> | undefined;

public qrcodeimage: SafeUrl | undefined; 
   constructor(private accountService: AccountService,
      private readonly sanitizer: DomSanitizer
   )
   {
   }
   ngOnChanges(changes : SimpleChanges)
   {
      for (const propName in changes) 
      {
         const chng = changes[propName];
         const cur  = JSON.stringify(chng.currentValue);
         const prev = JSON.stringify(chng.previousValue);
         // console.log("propName: " + propName + " currentValue: " + cur + " previousValue: " + prev);
         if(propName == "epctrans")
         {
            // origSOitem is already initialised with the new value
            console.log("ngOnChanges: propName: epctrans: " + JSON.stringify(this.epctrans));

               // Angular/typescript sucks when it comes to getting values initialized.
               // This form needs a list of accounts which it can only get asynchronously. 
               // ngOnChanges usually starts before the list is filled so the method which
               // loads the list must call populateFormFromSO when the list is loaded, but it might
               // happen that the list has already been filled so must call populateFormFromSO from
               // here if the list is filled.
               let epc : EPCtransaction = chng.currentValue;
               this.loadqrcode(epc);
         }
      } 
   }

   public loadqrcode(epc: EPCtransaction) 
   {
      this.accountService.getQRCode(epc).subscribe({
         next: (res)=>{
            if(!res)
            {
              console.log("loadqrcode[next]: variable is not initialized");
            }
            else
            {
               this.displayImage(res);
               // this.getCheckedBalance(res);
            }
          },
         error: (err)=>{
             console.log("loadqrcode[error]: An error occured during subscribe: " + JSON.stringify(err, null, 2));
             } ,
         complete: ()=>{console.log("loadqrcode[complete]: completed");}
      });
   }
   
   public displayImage(image : any) 
   {
      // No forking clue...
      if(this.qrcodeimg)
      {
         const objectURL = URL.createObjectURL(image);
         this.qrcodeimg.nativeElement.src = objectURL;      
      }
   }


}
