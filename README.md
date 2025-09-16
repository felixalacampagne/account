Account features implemented: 
   - transaction display with paging, goto page, last page, device dependent pagesize (no custom page size yet),
   - transaction update 
   - standing order application and editing/deleting,
   - transfer between accounts,
   - checked balances with manual trigger
   - show qr code for transfer
   - transfer account details update and delete
   - account creation and details update
   - permanent pay date sorted balance display

Still to come:
  - transaction search
  - non-Access database (obvious choice is mysql, or is there another type which is preferred nowadays [absolutely not Oracle!!!])
  - reconcile using CSV statement file (currently done via Excel for CBC - would be a challenge to make it generic) with some way to record exceptions encountered during the reconciliation (as happens in the Excel sheet). Would need new tables so probably will only happen after migration away from Access (can the Excel scripts work with a different DB?)

0.4.25 standing order end of month processing - when a monthly standing order date is at the end of the month then it will stick to the end of the month for subsequent entries. This means that entries on the 28th or later will eventually move to the end of the month. Thought about making this configurable but decided not worth the effort since any date after the 28th is going to move around due to the way Java does 'add month' processing, ie. 31st will become 30th and eventually 28th. Getting the UI to display a checkbox as part of the datepicker field also proved to be impossible which is another reason I abandoned the configurable EOM processing. Maybe in the future it might be interesting to add a last day X of the month, eg. last Wednesday of the month, but so far I don't have a need for anything like that.

0.4.24 various changes including standing order delete, display balances on phone in landscape mode, abort QR scan, manual trigger for balance recalculation
    
0.4.23 converted lineendings to LF and hopefully configured repo for LF endings. Requires some
local commands I think:

git config --global core.autocrlf false

git config --global core.eol lf

git config core.eol lf

git add --update --renormalize

0.4.20 Account management. To get this done as quickly as I could it is more or less a direct copy of the 
transfer account management with no attempt to make any of the code shared - partly because passing data between components 
in Angular is so convoluted and partly because the objects to be displayed are unrelated. Still, things like the delete 
confirmation could be shared but in the end not worth the time it would take - until I refactor to display the confirmation
dialog before the edit dialog is removed so cancel goes back to the edit instead of the list. 

0.4.19 01-Feb-2025 Transfer account management. Was so fed up with constantly being directed to the documentation for 
Angular Material 19 and reading about something which might work and then realising it was the wrong documentation and then
finding that once again the CIAs (continuous improvement grassholes) have made the new version incompatible with the 
previous version that I migrated to Angular 19. Needless to say this was yet another painful Angular experience. 
Never did figure out how to apply the standard Material colour variants to a button since they removed the 'color' attribute.

0.4.12 25-Jan-2025 QR code display. Creating the QR code image was no problem since I already
had all the code needed to for that. Displaying the bytes sent from the server was another matter - everything image related 
revolves around a URL to a file. Google, and hours of wasted time, to the rescue once again.

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
