Attribute VB_Name = "TYPEDEFS"
Global Const CB_FINDSTRINGEXACT = &H158&

Global apptitle As String
'Add version comments to GetVersionInfo...

' Structure to represent Petrol info extracted from
' the account database

Type consumption
    ConsDate As String     'Date of purchase
    ConsCost As Currency   'Const of purchase
    ConsPrice As Currency  'Price/litre
    ConsLitres As Single   '# of litres
    ConsCarDist As Long    'Car odometer reading
    ConsAve As Single      'Consumption
    ConsRunAve As Single   'Average over account period
    ConsFill As Integer    'Was it a fill up
    ConsDist As Long       'Distance over account period
    ConsReinit As Integer  'Need to reinitialise the cumulative stats - maybe for missing entries, maybe after a service.
End Type

Global Petrol() As consumption

'Structure representing info for the currently active account
Type AccInfo
   DBOPEN As Integer
   AccSet As Integer
   AccId As Long
   AccCode As String
   AccDesc As String
   AccCurr As String
   AccFmt As String
   AccSid As String
End Type

Type PrefInfo
   LockStaRefs As Integer
End Type

Global gCurrAcc As AccInfo
Global gDBName As String
Global gDB As Database
Global gTransCrit As String
Global gNewAcc As Long
Global gUser As String
Global gPassword As String


Global gTfrType As String



'Preference record names
Global Const LASTACC = "LastAccount"

'States for the global database
Global Const DBUNSET = 0
Global Const DBISOPEN = 1
Global Const DBWASOPEN = 2

Global gSQLStr As String

Function GetVersionInfo() As String

Dim s As String
Dim nl As String

