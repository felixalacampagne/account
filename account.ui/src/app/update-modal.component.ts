import {Component, ViewChild} from '@angular/core';
import {NgForm, NgModel} from '@angular/forms';
import {NgbModal, ModalDismissReasons, NgbDateStruct, NgbDatepickerConfig, NgbDateParserFormatter} from '@ng-bootstrap/ng-bootstrap';
import {mmddyyyNgbDateParserFormatter } from '../shared/datepickformatter';
import { FormsModule } from '@angular/forms';
@Component({
  selector: 'update-modal',
  templateUrl: './update-modal.component.html',
  providers: [{provide: NgbDateParserFormatter, useClass: mmddyyyNgbDateParserFormatter}],
  imports: [FormsModule],
  standalone: true
})

export class UpdateModalComponent {
   @ViewChild('content') content: any;
   public defaultdate: string = '';
   txDate: NgbDateStruct;
   txType: string;
   txComment: string = '';
   txAmount: string = '';
   txPastearea: string = '';
   closeResult: string = '';
   public txnTypes: string[] = [
     "BC",
     "AWAL",
     "ITFR",
     "INET",
     "PPAL",
     "CARD",
     "QRMP",
     "DDBT"
   ];

  constructor(private modalService: NgbModal) 
  {
   const d: Date = new Date();
   this.txDate = {year: d.getFullYear(), month: d.getMonth() + 1, day: d.getDate()};
   this.txType = 'BC';
  }

  open() {
    this.modalService.open(this.content, {ariaLabelledBy: 'modal-basic-title'}).result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
    }, (reason) => {
      this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });
  }

  private getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return  `with: ${reason}`;
    }
  }
  clearComment() {
   this.txComment = "";
 }
 
}
