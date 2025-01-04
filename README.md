v0.4.10 04-Jan-2025 The first release of 2025 - Happy New Year! The Christmas break allowed some time to play around trying 
to display a dynamic list of counterparties based on what is typed into the counterparty name field. Initially I though this
would be simply done by populating a dropdown as it is a common scenario which must be well supported by now. Well, I did manage to 
cobble togehter something which worked with a dropdown by resorting to 'Angular Bootstrap' and some jeggery-pokery I discovered
using signals (which I fully understand) - in other words this now basic UI behaviour is not really supported at all. Then I 
stumbled across the 'typeahead' seciton of the 'Angular Bootstrap' documentation and realised that it was probably what I needed to 
use - the name is immensly misleading as it appear to have nothing to do with searching or dynamic list display. After a while I
managed to get it to work just about how I expect it to even though I don't fully understand all the things that are done, 
especially when it comes to all the prefix symbols that are sprinkled around. The final revelation was that the 'string' variable 
used for the counterparty name was actually being populated with a transfer account object! Knowing this that the variable type
is completely meaningless made life a lot easier.
So now Account has the most used features: 
   - transaction display with paging,
   - transaction update 
   - standing order application and editing,
   - transfer between accounts,
   - checked balances
 
 To come:
  - account creation and details update
  - date sorted balance display (instead of entry/id sorted)
  - transaction search (maybe)
