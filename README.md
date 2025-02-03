Account features: 
   - transaction display with paging, goto page, last page, device dependent pagezise (no custom page size yet),
   - transaction update 
   - standing order application and editing,
   - transfer between accounts,
   - checked balances
   - show qr code for transfer
   - transfer account details update and delete
   - account creation and details update
   
Still to come:
  - a way to trigger checked balance recalc (updates via Excel cause inconsistencies in the list
    (although the final balance appears to be correct)
  - date sorted balance display (instead of entry/id sorted)
  - transaction search (maybe)
  - better transfer account handling (the old concept of 'phone banking' accounts is no longer useful)

0.4.12 
0.4.11 11-Jan-2025 improved the transaction pageination to make it easier to use especially when reconciling. Reconciling
is a good case for search but just displaying a list of matches with no reference to the full transaction list as is done 
in the VB version. Need to find a way to goto search hits in the actual transaction list while keeping the paging.
While reconciling the credit card statement concluded that the QR code would be handy to have after all so that will be 
the next thing.

v0.4.10 04-Jan-2025 The first release of 2025 - Happy New Year! The Christmas break allowed some time to play around trying 
to display a dynamic list of counterparties based on what is typed into the counterparty name field. Initially I though this
would be simply done by populating a dropdown as it is a common scenario which must be well supported by now. Well, I did manage to 
cobble togehter something which worked with a dropdown by resorting to 'Angular Bootstrap' and some jiggery-pokery I discovered
using signals (which I don't fully understand) - in other words this now basic UI behaviour is not really supported at all. Then I 
stumbled across the 'typeahead' seciton of the 'Angular Bootstrap' documentation and realised that it was probably what I needed to 
use - the name is immensly misleading as it appears to have nothing to do with searching or dynamic list display. After a while I
managed to get it to work just about how I expect it to even though I don't understand all the things that are done, 
especially when it comes to all the prefix symbols that are sprinkled around. The final revelation was that the 'string' variable 
used for the counterparty name was actually being populated with a transfer account object! Knowing that the variable type
is completely meaningless made life a lot easier.
