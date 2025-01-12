import { Component, Input } from '@angular/core';
import { CommonModule, JsonPipe } from '@angular/common';
import { EPCtransaction } from 'src/shared/model/epctransaction.model';
import { AccountService } from 'src/shared/service/account.service';

@Component({
   selector: 'qrcodepayer',
   standalone: true,
   imports: [CommonModule],
   templateUrl: './qrcodepayer.component.html',
   styleUrl: './qrcodepayer.component.css'
})
export class QrcodepayerComponent {

@Input() epctrans: EPCtransaction | undefined;

   constructor(private accountService: AccountService)
   {
   }

}
