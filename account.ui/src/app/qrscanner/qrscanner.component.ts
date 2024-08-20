import { Component, OnInit } from '@angular/core';
import { Html5QrcodeScanner } from 'html5-qrcode';
import { Html5QrcodeResult } from 'html5-qrcode/esm/core';

@Component({
  selector: 'app-qrscanner',
  templateUrl: './qrscanner.component.html',
  styleUrls: ['./qrscanner.component.css']
})
export class QrscannerComponent implements OnInit {
  html5QrcodeScanner: Html5QrcodeScanner | undefined; // Only defined while a scan is being performed

  constructor() { }

  ngOnInit(): void {
  }



  onScanSuccess(decodedText: string, decodedResult: Html5QrcodeResult) {
    // handle the scanned code as you like, for example:
    console.log('onScanSuccess: entry');
    if(this.html5QrcodeScanner != undefined) {
      console.log('onScanSuccess: stopping scanner');
      this.html5QrcodeScanner.clear();
      this.html5QrcodeScanner = undefined;
    }  
    console.log('onScanSuccess: Parsing result ' + decodedText);
    // TODO return the QR text to the 'listener'
    // const txn : TransactionItem = this.parseEPC(decodedText);
    // console.log('onScanSuccess: setting transaction details');
    // this.txAmount = txn.amount;
    // this.txComment = txn.comment;
    // this.txType = txn.type;
    console.log('onScanSuccess: exit');
  }
  
  onScanFailure(error: any) {
    // handle scan failure, usually better to ignore and keep scanning.
    // for example:
    //console.warn(`Code scan error = ${error}`);
  }
  
  doScan() {
  
    // Second parameter should be { fps: 10, qrbox: {width: 250, height: 250} }, but it doesn't compile
    // and there doesn't appear to be anyway to create an object of the type required
    // let conf: Html5QrcodeScannerConfig = { fps: 10, qrbox: {width: 250, height: 250} supportedScanTypes:[] };
    this.html5QrcodeScanner = new Html5QrcodeScanner(
      "reader",
      { fps: 10, qrbox: {width: 250, height: 250}, supportedScanTypes: [], rememberLastUsedCamera: true },
      /* verbose= */ false);
  
    // Could not get it to work. It appeared to execute the fist console.log of onScanSuccess and then vanish
    // literally without trace. Luckily I remembered something about 'this' getting lost especially in callbacks.
    // Eventually tracked the 'bind' thing down and whoopee it works now! It's a bit tricky to use with a PC camera
    // and an image on the iPhone or a QR code displayed on the PC. But it's really intended to use on the iPhone
    // where it probably wont actually work!
    let callback = (this.onScanSuccess).bind(this);
    this.html5QrcodeScanner.render(callback,  this.onScanFailure);
  }

}
