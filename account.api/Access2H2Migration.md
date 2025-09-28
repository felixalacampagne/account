# Access to H2 migration

Before starting the migration ensure as that the transaction reconciliation using Excel is as up to date
as possible since it may not work with the new H2 database.

The export from UCanaccess is fairly straight forward once the package is downloaded - there is a console.bat, as indicated 
by the documentation, and running it does connect to the database. Exporting the tables is also simple enough;

    export -t account ../../csv/account.csv;
    export -t transaction ../../csv/transaction.csv;
    export -t standingorders ../../csv/standingorders.csv;
    export -t PhoneAccounts ../../csv/phoneaccount.csv;
    export -t PhoneTrans ../../csv/phonetransaction.csv;
    export -t Prefs ../../csv/prefs.csv;

The column headers need to be modified to match the new column names.

WARNING: The CSV files MUST be edited as UTF-8, loading into UE and changing to UTF-8 doesn't seem to
work. Only way so far is to load the file, change the encoding in the status bar to UTF-8 and then drag the
file from explorer on to itself. The account.csv is the one to check as the format strings must contain
UTF-8 for the 'pound' and 'euro' signs.

The 'live' data contains invalid entries in the transaction data, ie. missing account ids. I fixed this
by searching for '^(\d{1,5});;' and replacing with '\1;25;'

Update the _h2cols.csv files in test/resources/csv with the fresh, cleaned, data, ensuring that UTF-8 is used
throughout and that the new column names are used.

Remove the accountH2create files in the db directory.

Run DatabasePopulateTest (need to remove the @Ignore first). the db directory now contains the 
migrated H2 database. The standalone development AccountApplication will run using it so it can be
validated before commiting it to the live system.