nl = Chr$(13) & Chr$(10)
's = s & "                   " & vbCrLf
s = s & "22 Jan 22  4.06.01 Start an external application, eg. QR code generator, after putting" & vbCrLf
s = s & "                   details on clipboard. Tweak foreign exchange for Beobank MC statements." & vbCrLf
s = s & "                   TODO: Format details as XML" & vbCrLf
s = s & "19 Jan 22  4.05.16 Put CBC manual entry on clipboard for display as QR code. " & vbCrLf
s = s & "27 Aug 16  4.05.14 Paste updated to work with external text using simple format. " & vbCrLf
s = s & "                   CBC-Online is no more, the replacement cannot be populated" & vbCrLf
s = s & "                   programmatically. CBC-Online manual entry option updated to" & vbCrLf
s = s & "                   make copy/pasting into the browser easier." & vbCrLf
s = s & "                   Misc. bug fixes." & vbCrLf
s = s & "09 Aug 14  4.05.10 Multiple backup locations." & vbCrLf
s = s & "02 Jun 12  4.05.07 Update for CBC Eurozone form which is now the only one available." & vbCrLf
s = s & "30 Dec 10  4.05.04 Tweaks for modified CBC-Online form." & vbCrLf
s = s & "30 Dec 10  4.05.03 Fix tfr screen to use correct last balance" & vbCrLf
s = s & "02 Sep 10  4.05.01 Port to DAO 3.6 and Access 2003 unencrypted DB" & vbCrLf
s = s & "01 Sep 10  4.04.09 Supports pasting from CBC csv reports imported into Excel or from UltraEdit" & vbCrLf
s = s & "24 Feb 10  4.04.07 CBC-Online bank account name is now parsed for street, postcode and town name" & vbCrLf
s = s & "                   and the relevant fields filled in on the form. A comma must be used to separate the fields." & vbCrLf
s = s & "17 Feb 10  4.04.03 Removed dependancy on MSCAL.OCX since this is part of MS-Office," & vbCrLf
s = s & "                   not part of Windows or VB" & vbCrLf
s = s & "17 Feb 10  4.04.01 CBC-Online uses direct fill for compatibility with IE8 since sendkeys" & vbCrLf
s = s & "                   didn't work anymore. Phonebanking no longer supported at all." & vbCrLf
s = s & "21 Feb 09  4.03.37 Fix for CBC account renaming causing pending transactions" & vbCrLf
s = s & "                   to become invisible. A record update is now done." & vbCrLf
s = s & "22 Apr 08  4.03.36 Changes to CBC internet banking stuff (phone is no longer" & vbCrLf
s = s & "                   used or supported. Mods to Petrol Statistics to allow" & vbCrLf
s = s & "                   for missing entries (ie. when Linda does it!)." & vbCrLf
s = s & "05 Feb 07  4.03.27 Many minor bug fixes and adjustments especially to the CBC" & vbCrLf
s = s & "                   phone banking. New menu option of foreign currency" & vbCrLf
s = s & "                   entries since the Ctrl-F seems to suddenly not work, no" & vbCrLf
s = s & "                   idea why, maybe related to why sendkeys doesn't work with IE7." & vbCrLf
s = s & "30 Mar 03  4.03.10 Forms should now be visible on screen when restored." & vbCrLf
s = s & "                   to a screen smaller than that used the last time the" & vbCrLf
s = s & "                   form was displayed. This is to make using Remote" & vbCrLf
s = s & "                   Desktop easier on the notebook." & vbCrLf
s = s & "21 May 02  4.03.07 First pass at implementing something for CBC-Online." & vbCrLf
s = s & "                   For this version the information is fed into the loaded" & vbCrLf
s = s & "                   web form using SendKeys. The format of what is sent" & vbCrLf
s = s & "                   is configured using a template file. Only transfers to" & vbCrLf
s = s & "                   3rd parties has been tried at the moment." & vbCrLf
s = s & "08 May 02  4.03.06 Modem beep length can be specified from the ini file." & vbCrLf
s = s & "                   Modem init string can be specified in the ini file since" & vbCrLf
s = s & "                   there appears to be a problem with the Aldi-PC modem" & vbCrLf
s = s & "                   so I need to be able to test different strings more easily." & vbCrLf
s = s & "                   The problem is that the modem cuts off the call after about" & vbCrLf
s = s & "                   a minute." & vbCrLf
s = s & "04 May 02  4.03.05 Backup file time check now allows for time taken for" & vbCrLf
s = s & "                   floppy to be written which appears to be significant under Win XP." & vbCrLf
s = s & "16 Jan 02  4.03.04 New phone script. There is no longer any need to ask" & vbCrLf
s = s & "                   which currency as the BEF is no more." & vbCrLf
s = s & "06 Nov 01  4.03.00 Fixes and changes for the migration of accounts to" & vbCrLf
s = s & "                   the Euro. There is minimal support in the app. specfically for this as" & vbCrLf
s = s & "                   it's not exactly an everyday event. New features include;" & vbCrLf
s = s & "                   - Copy/Paste selected transactions between accounts with" & vbCrLf
s = s & "                     currency conversion." & vbCrLf
s = s & "                   - Copy/Paste new account and new phone banking account information." & vbCrLf
s = s & "15 May 01  4.02.00 CBC timings changed again. The send times can now be configured" & vbCrLf
s = s & "                   via the ini file and can even be adjusted during a transaction" & vbCrLf
s = s & "                   if you are quick enough." & vbCrLf
s = s & "                   Additional minor bug fixes incl. the CBC Cancel transaction now should" & vbCrLf
s = s & "                   actually cancel the transaction!" & vbCrLf
s = s & "06 Aug 00  4.01.10 Standing order calculation also required the strange balance" & vbCrLf
s = s & "                   calculation fix." & vbCrLf
s = s & "03 Jul 00  4.01.08 Fix for strange balance calculations which suddenly" & vbCrLf
s = s & "                   appearing after 4 years without a problem." & vbCrLf
s = s & "13 Dec 99  4.01.07 The Y2K checked version." & vbCrLf
s = s & "                   Fix for date removal in CBC transmit form." & vbCrLf
s = s & "                   Fix for date added when DatePicker is opened and cancelled" & vbCrLf
s = s & "                   Fix for 'You must enter...' message when entering a new standing order." & vbCrLf
s = s & "30 Oct 99  4.01.06 Standing orders allow for -ve amounts, ie. credits" & vbCrLf
s = s & "30 Oct 99  4.01.05 Transfer from check to savings (no date memo or " & vbCrLf
s = s & "                   communication) has been tested. Abort behaviour should be better now." & vbCrLf
s = s & "27 Oct 99  4.01.04 Fix for crash when setting CBC phone accounts." & vbCrLf
s = s & "23 Jun 99  4.01.00 Addition of CBC Phone format started." & vbCrLf
s = s & "28 Mar 99  4.00.12 Converted database to Access 97 format." & vbCrLf
s = s & "                   Added support for multiple phone banking accounts at" & vbCrLf
s = s & "                   the same bank (CERA only)." & vbCrLf
s = s & "                   Misc. changes to use generic routines."
s = s & "23 Jan 99  4.00.10 Resync with SourceSafe after restoration" & vbCrLf
s = s & "                   Addition of calendar function in CERA transaction form" & vbCrLf
s = s & "09 Jan 99  4.00.09 Fix inability to clear credit/debit amount (004)" & vbCrLf
s = s & "05 Jan 99  4.00.08 Converted to VB 6" & vbCrLf
s = s & "08 Sep 98  4.00 Port of version 3.18 to VB 5.0 and TrueDBGrid" & vbCrLf
's = s & "18 Jun 97  v3.18 Modified method for handling the communication" & vbCrLf
's = s & "descriptions. They now come from an array which can be used for" & vbCrLf
's = s & "expanding them during the transaction confirmation phase." & vbCrLf
's = s & "18 Jun 97  v3.17 Special handling for Belgacom communications" & nl
's = s & "23 Oct 96  v3.16 The first character entered into the Type" & nl
's = s & "  field is now uppercased automatically like the rest of" & nl
's = s & "  the field. Also added a filter to the amounts fields to" & nl
's = s & "  prevent entry of non-currency characters." & nl
's = s & "17 Oct 96  v3.16 Moved all table creation bits into one function." & nl
's = s & "  Fixed some errors in the creation of a new db caused by" & nl
's = s & "  changes made whilst using an existing db. Use of a new" & nl
's = s & "  db is still not tested fully." & nl
's = s & "16 Oct 96  v3.15 Finally figured out how to enabled Copy" & nl
's = s & "  and Paste in the main account grid. Contents of cells can" & nl
's = s & "  be copied from checked transactions. Only unchecked" & nl
's = s & "  transactions can be pasted into." & nl
's = s & "10 Oct 96  v3.15 Fixed problem in Extracts. Debit clause was selecting" & nl
's = s & "  from Credit. Also converted the date clause to use" & nl
's = s & "  numeric values" & nl
's = s & "08 Oct 96  v3.15 Added commission option to the foreign exchange format." & nl
's = s & "  This is because Eurochecks are detailed as a rate and a commission." & nl
's = s & "05 Oct 96  v3.15 prompt for new statement reference added" & nl
's = s & "  A window pops up the first time a transaction is checked" & nl
's = s & "  in which the ref can be changed for the current account." & nl
's = s & "  For some reason if the first mouse click is on the OK" & nl
's = s & "  button it is ignored. This will not normally be the" & nl
's = s & "  case though..."
'04 Oct 96  v3.15 Added option to turn off the locking of
'  set statement reference columns. It still defaults to
'  locked. Set via preferences.
'02 Oct 96  v3.14 Finally tracked down the cause of the 'object already set', I hope,
'  error which I've been getting intermittently. Caused by
'  reentrancy into Form_Activate when doing standing orders
'  on startup, for example.
'23 Sep 96  v3.13 Added option to manually execute KBFoon
'  transactions for the rare case when I enter a transaction
'  but end up having to go to the bank or dial it by hand.
'  Also added the delete button just in case.
'07 Sep 96  v3.12 Minor bug fixes.
'- Foreign exchange now uses an existing comment even if
'  it doesn't have the [].
'- New record is appended even when the current line is
'  still not written to the db.
'- Amount editing. Alt-INS formats amounts the same as
'  click does.
'- KB-Foon should release the comm port when it is done so
'  PhoneLog can auto-detect the calls.
'13 Aug 96  v3.11 Fixed the 'Type Mismatch' which caused an app exit
'  while editing new entries. Modified Transfer behaviour a bit.
'  Prevented special keys (eg. Foreign transaction) when editing a
'  cell. Disabled TGs default behaviour for Ctrl-C, which is to clear
'  the entire entry!
'25 Jul 96  v3.10 Fixed 'no record' error when adding first
'  transaction to an account, and when deleting the last
'  remaining transaction. Main account form and the extracts
'  form now have an export to Word 6.0 option. It needs some
'  refinement but it works.
'17 Jul 96  v3.09 KB entry format changed (no more To:).
'  ForEx entry changed to try and force entry of a date
'  and allow for lack of warning from TG about when the
'  record gets written. More error handling. Still trying to
'  find the Type Mismatch seen occasionaly.
'  Addition of statement ids for cross-referencing with the
'  statements from the bank. New fields are automatically added
'  by SetCurrAcc - user needs modify design permissions.
'15 Jul 96  v3.08 allows db to open to be specified on the
'  command line. Improved error handling in some functions.
'05 May 96  v3.07 Change to standing order comments. The
'  date format now refers to the Pay Date rather than the
'  entry date.
'  KB-Foon only uses future payment mode if the paydate
'  is still in the future at transmission time.
'26 Apr 96  v3.06 Displays number of pending KB transactions
'  on top line of main account form.
'  Added save/restore for the search criteria screen.
'19 Apr 96  v3.05 Can now specify a date to send a KBFoon
'  transaction on (or after). This means things can be
'  put into the queue but will not be sent until after the
'  specified date.
'22 Mar 96  v3.04 Modified Petrol statistics in honour of my
'  new car. Petrol statistics can now be determined for
'  mulitple cars, providing different keywords are used.
'19 Mar 96  v3.03 Modified sending of multiple KBFoon transactions
'15 Mar 96  v3.02 Fixed bug with phone transfer between accounts.
'  both sides were being debited.
'13 Mar 96  v3.01 Added forex conversion routine to let me
'  enter payments without needing a calculator.
'22 Feb 96  v3.0. Added the KB-Foon utility. This started out as
'  a separate program but the lack of backup to disk was
'  annoying so rather than duplicate code the whole thing
'  has gone in here.
'  Added a standing order facility to enter regular payment
'  automatically, eg. for Mortgage, car etc.
'14 Feb 96  v2.1. Date sorting of the transactions. Required
'  a new field in transactions to enable the sorted balances
'  to be displayed. Feature added so I can keep track of the
'  current balance whilst recording payments to be made in
'  the future, e.g. Mortgage, Car loan, Future phone payments.
'09 Dec 95  v2.02. Attempt to fix "data change, operation stopped"
'  error seen when playing with checked entries. Failed to find
'  cause of incorrect values seen in checked entries since they
'  went away when I started to debug!
'  Put routine in to update an edited row when a menu item
'  is selected. This means I don't have to remember to change
'  to another row after making an entry in order for the
'  change to be saved. No way to do this except to put it
'  everywhere that it might be useful - currently the main
'  menu item click events and the transfer hot keys.
'06 Dec 95  Version changed to 2.01 to indicate the presence
'  of the improved extraction function and the fixing of
'  a bug in the recalculation of balances when changes are
'  made in the middle of a list.

'  Known problems:
'  * Open DB for a second time doesn't close an existing
'    db and doesn't unload the Account menu items which
'    results in a crash.
'    [Temp fix, which will probably be permanent! Open/New
'     are disabled once a db is open.]
'  * Some strange behaviour in OS/2 Window. Seems to fail
'    to find the ini file, which results in Accounts menu
'    being enabled but not the transfer... pos. due to
'    missing last account info (which I thought was in the
'    db itself!).
'    [Permanent fix. Removed OS/2!]
'  Features to have:
'  * Archive of entries before a certain date to a separate
'    database.
'    Must update accounts and transfer transactions.
'    Must ensure at least 1 transaction remains for each account - more
'      intelligent than archive all entries before date.
'    Only checked entries to be archived.
'    No entries after an unchecked entry can be archived
'    KBFoon stuff doesn't need to be archived as the relevant info
'      is contained in the transaction comment.
'    Program will need to detect an archive and
'      prevent any editing, standing orders, KBFooning etc..
'  * Probably nice to have printing, although so far I've not
'    really felt the need for it.
GetVersionInfo = s
End Function

